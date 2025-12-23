package someoneok.kic.utils.ws;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import com.google.gson.JsonObject;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.Color;
import someoneok.kic.modules.crimson.HuntingBoxValue;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.Updater;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.kuudra.KuudraValueCache;

import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.TitleUtils.showSubtitle;
import static someoneok.kic.utils.TitleUtils.showTitle;

public class KICWSHandler {
    public static void onWebSocketText(String message) {
        if ("ping".equals(message)) return;
        if (KICConfig.logWebsocketMessages) KICLogger.info("Received message: " + message);
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

            long lastUpdated = updateData.get("lastUpdated").getAsLong();
            if ("AUCTION".equals(type)) {
                KuudraValueCache.invalidateAuctionCache();
            } else if ("BAZAAR".equals(type)) {
                HuntingBoxValue.forceCleanupCaches(lastUpdated);
                KuudraValueCache.invalidateBazaarCache();
                KuudraValueCache.invalidateKeyCache();
            }
        } catch (Exception e) {
            KICLogger.error(e.getMessage());
        }
    }

    private static void handleError() {
        KICLogger.info("[KICWS] Something went wrong!");
    }
}
