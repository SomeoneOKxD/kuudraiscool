package someoneok.kic.modules.misc;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.event.ClickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.events.HypixelJoinEvent;
import someoneok.kic.models.APIException;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.modsDir;
import static someoneok.kic.utils.GeneralUtils.createClickComponent;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

public class EasyUpdater {
    // TODO: Change to actual github on main release
    private static final String UPDATE_CHECK_URL = "https://api.github.com/repos/SomeoneOKxD/balls/releases";
    private boolean updateChecked = false;
    private static final Pattern BETA = Pattern.compile("Beta(\\d+)");

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        if (updateChecked) return;

        Multithreading.schedule(() -> {
            checkForUpdates();
            updateChecked = true;
        }, 3, TimeUnit.SECONDS);
    }

    private void checkForUpdates() {
        KICLogger.info("[CFU] Checking for updates");
        try {
            JsonObject latestRelease = getLatestRelease();
            if (latestRelease == null) return;

            String latestVersion = parseVersion(latestRelease.get("tag_name").getAsString());
            String currentVersion = KIC.VERSION;
            KICLogger.info("[CFU] Latest: " + latestVersion);
            KICLogger.info("[CFU] Current: " + currentVersion);

            if (isNewerVersion(latestVersion, currentVersion)) {
                KICLogger.info("[CFU] New version detected");
                String message = "\n" + KICPrefix + " §eA new update is available: §f§lv" + latestVersion + "\n§aClick here or run /kic update to update!\n";
                sendMessageToPlayer(createClickComponent(true, message, ClickEvent.Action.RUN_COMMAND, "/kic update"));
            }
        } catch (APIException e) {
            KICLogger.info(e.getMessage());
        }
    }

    public static void downloadAndExtractUpdate() {
        try {
            JsonObject releaseJson = getLatestRelease();
            if (releaseJson == null) return;

            String remoteVersion = parseVersion(releaseJson.get("tag_name").getAsString());
            String currentVersion = KIC.VERSION;

            if (!isNewerVersion(remoteVersion, currentVersion)) {
                sendMessageToPlayer(KICPrefix + " §aYou're already using the latest version: §fv" + currentVersion);
                return;
            }

            String downloadUrl = releaseJson.getAsJsonArray("assets")
                    .get(0).getAsJsonObject()
                    .get("browser_download_url").getAsString();

            if (!modsDir.exists()) modsDir.mkdirs();

            URL fileUrl = new URL(downloadUrl);
            String fileName = fileUrl.getFile().substring(fileUrl.getFile().lastIndexOf('/') + 1);
            File modFile = new File(modsDir, fileName);

            try (InputStream in = fileUrl.openStream()) {
                Files.copy(in, modFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            markForDeletion(remoteVersion);

            sendMessageToPlayer(KICPrefix + " §aUpdate successful: New version §fv" + remoteVersion + " §awill be active after restarting the game.");
        } catch (IOException | APIException e) {
            KICLogger.info(e.getMessage());
        }
    }

    private static void markForDeletion(String latestVersion) {
        File[] existingMods = modsDir.listFiles((dir, name) ->
                name.toLowerCase().startsWith("kic-") &&
                        name.toLowerCase().endsWith(".jar") &&
                        !name.contains(latestVersion)
        );

        if (existingMods != null) {
            for (File oldMod : existingMods) {
                File renamed = new File(modsDir, oldMod.getName().replace(".jar", ".disabled"));
                if (oldMod.renameTo(renamed)) {
                    KICLogger.info("Renamed old mod: " + oldMod.getName());
                } else {
                    KICLogger.warn("Failed to rename old mod: " + oldMod.getName());
                }
            }
        }
    }

    public static void deleteOldMod() {
        File[] disabledMods = modsDir.listFiles((dir, name) ->
                name.toLowerCase().startsWith("kic-") &&
                        name.toLowerCase().endsWith(".disabled")
        );

        if (disabledMods != null) {
            for (File disabledMod : disabledMods) {
                if (disabledMod.delete()) {
                    KICLogger.info("Deleted old mod: " + disabledMod.getName());
                } else {
                    KICLogger.warn("Failed to delete old mod: " + disabledMod.getName());
                }
            }
        }
    }

    private static JsonObject getLatestRelease() throws APIException {
        JsonArray updateInfo = JsonUtils.parseString(NetworkUtils.sendGetRequest(UPDATE_CHECK_URL, false)).getAsJsonArray();
        return updateInfo.size() > 0 ? updateInfo.get(0).getAsJsonObject() : null;
    }

    private static String parseVersion(String tag) {
        return tag.startsWith("v") ? tag.substring(1) : tag;
    }

    private static boolean isNewerVersion(String newVer, String oldVer) {
        try {
            String[] a = newVer.split("\\."), b = oldVer.split("\\.");
            for (int i = 0; i < Math.max(a.length, b.length); i++) {
                int ai = i < a.length ? Integer.parseInt(a[i].replaceAll("[^\\d]", "")) : 0;
                int bi = i < b.length ? Integer.parseInt(b[i].replaceAll("[^\\d]", "")) : 0;
                if (ai > bi) return true;
                if (ai < bi) return false;
            }

            return (oldVer.contains("Beta") && !newVer.contains("Beta"))
                    || (newVer.contains("Beta") && oldVer.contains("Beta") &&
                    extractBetaNum(newVer) > extractBetaNum(oldVer));
        } catch (Exception e) {
            return false;
        }
    }

    private static int extractBetaNum(String version) {
        Matcher matcher = BETA.matcher(version);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}
