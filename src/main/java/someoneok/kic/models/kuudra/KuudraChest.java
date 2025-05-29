package someoneok.kic.models.kuudra;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.config.pages.KuudraRerollFailsafeOptions;
import someoneok.kic.models.APIException;
import someoneok.kic.models.HypixelRarity;
import someoneok.kic.models.crimson.*;
import someoneok.kic.models.request.Request;
import someoneok.kic.modules.kuudra.GodRoll;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.CacheManager;
import someoneok.kic.utils.ItemUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public enum KuudraChest {
    FREE("Free Chest"),
    PAID("Paid Chest");

    private final List<String> bazaarItemIds = Arrays.asList("KUUDRA_TEETH", "MANDRAA", "KUUDRA_MANDIBLE", "ESSENCE_CRIMSON", "KISMET_FEATHER");
    private final List<String> valuables = Arrays.asList("WHEEL_OF_FATE", "BURNING_KUUDRA_CORE", "ENRAGER", "TENTACLE_DYE", "ULTIMATE_FATAL_TEMPO", "ULTIMATE_INFERNO");
    private final String displayText;
    private KuudraKey keyNeeded;
    private int essence;
    private final Map<String, Value> values = new HashMap<>();
    private BazaarItemValue essenceValue;
    private BazaarItemValue kismetValue;
    private long totalValue;
    private AttributeItemValue godRollItem;
    private boolean hasValuables;

    KuudraChest(String displayText) {
        this.displayText = displayText;
        this.keyNeeded = null;
        this.essence = 0;
        this.totalValue = 0;
        this.essenceValue = new BazaarItemValue(new BazaarItem("ESSENCE_CRIMSON", "§dCrimson Essence"));
        this.kismetValue = new BazaarItemValue(new BazaarItem("KISMET_FEATHER", "§9Kismet Feather"));
        this.godRollItem = null;
        this.hasValuables = false;
    }

    public void reset() {
        this.keyNeeded = null;
        this.essence = 0;
        this.totalValue = 0;
        this.values.clear();
        this.essenceValue = new BazaarItemValue(new BazaarItem("ESSENCE_CRIMSON", "§dCrimson Essence"));
        this.kismetValue = new BazaarItemValue(new BazaarItem("KISMET_FEATHER", "§9Kismet Feather"));
        this.godRollItem = null;
        this.hasValuables = false;
    }

    public String getDisplayText() {
        return displayText;
    }

    public KuudraKey getKeyNeeded() {
        return keyNeeded;
    }

    public void setKeyNeeded(KuudraKey keyNeeded) {
        this.keyNeeded = keyNeeded;
    }

    public boolean hasItems() {
        return !values.isEmpty();
    }

    public void addItem(ItemStack item) {
        this.processItem(item);
    }

    public int getTeeth() {
        Value value = values.get("KUUDRA_TEETH");
        if (value instanceof BazaarItemValue) {
            return ((BazaarItemValue) value).getItemCount();
        }
        return 0;
    }

    public int getEssence() {
        if (KuudraProfitCalculatorOptions.ignoreEssence) {
            return 0;
        }

        int baseEssence = this.essence;

        if (KuudraProfitCalculatorOptions.includeKuudraPetPerk) {
            int kuudraPetRarity = KuudraProfitCalculatorOptions.kuudraPetRarity;
            int kuudraPetLevel = KuudraProfitCalculatorOptions.kuudraPetLevel;

            if (kuudraPetRarity != 0 && kuudraPetLevel != 0) {
                HypixelRarity petRarity = HypixelRarity.values()[kuudraPetRarity];
                double boostPercentage = getKuudraPetEssenceBoost(petRarity, kuudraPetLevel);

                baseEssence += (int) Math.round(baseEssence * (boostPercentage / 100.0));
            }
        }

        return baseEssence;
    }

    public int getRawEssence() {
        return this.essence;
    }

    public void setEssence(int essence) {
        this.essence = essence;
    }

    public static Optional<KuudraChest> getFromName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        for (KuudraChest chest : values()) {
            if (chest.displayText.equalsIgnoreCase(name)) {
                return Optional.of(chest);
            }
        }
        return Optional.empty();
    }

    private void processItem(ItemStack itemStack) {
        String type = getType(itemStack);
        KICLogger.info(String.format("Item: %s | ItemID: %s | Type: %s", itemStack.getDisplayName(), ItemUtils.getItemId(itemStack), type));
        if (type == null) return;
        switch (type) {
            case "ATTRIBUTES":
                String uuid = ItemUtils.getItemUuid(itemStack);
                AttributeItem attributeItem = ItemUtils.mapToAttributeItem(itemStack);
                if (attributeItem == null) return;
                if (valuables.contains(attributeItem.getItemId()) && !hasValuables) hasValuables = true;
                if (KuudraProfitCalculatorOptions.forceT5Attribute) {
                    Attributes attributes = attributeItem.getAttributes();
                    if (KuudraProfitCalculatorOptions.forceT5AttributeOnlyLB) {
                        if (KuudraProfitCalculatorOptions.attributePriceType == 0) {
                            if (attributes.getLevel1() > 5) attributes.setLevel1(5);
                            if (attributes.getLevel2() > 5) attributes.setLevel2(5);
                        }
                    } else {
                        if (attributes.getLevel1() > 5) attributes.setLevel1(5);
                        if (attributes.getLevel2() > 5) attributes.setLevel2(5);
                    }
                }
                values.put(uuid, new AttributeItemValue(attributeItem));
                break;
            case "BAZAAR-ENCHANTS":
                String[] enchant = ItemUtils.getFirstEnchant(itemStack);
                String enchantId = enchant[0];
                String enchantName = enchant[1];
                if (isNullOrEmpty(enchantId) || isNullOrEmpty(enchantName)) return;
                BazaarItem bazaarItem = new BazaarItem(enchantId, enchantName);
                if (valuables.contains(bazaarItem.getItemId()) && !hasValuables) hasValuables = true;
                values.put(enchantId, new BazaarItemValue(bazaarItem));
                break;
            case "BAZAAR":
                String bazaarId = ItemUtils.getItemId(itemStack);
                if (isNullOrEmpty(bazaarId)) return;
                String name1 = itemStack.getDisplayName() == null ? "" : itemStack.getDisplayName();
                BazaarItem bazaarItem2 = new BazaarItem(bazaarId, name1);
                if (valuables.contains(bazaarItem2.getItemId()) && !hasValuables) hasValuables = true;
                int count = itemStack.stackSize;
                if (count > 0) {
                    bazaarItem2.setCount(count);
                }
                values.put(bazaarId, new BazaarItemValue(bazaarItem2));
                break;
            case "AUCTION":
                String auctionId = ItemUtils.getItemId(itemStack);
                String auctionUuid = ItemUtils.getItemUuid(itemStack);
                String name2 = itemStack.getDisplayName() == null ? "" : itemStack.getDisplayName();
                if (isNullOrEmpty(auctionId) || isNullOrEmpty(auctionUuid)) return;
                AuctionItem auctionItem = new AuctionItem(auctionId, name2, auctionUuid);
                if (valuables.contains(auctionItem.getItemId()) && !hasValuables) hasValuables = true;
                values.put(auctionUuid, new AuctionItemValue(auctionItem));
                break;
        }
    }

    private String getType(ItemStack item) {
        if (!ItemUtils.hasItemId(item)) return null;
        if (ItemUtils.hasAttributes(item)) {
            return "ATTRIBUTES";
        } else if (ItemUtils.hasEnchants(item)) {
            return "BAZAAR-ENCHANTS";
        } else {
            String itemId = ItemUtils.getItemId(item);
            if (itemId != null) {
                if (bazaarItemIds.contains(itemId)) {
                    return "BAZAAR";
                } else {
                    return "AUCTION";
                }
            } else {
                return null;
            }
        }
    }

    public void updateValues(Runnable callback) {
        if (!ApiUtils.isVerified()) return;
        KICLogger.info("Updating values");

        List<Value> itemsToFetch = values.values().stream()
                .filter(item -> !item.isFetching() && !item.isCached())
                .collect(Collectors.toList());

        if (!essenceValue.isFetching() && !essenceValue.isCached()) itemsToFetch.add(essenceValue);
        if (!kismetValue.isFetching() && !kismetValue.isCached()) itemsToFetch.add(kismetValue);

        if (itemsToFetch.isEmpty()) {
            updateTotalValue();
            if (callback != null) {
                callback.run();
            }
            return;
        }

        KICLogger.info("Updating values from api");

        List<Request> requestItems = itemsToFetch.stream()
                .map(Value::mapToRequest)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (keyNeeded != null) {
            requestItems.add(keyNeeded.getRequest());
        }

        itemsToFetch.forEach(item -> item.setFetching(true));

        String requestBody = KIC.GSON.toJson(requestItems);

        KICLogger.info("Request body: " + requestBody);

        Multithreading.runAsync(() -> {
            try {
                JsonArray response = JsonUtils.parseString(NetworkUtils.sendPostRequest("https://api.sm0kez.com/crimson/prices", true, requestBody)).getAsJsonArray();
                if (response != null) {
                    for (JsonElement element : response) {
                        JsonObject obj = element.getAsJsonObject();
                        String type = obj.get("type").getAsString();
                        switch (type) {
                            case "ATTRIBUTES":
                                String uuid1 = obj.get("uuid").getAsString();
                                Value v1 = values.get(uuid1);
                                if (!(v1 instanceof AttributeItemValue)) continue;
                                AttributeItemValue attributeItemValue = (AttributeItemValue) v1;
                                attributeItemValue.setFetching(false);
                                attributeItemValue.setCached(true);
                                attributeItemValue.setItemPrice(obj.get("price").getAsLong());

                                Attributes attributes = attributeItemValue.getAttributes();
                                attributes.setLbPrice1(obj.get("priceAttribute1").getAsLong());
                                attributes.setAvgPrice1(obj.get("averagePriceAttribute1").getAsLong());
                                if (obj.has("priceAttribute2") && !obj.get("priceAttribute2").isJsonNull()) {
                                    attributes.setLbPrice2(obj.get("priceAttribute2").getAsLong());
                                    attributes.setAvgPrice2(obj.get("averagePriceAttribute2").getAsLong());
                                }
                                boolean godRoll = obj.get("godRoll").getAsBoolean();
                                attributes.setGodroll(godRoll);
                                if (obj.has("godRollPrice") && !obj.get("godRollPrice").isJsonNull()) {
                                    attributes.setGodrollLbPrice(obj.get("godRollPrice").getAsLong());
                                    attributes.setGodrollAvgPrice(obj.get("averageGodRollPrice").getAsLong());
                                }
                                if (godRoll) {
                                    godRollItem = attributeItemValue;
                                    GodRoll.show(attributeItemValue);
                                }
                                break;
                            case "BAZAAR":
                                String itemId = obj.get("itemId").getAsString();
                                Value v2;
                                if ("ESSENCE_CRIMSON".equals(itemId)) {
                                    v2 = essenceValue;
                                } else if ("KISMET_FEATHER".equals(itemId)) {
                                    v2 = kismetValue;
                                } else {
                                    v2 = values.get(itemId);
                                }
                                if (!(v2 instanceof BazaarItemValue)) continue;
                                BazaarItemValue bazaarItemValue = (BazaarItemValue) v2;
                                long buyPriceBazaar = obj.get("buyPrice").getAsLong();
                                long sellPriceBazaar = obj.get("sellPrice").getAsLong();
                                bazaarItemValue.setPrice(buyPriceBazaar, sellPriceBazaar);
                                bazaarItemValue.setFetching(false);
                                bazaarItemValue.setCached(true);
                                if ("ESSENCE_CRIMSON".equals(itemId)) {
                                    KuudraProfitTracker.updateEssencePrice(bazaarItemValue.getSingleValue());
                                } else if ("KISMET_FEATHER".equals(itemId)) {
                                    KuudraProfitTracker.updateKismetPrice(bazaarItemValue.getSingleValue());
                                } else if ("KUUDRA_TEETH".equals(itemId)) {
                                    KuudraProfitTracker.updateTeethPrice(bazaarItemValue.getSingleValue());
                                }
                            case "AUCTION":
                                String uuid2 = obj.get("uuid").getAsString();
                                Value v3 = values.get(uuid2);
                                if (!(v3 instanceof AuctionItemValue)) continue;
                                AuctionItemValue auctionItemValue = (AuctionItemValue) v3;
                                long priceAuction = obj.get("price").getAsLong();
                                long avgAuction = obj.get("averagePrice").getAsLong();
                                auctionItemValue.setPrice(priceAuction, avgAuction);
                                auctionItemValue.setFetching(false);
                                auctionItemValue.setCached(true);
                                break;
                            case "KEY":
                                if (keyNeeded != null) {
                                    long buyPriceKey = obj.get("buyPrice").getAsLong();
                                    long sellPriceKey = obj.get("sellPrice").getAsLong();
                                    keyNeeded.setPrice(buyPriceKey, sellPriceKey);
                                    KuudraProfitTracker.updateKeyPrice(keyNeeded);
                                }
                                break;
                        }
                    }
                    updateTotalValue();
                    addItemsToCache();
                }
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                itemsToFetch.forEach(item -> {
                    item.setFetching(false);
                    item.setCached(false);
                });
            }

            if (callback != null) {
                callback.run();
            }
        });
    }

    public Map<String, Value> getValues() {
        return values;
    }

    public BazaarItemValue getEssenceValue() {
        return essenceValue;
    }

    public BazaarItemValue getKismetValue() {
        return kismetValue;
    }

    private void updateTotalValue() {
        this.totalValue = 0;

        this.totalValue += getEssence() * essenceValue.getValue();

        values.values().forEach(value -> this.totalValue += getItemValue(value));

        if (keyNeeded != null) {
            this.totalValue -= keyNeeded.getPrice();
        }
    }

    public long getTotalValue(boolean rerolled) {
        if (rerolled) {
            return this.totalValue - kismetValue.getValue();
        }
        return this.totalValue;
    }

    public boolean hasGodRoll() {
        return godRollItem != null;
    }

    // Format uuid;itemId;attribute1;attribute1lvl;attribute2;attribute2lvl;lbPrice;avgPrice
    public String getGodRoll() {
        if (godRollItem == null) return null;
        Attributes attributes = godRollItem.getAttributes();
        return godRollItem.getUuid() + ";" +
                godRollItem.getItemId() + ";" +
                attributes.getName1() + ";" +
                attributes.getLevel1() + ";" +
                attributes.getName2() + ";" +
                attributes.getLevel2() + ";" +
                attributes.getGodrollLbPrice() + ";" +
                attributes.getGodrollAvgPrice();
    }

    private double getKuudraPetEssenceBoost(HypixelRarity rarity, int level) {
        if (level < 1) return 0;

        switch (rarity) {
            case COMMON:
                return level * 0.1;
            case UNCOMMON:
            case RARE:
                return level * 0.15;
            case EPIC:
            case LEGENDARY:
                return level * 0.2;
            default:
                return 0;
        }
    }

    public boolean shouldReroll() {
        KICLogger.info("Checking shouldReroll for chest: " + this.getDisplayText());

        if (this == KuudraChest.FREE) {
            KICLogger.info("Skipping reroll: Chest is FREE.");
            return false;
        }

        if (KICConfig.ACOnlyRerollInT5 && (keyNeeded == null || keyNeeded != KuudraKey.INFERNAL)) {
            KICLogger.info("Skipping reroll: onlyRerollInT5 is enabled and keyNeeded is not INFERNAL.");
            return false;
        }

        if (hasGodRoll()) {
            KICLogger.info("Skipping reroll: Found godroll");
            return false;
        }

        if (hasValuables) {
            KICLogger.info("Skipping reroll: Item id found in valuables");
            return false;
        }

        Set<String> failsafeAttributes = KuudraRerollFailsafeOptions.getEnabled();
        KICLogger.info("Enabled failsafe attributes: " + failsafeAttributes);

        for (Value value : values.values()) {
            if (value instanceof AttributeItemValue) {
                AttributeItemValue attrItem = (AttributeItemValue) value;
                Attributes attributes = attrItem.getAttributes();

                if (attributes.hasAttribute1() && failsafeAttributes.contains(attributes.getName1().toLowerCase())) {
                    KICLogger.info("Skipping reroll: Found failsafe attribute in slot 1: " + attributes.getName1());
                    return false;
                }
                if (attributes.hasAttribute2() && failsafeAttributes.contains(attributes.getName2().toLowerCase())) {
                    KICLogger.info("Skipping reroll: Found failsafe attribute in slot 2: " + attributes.getName2());
                    return false;
                }
            }
        }

        AtomicLong combinedValue = new AtomicLong(0);
        StringBuilder sb = new StringBuilder("Combined items: ");
        values.values().stream()
                .filter(value -> !(value instanceof BazaarItemValue && "KUUDRA_TEETH".equals(((BazaarItemValue) value).getItemId())))
                .forEach(value -> {
                    long itemValue = getItemValue(value);
                    combinedValue.addAndGet(itemValue);
                    sb.append(getItemId(value)).append(": ").append(itemValue).append(" | ");
                });
        long finalValue = combinedValue.get();

        KICLogger.info(sb.toString());
        KICLogger.info("Combined chest value: " + finalValue);
        KICLogger.info("Kismet Feather cost: " + kismetValue.getValue());
        KICLogger.info("Auto Reroll Minimum Value: " + KICConfig.ACAutoRerollMinValue);

        // Determine reroll decision
        boolean shouldReroll;
        if (KICConfig.ACShouldRerollType == 0) {
            shouldReroll = finalValue < kismetValue.getValue();
            KICLogger.info("Using Method 1 (Kismet Comparison). Should Reroll: " + shouldReroll);
        } else {
            shouldReroll = finalValue < KICConfig.ACAutoRerollMinValue;
            KICLogger.info("Using Method 2 (Min Combined Value Threshold). Should Reroll: " + shouldReroll);
        }

        return shouldReroll;
    }

    public boolean shouldBuy() {
        KICLogger.info("Checking shouldBuy for chest: " + this.getDisplayText());

        if (this == KuudraChest.FREE) {
            KICLogger.info("Skipping buy: Chest is FREE.");
            return false;
        }

        if (KICConfig.ACAlwaysAutoBuy) {
            KICLogger.info("Auto Buy enabled: Buying chest regardless of value.");
            return true;
        }

        if (hasValuables) {
            KICLogger.info("Should buy: Chest contains a valuable item");
            return true;
        }

        if (hasGodRoll()) {
            KICLogger.info("Should buy: Chest contains a god roll");
            return true;
        }

        KICLogger.info("Total Chest Value: " + this.totalValue);
        KICLogger.info("Auto Buy Minimum Profit: " + KICConfig.ACAutoBuyMinProfit);

        boolean shouldBuy = this.totalValue >= KICConfig.ACAutoBuyMinProfit;
        KICLogger.info("Should Buy: " + shouldBuy);
        return shouldBuy;
    }

    private String getItemId(Value value) {
        if (value instanceof BazaarItemValue) {
            return ((BazaarItemValue) value).getItemId();
        } else if (value instanceof AuctionItemValue) {
            return ((AuctionItemValue) value).getItemId();
        } else if (value instanceof AttributeItemValue) {
            return ((AttributeItemValue) value).getItemId();
        }
        return null;
    }

    private long getItemValue(Value value) {
        if (value instanceof BazaarItemValue) {
            return ((BazaarItemValue) value).getValue();
        } else if (value instanceof AuctionItemValue) {
            return ((AuctionItemValue) value).getValue();
        } else if (value instanceof AttributeItemValue) {
            return ((AttributeItemValue) value).getValue(this.essenceValue);
        }
        return 0;
    }

    private void addItemsToCache() {
        values.values().stream()
                .filter(AttributeItemValue.class::isInstance)
                .map(AttributeItemValue.class::cast)
                .filter(AttributeItemValue::isCached)
                .forEach(CacheManager::addItem);
    }
}
