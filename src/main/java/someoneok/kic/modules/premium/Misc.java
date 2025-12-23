package someoneok.kic.modules.premium;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.models.APIException;
import someoneok.kic.models.PlayerInfo;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.PartyUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.apiHost;
import static someoneok.kic.utils.ApiUtils.hasPremium;
import static someoneok.kic.utils.ChatUtils.createHoverComponent;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.formatId;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class Misc {
    public static void checkParty() {
        if (!hasPremium()) {
            sendMessageToPlayer(KIC.KICPrefix + " §cThis is a premium feature.");
            return;
        }

        if (!PartyUtils.inParty()) {
            sendMessageToPlayer(KIC.KICPrefix + " §cYou are not in a party.");
            return;
        }

        List<String> uuids = PartyUtils.getMembers().stream().map(UUID::toString).collect(Collectors.toList());

        if (uuids.isEmpty()) return;
        Multithreading.runAsync(() -> {
            String requestBody = KIC.GSON.toJson(uuids);
            JsonArray response;
            try {
                response = JsonUtils.parseString(NetworkUtils.sendPostRequest(apiHost() + "/premium/kic", true, requestBody)).getAsJsonArray();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }

            try {
                List<KICUserData> data = new ArrayList<>();
                if (response != null && response.isJsonArray()) {
                    for (JsonElement element : response) {
                        if (!element.isJsonObject()) continue;

                        JsonObject user = element.getAsJsonObject();

                        JsonObject playerInfo = user.getAsJsonObject("playerInfo");
                        String username = playerInfo.get("username").getAsString();
                        String uuid = playerInfo.get("uuid").getAsString();

                        boolean online = user.get("online").getAsBoolean();

                        data.add(new KICUserData(new PlayerInfo(username, uuid), online));
                    }
                }
                processPartyData(data);
            } catch (Exception e) {
                KICLogger.error("Error parsing auction data: " + e.getMessage());
            }
        });
    }

    private static void processPartyData(List<KICUserData> data) {
        if (data.isEmpty()) {
            sendMessageToPlayer(new ChatComponentText(KICPrefix + " §bNo KIC users in party!"));
        } else {
            String msg = KICPrefix + " §bKIC users in party §7(§9" + data.size() + "§7) §7[§8HOVER§7]";
            String hoverText = data.stream().map(KICUserData::toString).collect(Collectors.joining("\n")).trim();
            sendMessageToPlayer(createHoverComponent(true, msg, hoverText));
        }
    }

    private static class KICUserData {
        private final PlayerInfo playerInfo;
        private final boolean online;

        public KICUserData(PlayerInfo playerInfo, boolean online) {
            this.playerInfo = playerInfo;
            this.online = online;
        }

        @Override
        public String toString() {
            return String.format("§b%s %s", playerInfo.getUsername(), online ? "§aOnline" : "§cOffline");
        }
    }

    public static void getStatus(String player) {
        if (!hasPremium()) {
            sendMessageToPlayer(KIC.KICPrefix + " §cThis is a premium feature.");
            return;
        }

        if (isNullOrEmpty(player)) {
            sendMessageToPlayer(KIC.KICPrefix + " §cInvalid player.");
            return;
        }

        Multithreading.runAsync(() -> {
            JsonObject response;
            try {
                response = JsonUtils.parseString(NetworkUtils.sendGetRequest(apiHost() + "/premium/status/" + player, true)).getAsJsonObject();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }

            try {
                if (response != null && response.isJsonObject()) {
                    JsonObject playerInfo = response.getAsJsonObject("playerInfo");
                    String username = playerInfo.get("username").getAsString();
                    String uuid = playerInfo.get("uuid").getAsString();

                    boolean online = response.get("online").getAsBoolean();
                    String gameType = response.get("gameType").getAsString();
                    String mode = response.get("mode").getAsString();
                    String map = response.get("map").getAsString();

                    StatusData data = new StatusData(new PlayerInfo(username, uuid), online, gameType, mode, map);
                    sendMessageToPlayer(KICPrefix + " " + data);
                }

            } catch (Exception e) {
                KICLogger.error("Error parsing auction data: " + e.getMessage());
            }
        });
    }

    private static class StatusData {
        private final PlayerInfo playerInfo;
        private final boolean online;
        private final String gameType;
        private final String mode;
        private final String map;

        public StatusData(PlayerInfo playerInfo, boolean online, String gameType, String mode, String map) {
            this.playerInfo = playerInfo;
            this.online = online;
            this.gameType = gameType;
            this.mode = mode;
            this.map = map;
        }

        @Override
        public String toString() {
            if (online) {
                return String.format("§b§l%s §r§acurrently in §b%s§a.", playerInfo.getUsername(), formatId(gameType));
            } else {
                return String.format("§b§l%s §r§cis currently offline.", playerInfo.getUsername());
            }
        }
    }
}
