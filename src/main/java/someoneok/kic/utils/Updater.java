package someoneok.kic.utils;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonElement;
import moe.nea.libautoupdate.*;
import net.minecraft.event.ClickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.events.HypixelJoinEvent;
import someoneok.kic.utils.dev.KICLogger;

import java.util.concurrent.TimeUnit;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ChatUtils.createClickComponent;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;

public class Updater {
    private static final long CACHE_TIMEOUT = 5 * 60 * 1000L;
    private static final String GITHUB_OWNER = "SomeoneOKxD";
    private static final String GITHUB_REPO = "kuudraiscool";

    private static UpdateContext updateContext;
    private static long lastChecked = 0;
    private static PotentialUpdate cachedUpdate = null;

    private static boolean isCheckingUpdate = false;
    private static boolean isDownloadingUpdate = false;
    private static boolean updatePending = false;

    public static void initialize() {
        updateContext = new UpdateContext(
                UpdateSource.githubUpdateSource(GITHUB_OWNER, GITHUB_REPO),
                UpdateTarget.deleteAndSaveInTheSameFolder(Updater.class),
                new CurrentVersion() {
                    @Override
                    public String display() {
                        return KIC.VERSION;
                    }

                    @Override
                    public boolean isOlderThan(JsonElement element) {
                        if (element == null || element.isJsonNull()) return true;
                        String asString = element.getAsString();
                        if (asString == null) return true;
                        return getIntVersion(KIC.VERSION) < getIntVersion(asString);
                    }

                    @Override
                    public String toString() {
                        return KIC.VERSION;
                    }
                },
                KIC.MODID
        );

        updateContext.cleanup();
    }

    private static int getIntVersion(String version) {
        try {
            String[] parts = version.replace("v", "").split("\\.");
            int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return major * 10000 + minor * 100 + patch;
        } catch (NumberFormatException e) {
            KICLogger.warn("[Version] Failed to parse version string: " + version);
            return 0;
        }
    }

    public static void checkForUpdates(boolean manual) {
        if (isCheckingUpdate) return;
        isCheckingUpdate = true;

        long now = System.currentTimeMillis();
        boolean isCacheValid = (now - lastChecked) < CACHE_TIMEOUT;

        if (isCacheValid && cachedUpdate != null) {
            isCheckingUpdate = false;
            handleCachedUpdate(manual);
            return;
        }

        updateContext.checkUpdate("full").thenAccept(potentialUpdate -> {
            lastChecked = System.currentTimeMillis();
            cachedUpdate = potentialUpdate;
            isCheckingUpdate = false;
            handleCachedUpdate(manual);
        }).exceptionally(ex -> {
            isCheckingUpdate = false;
            sendMessageToPlayer(KICPrefix + " §cFailed to check for updates please try again later or ask for help in the discord.");
            KICLogger.error("[CFU] Error while checking for update: " + ex.getMessage());
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
            downloadUpdate(cachedUpdate);
            return;
        }

        isDownloadingUpdate = true;
        updateContext.checkUpdate("full").thenAccept(potentialUpdate -> {
            lastChecked = System.currentTimeMillis();
            cachedUpdate = potentialUpdate;
            isDownloadingUpdate = false;

            if (potentialUpdate != null && potentialUpdate.isUpdateAvailable()) {
                downloadUpdate(potentialUpdate);
            } else {
                sendMessageToPlayer(KICPrefix + " §aYou're already on the latest version.");
            }
        }).exceptionally(ex -> {
            isDownloadingUpdate = false;
            sendMessageToPlayer(KICPrefix + " §cFailed to check for updates please try again later or ask for help in the discord.");
            KICLogger.error("[PU] Error while checking for update: " + ex.getMessage());
            return null;
        });
    }

    private static void handleCachedUpdate(boolean manual) {
        if (cachedUpdate != null && cachedUpdate.isUpdateAvailable()) {
            String message = "\n" + KICPrefix + " §eUpdate available: §f§lv" + cachedUpdate.getUpdate().getVersionNumber().getAsString()
                    + "\n§aClick here or run '/kic update' to install.\n";
            sendMessageToPlayer(createClickComponent(true, message, ClickEvent.Action.RUN_COMMAND, "/kic update"));
        } else if (manual) {
            sendMessageToPlayer(KICPrefix + " §aYou're already on the latest version.");
        }
    }

    private static void downloadUpdate(PotentialUpdate potentialUpdate) {
        isDownloadingUpdate = true;

        potentialUpdate.launchUpdate().thenRun(() -> {
            isDownloadingUpdate = false;
            updatePending = true;
            sendMessageToPlayer(KICPrefix + " §aUpdate downloaded. Restart the game to apply it.");
        }).exceptionally(ex -> {
            isDownloadingUpdate = false;
            sendMessageToPlayer(KICPrefix + " §cFailed to apply update.");
            KICLogger.error("[Download] Error: " + ex.getMessage());
            return null;
        });
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        Multithreading.schedule(() -> checkForUpdates(false), 3, TimeUnit.SECONDS);
    }
}
