package someoneok.kic.models.crimson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import someoneok.kic.models.kicauction.KICAuctionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeData {
    private final String item;
    private final String attribute;
    private final long totalCost;
    private final int startLevel;
    private final int endLevel;
    private final long timestamp;
    private final Map<Integer, List<KICAuctionItem>> levels;

    public UpgradeData(String item, String attribute, long totalCost, int startLevel, int endLevel, long timestamp, JsonObject levelsData) {
        this.item = item;
        this.attribute = attribute;
        this.totalCost = totalCost;
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        this.timestamp = timestamp;
        this.levels = new HashMap<>();
        parseLevels(levelsData);
    }

    private void parseLevels(JsonObject levelsData) {
        for (int level = startLevel + 1; level <= endLevel; level++) {
            String levelKey = String.valueOf(level);
            if (!levelsData.has(levelKey)) continue;

            JsonArray auctionsArray = levelsData.getAsJsonArray(levelKey);
            if (auctionsArray.size() == 0) continue;

            List<KICAuctionItem> itemsAtLevel = new ArrayList<>();

            for (JsonElement element : auctionsArray) {
                JsonObject itemData = element.getAsJsonObject();

                String uuid = itemData.get("uuid").getAsString();
                String itemId = itemData.get("itemId").getAsString();
                long price = itemData.get("price").getAsLong();

                String attribute1 = itemData.has("attribute1") && !itemData.get("attribute1").isJsonNull()
                        ? itemData.get("attribute1").getAsString() : null;
                Integer attributeLvl1 = itemData.has("attributeLvl1") && !itemData.get("attributeLvl1").isJsonNull()
                        ? itemData.get("attributeLvl1").getAsInt() : null;

                String attribute2 = itemData.has("attribute2") && !itemData.get("attribute2").isJsonNull()
                        ? itemData.get("attribute2").getAsString() : null;
                Integer attributeLvl2 = itemData.has("attributeLvl2") && !itemData.get("attributeLvl2").isJsonNull()
                        ? itemData.get("attributeLvl2").getAsInt() : null;

                KICAuctionItem auctionItem = new KICAuctionItem(uuid, itemId, price, attribute1, attributeLvl1, attribute2, attributeLvl2);
                itemsAtLevel.add(auctionItem);
            }

            levels.put(level, itemsAtLevel);
        }
    }

    public String getItem() {
        return item;
    }

    public String getAttribute() {
        return attribute;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public int getEndLevel() {
        return endLevel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<Integer, List<KICAuctionItem>> getLevels() {
        return levels;
    }
}
