package someoneok.kic.models.kicauction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;

public class AuctionData {
    private final String attribute1;
    private final int attributeLvl1;
    private final String attribute2;
    private final int attributeLvl2;
    private final long timestamp;
    private final List<KICAuctionItem> shards;
    private final Map<ArmorCategory, Map<String, List<KICAuctionItem>>> armor;
    private final Map<EquipmentCategory, Map<EquipmentType, List<KICAuctionItem>>> equipment;
    private final List<KICAuctionItem> rods;

    public AuctionData(String attribute1, int attributeLvl1, String attribute2, int attributeLvl2, long timestamp, JsonObject armorData, JsonObject equipmentData, JsonArray shardsData, JsonArray rodsData) {
        this.attribute1 = attribute1;
        this.attributeLvl1 = attributeLvl1;
        this.attribute2 = attribute2;
        this.attributeLvl2 = attributeLvl2;
        this.timestamp = timestamp;

        this.armor = new HashMap<>();
        parseArmor(armorData);

        this.equipment = new HashMap<>();
        parseEquipment(equipmentData);

        this.shards = new ArrayList<>();
        parseShards(shardsData);

        this.rods = new ArrayList<>();
        parseRods(rodsData);
    }

    public String getAttribute1() {
        return attribute1;
    }

    public int getAttributeLvl1() {
        return attributeLvl1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public int getAttributeLvl2() {
        return attributeLvl2;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, List<KICAuctionItem>> getArmor(ArmorCategory category) {
        return armor.get(category);
    }

    public List<KICAuctionItem> getEquipment(EquipmentCategory category, EquipmentType type) {
        return equipment.get(category).get(type);
    }

    public List<KICAuctionItem> getShards() {
        return shards;
    }

    public List<KICAuctionItem> getRods() {
        return rods;
    }

    private void parseArmor(JsonObject armorData) {
        if (armorData == null || armorData.isJsonNull() || armorData.entrySet().isEmpty()) {
            return;
        }

        for (Map.Entry<String, JsonElement> categoryEntry : armorData.entrySet()) {
            Optional<ArmorCategory> category = ArmorCategory.getFromName(categoryEntry.getKey());
            if (!category.isPresent()) continue;
            JsonObject categoryObject = categoryEntry.getValue().getAsJsonObject();

            Map<String, List<KICAuctionItem>> itemsMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> itemEntry : categoryObject.entrySet()) {
                String itemName = itemEntry.getKey();
                JsonArray itemArray = itemEntry.getValue().getAsJsonArray();

                List<KICAuctionItem> auctionItems = new ArrayList<>();

                for (JsonElement itemElement : itemArray) {
                    if (!itemElement.isJsonObject()) continue;

                    try {
                        JsonObject itemObject = itemElement.getAsJsonObject();
                        String uuid = itemObject.has("uuid") ? itemObject.get("uuid").getAsString() : "";
                        String itemId = itemObject.has("itemId") ? itemObject.get("itemId").getAsString() : "";
                        long price = itemObject.has("price") ? itemObject.get("price").getAsLong() : 0L;

                        String attribute1 = itemObject.has("attribute1") && !itemObject.get("attribute1").isJsonNull() ? itemObject.get("attribute1").getAsString() : "";
                        Integer attributeLvl1 = itemObject.has("attributeLvl1") && !itemObject.get("attributeLvl1").isJsonNull() ? itemObject.get("attributeLvl1").getAsInt() : 0;
                        String attribute2 = itemObject.has("attribute2") && !itemObject.get("attribute2").isJsonNull() ? itemObject.get("attribute2").getAsString() : "";
                        Integer attributeLvl2 = itemObject.has("attributeLvl2") && !itemObject.get("attributeLvl2").isJsonNull() ? itemObject.get("attributeLvl2").getAsInt() : 0;

                        List<String> lore = new ArrayList<>();
                        String modifier = null;
                        Integer upgradeLevel = null;

                        if (itemObject.has("extra") && itemObject.get("extra").isJsonObject()) {
                            JsonObject extra = itemObject.getAsJsonObject("extra");

                            if (extra.has("lore") && extra.get("lore").isJsonArray()) {
                                JsonArray jsonLore = extra.getAsJsonArray("lore");
                                for (JsonElement loreElement : jsonLore) {
                                    if (loreElement.isJsonPrimitive()) {
                                        lore.add(loreElement.getAsString());
                                    }
                                }
                            }

                            if (extra.has("extraAttributes") && extra.get("extraAttributes").isJsonObject()) {
                                JsonObject extraAttributes = extra.getAsJsonObject("extraAttributes");

                                if (extraAttributes.has("modifier")) {
                                    modifier = extraAttributes.get("modifier").getAsString();
                                }

                                if (extraAttributes.has("rarity_upgrades")) {
                                    try {
                                        upgradeLevel = Integer.parseInt(extraAttributes.get("rarity_upgrades").getAsString());
                                    } catch (NumberFormatException ignored) {}
                                }
                            }
                        }

                        KICAuctionItem auctionItem = new KICAuctionItem(uuid, itemId, price, attribute1, attributeLvl1, attribute2, attributeLvl2, lore, modifier, upgradeLevel);
                        auctionItems.add(auctionItem);

                    } catch (JsonSyntaxException | IllegalStateException e) {
                        KICLogger.error("Error parsing armor item: " + e.getMessage());
                    }
                }

                itemsMap.put(itemName, auctionItems);
            }

            armor.put(category.get(), itemsMap);
        }
    }

    private void parseEquipment(JsonObject equipmentData) {
        if (equipmentData == null || equipmentData.isJsonNull() || equipmentData.entrySet().isEmpty()) {
            return;
        }

        for (Map.Entry<String, JsonElement> categoryEntry : equipmentData.entrySet()) {
            Optional<EquipmentCategory> category = EquipmentCategory.getFromName(categoryEntry.getKey());
            if (!category.isPresent()) continue;
            JsonObject categoryObject = categoryEntry.getValue().getAsJsonObject();

            Map<EquipmentType, List<KICAuctionItem>> itemsMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> itemEntry : categoryObject.entrySet()) {
                Optional<EquipmentType> type = EquipmentType.getFromId(itemEntry.getKey());
                if (!type.isPresent()) continue;
                JsonArray itemArray = itemEntry.getValue().getAsJsonArray();
                List<KICAuctionItem> auctionItems = new ArrayList<>();

                for (JsonElement itemElement : itemArray) {
                    if (!itemElement.isJsonObject()) continue;

                    try {
                        JsonObject itemObject = itemElement.getAsJsonObject();
                        String uuid = itemObject.has("uuid") ? itemObject.get("uuid").getAsString() : "";
                        String itemId = itemObject.has("itemId") ? itemObject.get("itemId").getAsString() : "";
                        long price = itemObject.has("price") ? itemObject.get("price").getAsLong() : 0L;

                        String attribute1 = itemObject.has("attribute1") && !itemObject.get("attribute1").isJsonNull() ? itemObject.get("attribute1").getAsString() : "";
                        Integer attributeLvl1 = itemObject.has("attributeLvl1") && !itemObject.get("attributeLvl1").isJsonNull() ? itemObject.get("attributeLvl1").getAsInt() : 0;
                        String attribute2 = itemObject.has("attribute2") && !itemObject.get("attribute2").isJsonNull() ? itemObject.get("attribute2").getAsString() : "";
                        Integer attributeLvl2 = itemObject.has("attributeLvl2") && !itemObject.get("attributeLvl2").isJsonNull() ? itemObject.get("attributeLvl2").getAsInt() : 0;

                        List<String> lore = new ArrayList<>();
                        String modifier = null;
                        Integer upgradeLevel = null;

                        if (itemObject.has("extra") && itemObject.get("extra").isJsonObject()) {
                            JsonObject extra = itemObject.getAsJsonObject("extra");

                            if (extra.has("lore") && extra.get("lore").isJsonArray()) {
                                JsonArray jsonLore = extra.getAsJsonArray("lore");
                                for (JsonElement loreElement : jsonLore) {
                                    if (loreElement.isJsonPrimitive()) {
                                        lore.add(loreElement.getAsString());
                                    }
                                }
                            }

                            if (extra.has("extraAttributes") && extra.get("extraAttributes").isJsonObject()) {
                                JsonObject extraAttributes = extra.getAsJsonObject("extraAttributes");

                                if (extraAttributes.has("modifier")) {
                                    modifier = extraAttributes.get("modifier").getAsString();
                                }

                                if (extraAttributes.has("rarity_upgrades")) {
                                    try {
                                        upgradeLevel = Integer.parseInt(extraAttributes.get("rarity_upgrades").getAsString());
                                    } catch (NumberFormatException ignored) {}
                                }
                            }
                        }

                        KICAuctionItem auctionItem = new KICAuctionItem(uuid, itemId, price, attribute1, attributeLvl1, attribute2, attributeLvl2, lore, modifier, upgradeLevel);
                        auctionItems.add(auctionItem);

                    } catch (JsonSyntaxException | IllegalStateException e) {
                        KICLogger.error("Error parsing equipment item: " + e.getMessage());
                    }
                }

                itemsMap.put(type.get(), auctionItems);
            }

            equipment.put(category.get(), itemsMap);
        }
    }

