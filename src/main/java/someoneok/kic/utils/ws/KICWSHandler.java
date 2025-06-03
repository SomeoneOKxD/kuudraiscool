package someoneok.kic.utils.ws;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.JsonObject;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.Color;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.CacheManager;
import someoneok.kic.utils.Updater;
import someoneok.kic.utils.dev.KICLogger;

import java.time.Instant;
import java.util.Objects;

import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.TitleUtils.showSubtitle;
import static someoneok.kic.utils.TitleUtils.showTitle;

public class KICWSHandler {
    public static void onWebSocketText(String message) {
        if ("ping".equals(message)) return;
        KICLogger.info("Received message: " + message);
        JsonObject data = JsonUtils.parseString(message).getAsJsonObject();
        if (data == null) return;
        switch (data.get("type").getAsString()) {
            case "CHAT":
                handleChat(data, false, false);
                break;
            case "PREMIUM_CHAT":
                if (ApiUtils.hasPremium()) handleChat(data, false, true);
                break;
            case "DISCORD":
                handleChat(data, true, false);
                break;
            case "PREMIUM_DISCORD":
                if (ApiUtils.hasPremium()) handleChat(data, true, true);
                break;
            case "UPDATE":
                handleUpdate();
                break;
            case "ANNOUNCEMENT":
                handleAnnouncement(data);
                break;
            case "TITLE":
                handleTitle(data);
                break;
            case "DATA_UPDATE":
                handleDataUpdate(data);
                break;
            case "ERROR":
                handleError();
                break;
        }
    }

    private static void handleChat(JsonObject data, boolean discord, boolean premium) {
        boolean enabled = premium ? KICConfig.kicPlusChat : KICConfig.kicChat;
        if (!enabled) return;

        try {
            String user = data.get("user").getAsString();
            JsonObject msgData = data.getAsJsonObject("data");

            String message = msgData.get("message").getAsString().replace("\n", " ");
            String textColor = premium ? Color.getColorCode(KICConfig.kicPlusChatColor) : Color.getColorCode(KICConfig.kicChatColor);
            String kicPrefix = premium ? KIC.KICPlusPrefix : KIC.KICPrefix;
            String discordTag = discord ? " §7(§bD§7)" : "";

            if (msgData.has("showNickname") && msgData.get("showNickname").getAsBoolean()) {
                user = msgData.get("nickname").getAsString();
            }

            String chatLine;
            if (msgData.has("showPrefix") && msgData.get("showPrefix").getAsBoolean()) {
                String prefix = msgData.get("prefix").getAsString();
                chatLine = String.format("%s §7| §r§7[%s§r§7] §3%s§7%s: %s%s", kicPrefix, prefix, user, discordTag, textColor, message);
            } else {
                chatLine = String.format("%s §7| §3%s§7%s: %s%s", kicPrefix, user, discordTag, textColor, message);
            }

            sendMessageToPlayer(chatLine);
        } catch (Exception e) {
            KICLogger.error(e.getMessage());
        }
    }

    private static void handleUpdate() {
        Updater.checkForUpdates(false);
    }

    private static void handleAnnouncement(JsonObject data) {
        try {
            String message = data.getAsJsonObject("data").get("message").getAsString();
            sendMessageToPlayer(String.format("%s §7| §6§lAnnouncement: %s", KIC.KICPrefix, message));
        } catch (Exception e) {
            KICLogger.error(e.getMessage());
        }
    }

    private static void handleTitle(JsonObject data) {
        try {
            JsonObject jsonData = data.getAsJsonObject("data");
            String title = jsonData.get("title").getAsString();
            String subtitle = jsonData.get("subtitle").getAsString();
            showTitle(title, 70);
            if (!isNullOrEmpty(subtitle)) {
                showSubtitle(subtitle, 70);
            }
        } catch (Exception e) {
            KICLogger.error(e.getMessage());
        }
    }

    private static void handleDataUpdate(JsonObject data) {
        try {
            JsonObject updateData = data.getAsJsonObject("data");
            String type = updateData.get("type").getAsString();
            if (!Objects.equals("AUCTION", type)) return;

            long lastUpdated = updateData.get("lastUpdated").getAsLong();
            CacheManager.setAuctionUpdate(lastUpdated);

            if (KICConfig.notifyAhUpdate) {
                long currentTime = Instant.now().toEpochMilli();
                long timeDiff = (currentTime - lastUpdated) / 1000;

                long minutes = timeDiff / 60;
                long seconds = timeDiff % 60;

                String timeString = String.format("%dm %ds ago", minutes, seconds);

                switch (KICConfig.dataUpdateNotificationMethod) {
                    case 0:
                        sendMessageToPlayer(String.format("%s §7| §bAuction data updated! §7(§3%s§7)", KIC.KICDataPrefix, timeString));
                        break;
                    case 1:
                        Multithreading.runAsync(() -> Notifications.INSTANCE.send("[KIC] Auction data updated!", "Auction data was updated " + timeString));
                        break;
                    case 2:
                        sendMessageToPlayer(String.format("%s §7| §bAuction data updated! §7(§3%s§7)", KIC.KICDataPrefix, timeString));
                        Multithreading.runAsync(() -> Notifications.INSTANCE.send("[KIC] Auction data updated!", "Auction data was updated " + timeString));
                        break;
                }
            }
        } catch (Exception e) {
            KICLogger.error(e.getMessage());
        }
    }

    private static void handleError() {
        KICLogger.info("[KICWS] Something went wrong!");
    }
}
