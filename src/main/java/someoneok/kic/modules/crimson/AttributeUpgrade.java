package someoneok.kic.modules.crimson;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import someoneok.kic.KIC;
import someoneok.kic.models.APIException;
import someoneok.kic.models.crimson.UpgradeData;
import someoneok.kic.models.kicauction.KICAuctionItem;
import someoneok.kic.models.request.AttributeUpgradeRequest;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.List;
import java.util.Map;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.createHoverAndClickComponent;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.*;

public class AttributeUpgrade {
    private static UpgradeData upgradeData;
    private static long timestamp = 0;
    private static IChatComponent message;
    private static final GuiNewChat chatGui = KIC.mc.ingameGUI.getChatGUI();

    public static void show(AttributeUpgradeRequest attributeUpgradeRequest) {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        upgradeData = null;
        timestamp = 0;
        message = null;
        Multithreading.runAsync(() -> {
            String requestBody = KIC.GSON.toJson(attributeUpgradeRequest);
            KICLogger.info(requestBody);
            JsonObject response;
            try {
                response = JsonUtils.parseString(NetworkUtils.sendPostRequest("https://api.sm0kez.com/crimson/attribute/upgrade", true, requestBody)).getAsJsonObject();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }

            try {
                String item = (response.has("item") && !response.get("item").isJsonNull()) ? response.get("item").getAsString() : null;
                String attribute = (response.has("attribute") && !response.get("attribute").isJsonNull()) ? response.get("attribute").getAsString() : null;
                long totalCost = (response.has("totalCost") && !response.get("totalCost").isJsonNull()) ? response.get("totalCost").getAsLong() : 0;
                int startLevel = (response.has("startLevel") && !response.get("startLevel").isJsonNull()) ? response.get("startLevel").getAsInt() : 0;
                int endLevel = (response.has("endLevel") && !response.get("endLevel").isJsonNull()) ? response.get("endLevel").getAsInt() : 0;
                long timestamp = (response.has("timestamp") && !response.get("timestamp").isJsonNull()) ? response.get("timestamp").getAsLong() : 0;

                JsonObject levels = (response.has("levels") && !response.get("levels").isJsonNull()) ? response.get("levels").getAsJsonObject() : null;

                upgradeData = new UpgradeData(item, attribute, totalCost, startLevel, endLevel, timestamp, levels);

                processData();
                sendMessage();
            } catch (Exception e) {
                KICLogger.error("Error parsing auction data: " + e.getMessage());
            }
        });
    }

    private static void processData() {
        IChatComponent message = new ChatComponentText("");
        String item = upgradeData.getItem();
        String attribute = upgradeData.getAttribute();
        int startLevel = upgradeData.getStartLevel();
        int endLevel = upgradeData.getEndLevel();
        long totalCost = upgradeData.getTotalCost();
        timestamp = upgradeData.getTimestamp();
        Map<Integer, List<KICAuctionItem>> levels = upgradeData.getLevels();

        message.appendSibling(new ChatComponentText(String.format(
                "\n%s §6Cheapest way to upgrade §b%s §6on your §2%s §6from §e%d §6to §e%d§6.",
                KICPrefix, formatId(attribute), formatId(item), startLevel, endLevel)));

        message.appendSibling(new ChatComponentText(String.format(
                "\n§6Total price: §e%s§6, last refresh was §e%s§6.",
                parseToShorthandNumber(totalCost), timeSince(timestamp))));

        for (Map.Entry<Integer, List<KICAuctionItem>> entry : levels.entrySet()) {
            int level = entry.getKey();
            List<KICAuctionItem> items = entry.getValue();

            long levelCost = items.stream()
                    .mapToLong(KICAuctionItem::getPrice)
                    .sum();

            message.appendSibling(new ChatComponentText(String.format(
                    "\n\n§bUpgrade to §e%d §b| §e%s", level, parseToShorthandNumber(levelCost))));

            for (KICAuctionItem itemData : items) {
                message.appendSibling(createHoverAndClickComponent(true,
                        String.format("\n §6- §9%s §6for §e%s", formatId(itemData.getItemId()), parseToShorthandNumber(itemData.getPrice())),
                        "§7Click to view this auction", "/viewauction " + itemData.getUuid()));
            }
        }

        AttributeUpgrade.message = message;
    }

    private static void sendMessage() {
        if (message != null) {
            sendOrReplaceKicMessage(message);
        }
    }

    private static void sendOrReplaceKicMessage(IChatComponent message) {
        int id = (int) (timestamp % 1_000_000);
        chatGui.deleteChatLine(id);
        chatGui.printChatMessageWithOptionalDeletion(message, id);
    }
}