    private void parseShards(JsonArray shardsData) {
        if (shardsData == null || shardsData.isJsonNull() || shardsData.size() == 0) {
            return;
        }

        shardsData.forEach(shardElement -> {
            if (shardElement == null || shardElement.isJsonNull() || !shardElement.isJsonObject()) {
                return;
            }

            try {
                JsonObject shardObject = shardElement.getAsJsonObject();
                if (!shardObject.has("uuid") || !shardObject.has("itemId") || !shardObject.has("price")) {
                    return;
                }

                String uuid = shardObject.get("uuid").getAsString();
                String itemId = shardObject.get("itemId").getAsString();
                long price = shardObject.get("price").getAsLong();

                String attribute1 = shardObject.has("attribute1") && !shardObject.get("attribute1").isJsonNull() ? shardObject.get("attribute1").getAsString() : "";
                Integer attributeLvl1 = shardObject.has("attributeLvl1") && !shardObject.get("attributeLvl1").isJsonNull() ? shardObject.get("attributeLvl1").getAsInt() : 0;

                List<String> lore = new ArrayList<>();
                if (shardObject.has("extra") && shardObject.get("extra").isJsonObject()) {
                    JsonObject extra = shardObject.getAsJsonObject("extra");
                    if (extra.has("lore") && extra.get("lore").isJsonArray()) {
                        JsonArray jsonLore = extra.getAsJsonArray("lore");
                        for (JsonElement loreElement : jsonLore) {
                            if (loreElement.isJsonPrimitive()) {
                                lore.add(loreElement.getAsString());
                            }
                        }
                    }
                }

                KICAuctionItem shard = new KICAuctionItem(uuid, itemId, price, attribute1, attributeLvl1, null, null, lore);
                shards.add(shard);
            } catch (JsonSyntaxException | IllegalStateException e) {
                KICLogger.error("Error parsing shard data: " + e.getMessage());
            }
        });
    }

