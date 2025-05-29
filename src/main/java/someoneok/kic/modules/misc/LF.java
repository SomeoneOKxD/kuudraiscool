package someoneok.kic.modules.misc;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import someoneok.kic.KIC;
import someoneok.kic.models.APIException;
import someoneok.kic.models.PlayerInfo;
import someoneok.kic.models.misc.LFData;
import someoneok.kic.models.misc.LFItemData;
import someoneok.kic.models.request.LFRequest;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.GeneralUtils.*;
import static someoneok.kic.utils.StringUtils.generateDashString;

public class LF {
    private static final Set<String> sourcesWithPage = new HashSet<>(Arrays.asList("Enderchest", "Backpack", "Wardrobe"));
    private static LFData lfData;
    private static IChatComponent message;

    public static void show(LFRequest lfRequest) {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        lfData = null;
        message = null;
        Multithreading.runAsync(() -> {
            String requestBody = KIC.GSON.toJson(lfRequest);
            JsonObject response;
            try {
                response = JsonUtils.parseString(NetworkUtils.sendPostRequest("https://api.sm0kez.com/hypixel/lf", true, requestBody)).getAsJsonObject();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }

            try {
                JsonObject playerInfo = response.getAsJsonObject("playerInfo");
                JsonArray result = response.getAsJsonArray("result");

                String username = playerInfo.get("username").getAsString();
                String uuid = playerInfo.get("uuid").getAsString();

                List<LFItemData> items = new ArrayList<>();
                if (result.size() > 0) {
                    for (JsonElement itemElement : result) {
                        JsonObject item = itemElement.getAsJsonObject();
                        String name = item.has("name") ? item.get("name").getAsString() : "";
                        String lore = item.has("lore") ? item.get("lore").getAsString() : "";
                        int count = item.has("count") ? item.get("count").getAsInt() : 1;
                        String source = item.has("source") ? item.get("source").getAsString() : "";
                        int page = item.has("page") ? item.get("page").getAsInt() : -1;

                        items.add(new LFItemData(name, lore, count, source, page));
                    }
                }

                lfData = new LFData(new PlayerInfo(username, uuid), items);
                processData(lfRequest.getSearch(), lfRequest.isLore());
                if (message != null) {
                    sendMessageToPlayer(message);
                }
            } catch (Exception e) {
                KICLogger.error("Error parsing auction data: " + e.getMessage());
            }
        });
    }

    private static void processData(String search, boolean lore) {
        String startString = String.format("§2§m-----§f[- §2%s §f-]§2§m-----\n", lfData.getPlayerInfo().getUsername());
        IChatComponent message = new ChatComponentText(startString);
        message.appendSibling(new ChatComponentText(String.format("§aResults for: §7§o%s%s", search, lore ? " (lore)" : "")));

        if (lfData.getResult().isEmpty()) {
            message.appendSibling(new ChatComponentText("\n\n§cNo items found!\n"));
        } else {
            boolean isMe = mc.thePlayer.getName().equalsIgnoreCase(lfData.getPlayerInfo().getUsername());

            Map<String, LFItemData> itemMap = new LinkedHashMap<>();

            for (LFItemData item : lfData.getResult()) {
                String key = item.getName() + "||" + item.getLore() + "||" + item.getSource() + "||" + item.getPage();
                if (itemMap.containsKey(key)) {
                    itemMap.get(key).setCount(itemMap.get(key).getCount() + item.getCount());
                } else {
                    itemMap.put(key, new LFItemData(item.getName(), item.getLore(), item.getCount(), item.getSource(), item.getPage()));
                }
            }

            int num = 1;
            for (LFItemData item : itemMap.values()) {
                message.appendSibling(new ChatComponentText("\n"));
                String sourceText = getSourceText(item.getSource(), item.getPage());
                String text = String.format("§7#%d %dx %s %s", num, item.getCount(), item.getName(), sourceText);

                if (isMe) {
                    String cmd = getCmd(item.getSource(), item.getPage());
                    if (cmd == null) {
                        message.appendSibling(createHoverComponent(true, text, item.getLore()));
                    } else {
                        message.appendSibling(createHoverAndClickComponent(true, text, item.getLore(), cmd));
                    }
                } else {
                    message.appendSibling(createHoverComponent(true, text, item.getLore()));
                }
                num++;
            }
        }

        message.appendSibling(new ChatComponentText(generateDashString(startString, "§2§m")));
        LF.message = message;
    }

    private static String getCmd(String source, int page) {
        switch (source) {
            case "Pets": return "/pets";
            case "Equipment": return "/equipment";
            case "Enderchest": return "/enderchest " + page;
            case "Backpack": return "/backpack " + page;
            case "Wardrobe": return "/wardrobe " + page;
            case "Accessory Bag": return "/accessorybag";
            case "Personal Vault": return "/bank";
            default: return null;
        }
    }

    private static String getSourceText(String source, int page) {
        return sourcesWithPage.contains(source) ?
                String.format("§r§7(%s #%d)", source, page) :
                String.format("§r§7(%s)", source);
    }
}
