package someoneok.kic.modules.crimson;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import someoneok.kic.KIC;
import someoneok.kic.commands.InternalCommand;
import someoneok.kic.models.APIException;
import someoneok.kic.models.kicauction.*;
import someoneok.kic.models.request.AuctionDataRequest;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.models.kicauction.ArmorType.isArmorType;
import static someoneok.kic.utils.GeneralUtils.createHoverAndClickComponent;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.*;

public class AttributePrice {
    private static final int CHAT_ID_AP_MESSAGE = 757367;
    private static AuctionData auctionData;
    private static int currentCategory = 0;
    private static long timestamp = 0;
    private static IChatComponent message;
    private static String kaCmd = "/ka";
    private static final GuiNewChat chatGui = KIC.mc.ingameGUI.getChatGUI();

    public static void show(AuctionDataRequest auctionDataRequest) {
        auctionData = null;
        currentCategory = 0;
        timestamp = 0;
        message = null;
        kaCmd = "/ka";
        Multithreading.runAsync(() -> {
            String requestBody = KIC.GSON.toJson(auctionDataRequest);
            JsonObject response;
            try {
                response = JsonUtils.parseString(NetworkUtils.sendPostRequest("https://api.sm0kez.com/crimson/attribute/prices?limit=5&extra=false", true, requestBody)).getAsJsonObject();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }

            try {
                String attribute1 = (response.has("attribute1") && !response.get("attribute1").isJsonNull()) ? response.get("attribute1").getAsString() : null;
                int attributeLvl1 = (response.has("attributeLvl1") && !response.get("attributeLvl1").isJsonNull()) ? response.get("attributeLvl1").getAsInt() : 0;
                String attribute2 = (response.has("attribute2") && !response.get("attribute2").isJsonNull()) ? response.get("attribute2").getAsString() : null;
                int attributeLvl2 = (response.has("attributeLvl2") && !response.get("attributeLvl2").isJsonNull()) ? response.get("attributeLvl2").getAsInt() : 0;
                long timestamp = (response.has("timestamp") && !response.get("timestamp").isJsonNull()) ? response.get("timestamp").getAsLong() : 0;

                JsonObject armor = (response.has("armor") && !response.get("armor").isJsonNull()) ? response.get("armor").getAsJsonObject() : null;
                JsonObject equipment = (response.has("equipment") && !response.get("equipment").isJsonNull()) ? response.get("equipment").getAsJsonObject() : null;
                JsonArray shards = (response.has("shards") && !response.get("shards").isJsonNull()) ? response.get("shards").getAsJsonArray() : null;
                JsonArray rods = (response.has("rods") && !response.get("rods").isJsonNull()) ? response.get("rods").getAsJsonArray() : null;

                auctionData = new AuctionData(attribute1, attributeLvl1, attribute2, attributeLvl2, timestamp, armor, equipment, shards, rods);

                setKaCmd(auctionDataRequest);
                processData();
                sendMessage();
            } catch (Exception e) {
                KICLogger.error("Error parsing auction data: " + e.getMessage());
            }
        });
    }

    private static void setKaCmd(AuctionDataRequest request) {
        List<String> parts = new ArrayList<>();
        parts.add("/ka");

        if (request.getAttribute1() != null) parts.add(request.getAttribute1());
        if (request.getAttributeLvl1() != null) parts.add(String.valueOf(request.getAttributeLvl1()));
        if (request.getAttribute2() != null) parts.add(request.getAttribute2());
        if (request.getAttributeLvl2() != null) parts.add(String.valueOf(request.getAttributeLvl2()));

        kaCmd = String.join(" ", parts);
    }