    private void parseRods(JsonArray rodsData) {
        if (rodsData == null || rodsData.isJsonNull() || rodsData.size() == 0) {
            return;
        }

        rodsData.forEach(rodElement -> {
            if (rodElement == null || rodElement.isJsonNull() || !rodElement.isJsonObject()) {
                return;
            }

            try {
                JsonObject rodObject = rodElement.getAsJsonObject();
                if (!rodObject.has("uuid") || !rodObject.has("itemId") || !rodObject.has("price")) {
                    return;
                }

                String uuid = rodObject.get("uuid").getAsString();
                String itemId = rodObject.get("itemId").getAsString();
                long price = rodObject.get("price").getAsLong();

                String attribute1 = rodObject.has("attribute1") && !rodObject.get("attribute1").isJsonNull() ? rodObject.get("attribute1").getAsString() : "";
                Integer attributeLvl1 = rodObject.has("attributeLvl1") && !rodObject.get("attributeLvl1").isJsonNull() ? rodObject.get("attributeLvl1").getAsInt() : 0;
                String attribute2 = rodObject.has("attribute2") && !rodObject.get("attribute2").isJsonNull() ? rodObject.get("attribute2").getAsString() : "";
                Integer attributeLvl2 = rodObject.has("attributeLvl2") && !rodObject.get("attributeLvl2").isJsonNull() ? rodObject.get("attributeLvl2").getAsInt() : 0;

                List<String> lore = new ArrayList<>();
                if (rodObject.has("extra") && rodObject.get("extra").isJsonObject()) {
                    JsonObject extra = rodObject.getAsJsonObject("extra");
                    if (extra.has("lore") && extra.get("lore").isJsonArray()) {
                        JsonArray jsonLore = extra.getAsJsonArray("lore");
                        for (JsonElement loreElement : jsonLore) {
                            if (loreElement.isJsonPrimitive()) {
                                lore.add(loreElement.getAsString());
                            }
                        }
                    }
                }

                KICAuctionItem rod = new KICAuctionItem(uuid, itemId, price, attribute1, attributeLvl1, attribute2, attributeLvl2, lore);
                rods.add(rod);
            } catch (JsonSyntaxException | IllegalStateException e) {
                KICLogger.error("Error parsing shard data: " + e.getMessage());
            }
        });
    }
}
