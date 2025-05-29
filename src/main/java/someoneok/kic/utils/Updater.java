package someoneok.kic.utils;

import cc.polyfrost.oneconfig.utils.Multithreading;
import moe.nea.libautoupdate.*;
import net.minecraft.event.ClickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.events.HypixelJoinEvent;
import someoneok.kic.utils.dev.KICLogger;

import java.util.concurrent.TimeUnit;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.createClickComponent;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

public class Updater {
    private static final long CACHE_TIMEOUT = 5 * 60 * 1000L;
    private static UpdateContext updateContext;
    private static boolean updatePending = false;
    private static long lastChecked = 0;
    private static PotentialUpdate cachedUpdate = null;

    private static boolean isCheckingUpdate = false;
    private static boolean isDownloadingUpdate = false;

    public static void initialize() {
        updateContext = new UpdateContext(
                UpdateSource.gistSource("SomeoneOKxD", "14f6692b48192ce381939b9491d6ebe4"),
                UpdateTarget.deleteAndSaveInTheSameFolder(Updater.class),
                CurrentVersion.of(getIntVersion()),
                KIC.MODID
        );

        updateContext.cleanup();
    }

    private static int getIntVersion() {
        String[] parts = KIC.VERSION.split("\\.");
        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return major * 10000 + minor * 100 + patch;
    }

    public static void checkForUpdates(boolean manual) {
        if (isCheckingUpdate) return;
        isCheckingUpdate = true;

        long now = System.currentTimeMillis();
        boolean isCacheValid = (now - lastChecked) < CACHE_TIMEOUT;

        if (isCacheValid && cachedUpdate != null) {
            isCheckingUpdate = false;
            if (cachedUpdate.isUpdateAvailable()) {
                String message = "\n" + KICPrefix + " §eUpdate available: §f§lv" + cachedUpdate.getUpdate().getVersionName() + "\n§aClick here or run '/kic update' to install.\n";
                sendMessageToPlayer(createClickComponent(true, message, ClickEvent.Action.RUN_COMMAND, "/kic update"));
            } else if (manual) {
                sendMessageToPlayer(KICPrefix + " §aYou're already on the latest version.");
            }
            return;
        }

        updateContext.checkUpdate("upstream").thenAccept(potentialUpdate -> {
            lastChecked = System.currentTimeMillis();
            cachedUpdate = potentialUpdate;

            if (potentialUpdate != null && potentialUpdate.isUpdateAvailable()) {
                String message = "\n" + KICPrefix + " §eUpdate available: §f§lv" + cachedUpdate.getUpdate().getVersionName() + "\n§aClick here or run '/kic update' to install.\n";
                sendMessageToPlayer(createClickComponent(true, message, ClickEvent.Action.RUN_COMMAND, "/kic update"));
            } else if (manual) {
                sendMessageToPlayer(KICPrefix + " §aYou're already on the latest version.");
            }
            isCheckingUpdate = false;
        }).exceptionally(ex -> {
            sendMessageToPlayer(KICPrefix + " §cFailed to check for updates please try again later or ask for help in the discord.");
            KICLogger.error("[CFU] Error while checking for update: " + ex.getMessage());
            isCheckingUpdate = false;
            return null;
        });
    }

    public static void performUpdate() {
        if (updatePending) {
            sendMessageToPlayer(KICPrefix + " §eAn update has already been downloaded. §fRestart the game to apply it.");
            return;
        }

        if (isDownloadingUpdate) {
            sendMessageToPlayer(KICPrefix + " §eUpdate is already being downloaded...");
            return;
        }

        long now = System.currentTimeMillis();
        boolean isCacheValid = (now - lastChecked) < CACHE_TIMEOUT;

        if (isCacheValid && cachedUpdate != null && cachedUpdate.isUpdateAvailable()) {
            isDownloadingUpdate = true;
            cachedUpdate.launchUpdate().thenRun(() -> {
                updatePending = true;
                isDownloadingUpdate = false;
                sendMessageToPlayer(KICPrefix + " §aUpdate downloaded. Restart the game to apply it.");
            }).exceptionally(ex -> {
                isDownloadingUpdate = false;
                sendMessageToPlayer(KICPrefix + " §cFailed to update the mod please try again later or ask for help in the discord.");
                KICLogger.error("[PU1] Error while updating: " + ex.getMessage());
                return null;
            });
            return;
        }

        isDownloadingUpdate = true;
        updateContext.checkUpdate("upstream").thenAccept(potentialUpdate -> {
            lastChecked = System.currentTimeMillis();
            cachedUpdate = potentialUpdate;

            if (potentialUpdate == null || !potentialUpdate.isUpdateAvailable()) {
                sendMessageToPlayer(KICPrefix + " §aYou're already on the latest version.");
                isDownloadingUpdate = false;
                return;
            }

            potentialUpdate.launchUpdate().thenRun(() -> {
                updatePending = true;
                isDownloadingUpdate = false;
                sendMessageToPlayer(KICPrefix + " §aUpdate successful: New version will be active after restarting the game.");
            }).exceptionally(ex -> {
                isDownloadingUpdate = false;
                sendMessageToPlayer(KICPrefix + " §cFailed to update the mod please try again later or ask for help in the discord.");
                KICLogger.error("[PU2] Error while updating: " + ex.getMessage());
                return null;
            });
        }).exceptionally(ex -> {
            isDownloadingUpdate = false;
            sendMessageToPlayer(KICPrefix + " §cFailed to check for updates please try again later or ask for help in the discord.");
            KICLogger.error("[PU] Error while checking for update: " + ex.getMessage());
            return null;
        });
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        Multithreading.schedule(() -> checkForUpdates(false), 3, TimeUnit.SECONDS);
    }
}