    private static void processData() {
        IChatComponent message = new ChatComponentText("");
        String attribute1 = auctionData.getAttribute1();
        int attributeLvl1 = auctionData.getAttributeLvl1();
        String attribute2 = auctionData.getAttribute2();
        int attributeLvl2 = auctionData.getAttributeLvl2();
        timestamp = auctionData.getTimestamp();

        boolean hasAttribute2 = attribute2 != null && !attribute2.isEmpty();
        String formattedTimestamp = timeSince(timestamp);

        String header = hasAttribute2 ?
                String.format("\n%s §6Cheapest auctions for §b%s%s §6and §b%s%s", KICPrefix, attribute1,
                        (attributeLvl1 == 0 ? "" : " " + attributeLvl1), attribute2,
                        (attributeLvl2 == 0 ? "" : " " + attributeLvl2))
                : String.format("\n%s §6Cheapest auctions for §b%s%s", KICPrefix, attribute1,
                (attributeLvl1 == 0 ? "" : " " + attributeLvl1));
        message.appendSibling(new ChatComponentText(header));

        String categoryText = String.format(" §6on §2%s§6.\n", currentCategory == 0 ? "Armor" :
                (currentCategory == 1 ? "Equipment" : (currentCategory == 2 ? "Shard" : "Rod")));
        message.appendSibling(new ChatComponentText(categoryText));

        message.appendSibling(new ChatComponentText("§6Click to open the auction. Last refresh was §e" + formattedTimestamp + "\n\n"));

        if (currentCategory == 0) {
            if (hasAttribute2) {
                for (ArmorCategory category : ArmorCategory.values()) {
                    Map<String, List<KICAuctionItem>> armorItems = auctionData.getArmor(category);
                    if (armorItems == null || armorItems.isEmpty()) {
                        message.appendSibling(new ChatComponentText(String.format("§cNo §l%s§r found!\n\n", category.getDisplayText())));
                        continue;
                    }

                    message.appendSibling(new ChatComponentText(String.format("§6Cheapest §2%s\n", category.getDisplayText())));
                    armorItems.forEach((name, items) -> {
                        if (!items.isEmpty()) {
                            KICAuctionItem cheapest = items.get(0);
                            if (isArmorType(cheapest.getItemId())) {
                                message.appendSibling(createHoverAndClickComponent(true,
                                        String.format("§6- §9%s §6for §e%s\n", formatId(cheapest.getItemId()), parseToShorthandNumber(cheapest.getPrice())),
                                        "§7Click to view this auction", "/viewauction " + cheapest.getUuid()));
                            }
                        }
                    });
                    message.appendSibling(new ChatComponentText("\n"));
                }
            } else {
                for (ArmorCategory category : ArmorCategory.values()) {
                    List<KICAuctionItem> armor = auctionData.getArmor(category)
                            .entrySet().stream()
                            .filter(entry -> isArmorType(entry.getKey()))
                            .flatMap(entry -> entry.getValue().stream())
                            .sorted(Comparator.comparingLong(KICAuctionItem::getPrice))
                            .limit(5)
                            .collect(Collectors.toList());

                    if (armor.isEmpty()) {
                        message.appendSibling(new ChatComponentText(String.format("§cNo §l%s§r §cfound!\n\n", category.getDisplayText())));
                        continue;
                    }

                    message.appendSibling(new ChatComponentText(String.format("§6Cheapest §2%s\n", category.getDisplayText())));
                    for (KICAuctionItem item : armor) {
                        int attributeLvl = item.getAttribute1(true).toUpperCase().contains(attribute1.toUpperCase()) ? item.getAttributeLvl1() : item.getAttributeLvl2();
                        long baseLevelValue = item.getPrice() / (long) Math.pow(2, attributeLvl - 1);
                        long adjustedOutput = baseLevelValue * (long) Math.pow(2, 5 - 1);
                        message.appendSibling(createHoverAndClickComponent(true,
                                String.format("§6- §9%s §6for §e%s §7(§a%s§7/§alvl 5§7)\n", formatId(item.getItemId()), parseToShorthandNumber(item.getPrice()), parseToShorthandNumber(adjustedOutput)),
                                "§7Click to view this auction", "/viewauction " + item.getUuid()));
                    }
                    message.appendSibling(new ChatComponentText("\n"));
                }
            }
        } else if (currentCategory == 1) {
            for (EquipmentCategory category : EquipmentCategory.values()) {
                boolean categoryHasItems = false;
                IChatComponent categoryMessage = new ChatComponentText(String.format("§6Cheapest §2%s\n", category.getDisplayText()));

                for (EquipmentType type : EquipmentType.values()) {
                    List<KICAuctionItem> items = auctionData.getEquipment(category, type);
                    if (items == null || items.isEmpty()) continue;

                    KICAuctionItem cheapest = items.get(0);
                    if (hasAttribute2) {
                        message.appendSibling(createHoverAndClickComponent(true,
                                String.format("§6- §9%s §6for §e%s\n", formatId(cheapest.getItemId()), parseToShorthandNumber(cheapest.getPrice())),
                                "§7Click to view this auction", "/viewauction " + cheapest.getUuid()));
                    } else {
                        int attributeLvl = cheapest.getAttribute1(true).toUpperCase().contains(attribute1.toUpperCase()) ? cheapest.getAttributeLvl1() : cheapest.getAttributeLvl2();
                        long baseLevelValue = cheapest.getPrice() / (long) Math.pow(2, attributeLvl - 1);
                        long adjustedOutput = baseLevelValue * (long) Math.pow(2, 5 - 1);
                        categoryMessage.appendSibling(createHoverAndClickComponent(true,
                                String.format("§6- §9%s §6for §e%s §7(§a%s§7/§alvl 5§7)\n", formatId(cheapest.getItemId()), parseToShorthandNumber(cheapest.getPrice()), parseToShorthandNumber(adjustedOutput)),
                                "§7Click to view this auction", "/viewauction " + cheapest.getUuid()));
                    }

                    categoryHasItems = true;
                }

                if (categoryHasItems) {
                    message.appendSibling(categoryMessage);
                    message.appendSibling(new ChatComponentText("\n"));
                } else {
                    message.appendSibling(new ChatComponentText(String.format("§cNo §l%s§r §cfound!\n\n", category.getDisplayText())));
                }
            }
        } else if (currentCategory == 2) {
            List<KICAuctionItem> shards = auctionData.getShards();
            if (!shards.isEmpty() && !hasAttribute2) {
                message.appendSibling(new ChatComponentText("§6Cheapest §2Attribute Shards\n"));
                shards.forEach(shard -> {
                    long baseLevelValue = shard.getPrice() / (long) Math.pow(2, shard.getAttributeLvl1() - 1);
                    long adjustedOutput = baseLevelValue * (long) Math.pow(2, 5 - 1);
                    message.appendSibling(createHoverAndClickComponent(true,
                            String.format("§6- §9%s §6for §e%s §7(§a%s§7/§alvl 5§7)\n", formatId(shard.getItemId()), parseToShorthandNumber(shard.getPrice()), parseToShorthandNumber(adjustedOutput)),
                            "§7Click to view this auction", "/viewauction " + shard.getUuid()));
                });
                message.appendSibling(new ChatComponentText("\n"));
            } else {
                message.appendSibling(new ChatComponentText("§cNo §lShards§r §cfound!\n\n"));
            }
        } else if (currentCategory == 3) {
            List<KICAuctionItem> rods = auctionData.getRods();
            if (!rods.isEmpty()) {
                message.appendSibling(new ChatComponentText("§6Cheapest §2Rods\n"));
                rods.forEach(rod -> {
                    if (hasAttribute2) {
                        message.appendSibling(createHoverAndClickComponent(true,
                                String.format("§6- §9%s §6for §e%s\n", formatId(rod.getItemId()), parseToShorthandNumber(rod.getPrice())),
                                "§7Click to view this auction", "/viewauction " + rod.getUuid()));
                    } else {
                        int attributeLvl = rod.getAttribute1(true).toUpperCase().contains(attribute1.toUpperCase()) ? rod.getAttributeLvl1() : rod.getAttributeLvl2();
                        long baseLevelValue = rod.getPrice() / (long) Math.pow(2, attributeLvl - 1);
                        long adjustedOutput = baseLevelValue * (long) Math.pow(2, 5 - 1);
                        message.appendSibling(createHoverAndClickComponent(true,
                                String.format("§6- §9%s §6for §e%s §7(§a%s§7/§alvl 5§7)\n", formatId(rod.getItemId()), parseToShorthandNumber(rod.getPrice()), parseToShorthandNumber(adjustedOutput)),
                                "§7Click to view this auction", "/viewauction " + rod.getUuid()));
                    }
                });
                message.appendSibling(new ChatComponentText("\n"));
            } else {
                message.appendSibling(new ChatComponentText("§cNo §lRods§r §cfound!\n\n"));
            }
        }

        message.appendSibling(createHoverAndClickComponent(true, String.format("§6Change category: §7[ §a§l%s§r §7]\n",
                        currentCategory == 0 ? "Armor" : currentCategory == 1 ? "Equipment" : currentCategory == 2 ? "Shard" : "Rod"),
                String.format("§6Categories:\n\n%sArmor\n%sEquipment\n%sShard\n%sRod",
                        currentCategory == 0 ? "§a> " : "§6",
                        currentCategory == 1 ? "§a> " : "§6",
                        currentCategory == 2 ? "§a> " : "§6",
                        currentCategory == 3 ? "§a> " : "§6"),
                "/intkiccmd " + currentCategory));

        InternalCommand.setAction(AttributePrice::changeCategory);

        message.appendSibling(createHoverAndClickComponent(true, "§6Use §b§l/ka§r §6to check more auction listings!\n",
                "§7Click to open the KIC Auction GUI.", kaCmd));

        AttributePrice.message = message;
    }

    private static void changeCategory(int currentCategory) {
        AttributePrice.currentCategory = (currentCategory + 1) % 4;
        processData();
        sendMessage();
    }

    private static void sendMessage() {
        if (message != null) {
            sendOrReplaceKicMessage(message);
        }
    }

    private static void sendOrReplaceKicMessage(IChatComponent message) {
        chatGui.deleteChatLine(CHAT_ID_AP_MESSAGE);
        chatGui.printChatMessageWithOptionalDeletion(message, CHAT_ID_AP_MESSAGE);
    }
}
