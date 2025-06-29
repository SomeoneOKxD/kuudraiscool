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
import someoneok.kic.modules.kuudra.Hologram;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.ItemUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.ItemUtils.getItemStars;
import static someoneok.kic.utils.ItemUtils.isArmorPiece;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public enum KuudraChest {
    FREE("Free Chest"),
    PAID("Paid Chest");

    private static final int[] ESSENCE_TOTALS = {30, 65, 105, 150, 200, 255, 315, 385, 465, 555};
    private static final int[] HEAVY_PEARL_TOTALS = {0, 0, 0, 0, 0, 0, 0, 2, 5, 9};
    private final List<String> bazaarItemIds = Arrays.asList("KUUDRA_TEETH", "MANDRAA", "KUUDRA_MANDIBLE", "ESSENCE_CRIMSON", "KISMET_FEATHER", "HEAVY_PEARL");
    private final List<String> valuables = Arrays.asList("WHEEL_OF_FATE", "BURNING_KUUDRA_CORE", "TORMENTOR", "HELLSTORM_STAFF", "TENTACLE_DYE", "ENCHANTMENT_ULTIMATE_FATAL_TEMPO_1", "ENCHANTMENT_ULTIMATE_INFERNO_1", "ANANKE_FEATHER");

    private final String displayText;
    private KuudraKey keyNeeded;
    private int essence;
    private final Map<String, Value> values = new HashMap<>();
    private long totalValue;
    private boolean hasValuable;
    private Value valuableItem;
    private BazaarItemValue essenceValue;
    private BazaarItemValue kismetValue;
    private BazaarItemValue heavyPearlValue;

    KuudraChest(String displayText) {
        this.displayText = displayText;
        this.keyNeeded = null;
        this.essence = 0;
        this.totalValue = 0;
        this.hasValuable = false;
        this.valuableItem = null;

        this.essenceValue = new BazaarItemValue(new BazaarItem("ESSENCE_CRIMSON", "§dCrimson Essence"));
        this.kismetValue = new BazaarItemValue(new BazaarItem("KISMET_FEATHER", "§9Kismet Feather"));
        this.heavyPearlValue = new BazaarItemValue(new BazaarItem("Heavy Pearl", "§6Heavy Pearl"));
    }

    public void reset() {
        this.keyNeeded = null;
        this.essence = 0;
        this.values.clear();
        this.totalValue = 0;
        this.hasValuable = false;
        this.valuableItem = null;

        this.essenceValue = new BazaarItemValue(new BazaarItem("ESSENCE_CRIMSON", "§dCrimson Essence"));
        this.kismetValue = new BazaarItemValue(new BazaarItem("KISMET_FEATHER", "§9Kismet Feather"));
        this.heavyPearlValue = new BazaarItemValue(new BazaarItem("Heavy Pearl", "§6Heavy Pearl"));
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
        String type = getType(item);
        if (type == null) return;
        switch (type) {
            case "BAZAAR-ENCHANTS":
                String[] enchant = ItemUtils.getFirstEnchant(item);
                String enchantId = enchant[0];
                String enchantName = enchant[1];
                if (isNullOrEmpty(enchantId) || isNullOrEmpty(enchantName)) return;
                BazaarItem bzItem1 = new BazaarItem(enchantId, enchantName);
                if (valuables.contains(bzItem1.getItemId()) && !hasValuable) hasValuable = true;
                values.put(enchantId, new BazaarItemValue(bzItem1));
                break;
            case "BAZAAR":
                String bazaarId = ItemUtils.getItemId(item);
                if (isNullOrEmpty(bazaarId)) return;
                String bzName = item.getDisplayName() == null ? "" : item.getDisplayName();
                BazaarItem bzItem2 = new BazaarItem(bazaarId, bzName);
                if (valuables.contains(bzItem2.getItemId()) && !hasValuable) hasValuable = true;
                int count = item.stackSize;
                if (count > 0) {
                    bzItem2.setCount(count);
                }
                values.put(bazaarId, new BazaarItemValue(bzItem2));
                break;
            case "AUCTION":
                String ahId = ItemUtils.getItemId(item);
                String ahUuid = ItemUtils.getItemUuid(item);
                String ahName = item.getDisplayName() == null ? "" : item.getDisplayName();
                int stars = getItemStars(item);
                if (isNullOrEmpty(ahId) || isNullOrEmpty(ahUuid)) return;
                AuctionItem auctionItem = new AuctionItem(ahId, ahName, ahUuid, stars);
                if (valuables.contains(auctionItem.getItemId()) && !hasValuable) hasValuable = true;
                values.put(ahUuid, new AuctionItemValue(auctionItem));
                break;
        }
    }

    public void addShard(String name, String itemId, int count) {
        Value existingShard = values.get(itemId);

        if (existingShard == null) {
            BazaarItem bazaarItem = new BazaarItem(itemId, name, count);
            values.put(itemId, new BazaarItemValue(bazaarItem));
        } else if (existingShard instanceof BazaarItemValue) {
            ((BazaarItemValue) existingShard).addCount(count);
        }
    }

    public int getTeeth() {
        Value value = values.get("KUUDRA_TEETH");
        if (value instanceof BazaarItemValue) {
            return ((BazaarItemValue) value).getItemCount();
        }
        return 0;
    }

    public void addEssence(int essence) {
        this.essence += essence;
    }

    public int getEssence() {
        if (KuudraProfitCalculatorOptions.ignoreEssence) return 0;

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
        return essence;
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

    public void updateValues(Runnable callback) {
        if (!ApiUtils.isVerified()) return;
        KICLogger.info("Updating values");

        List<Value> itemsToFetch = values.values().stream()
                .filter(item -> !item.isFetching() && !item.isCached())
                .collect(Collectors.toList());

        if (!essenceValue.isFetching() && !essenceValue.isCached()) itemsToFetch.add(essenceValue);
        if (!kismetValue.isFetching() && !kismetValue.isCached()) itemsToFetch.add(kismetValue);
        if (!values.containsKey("HEAVY_PEARL") && !heavyPearlValue.isFetching() && !heavyPearlValue.isCached()) itemsToFetch.add(heavyPearlValue);

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
                            case "BAZAAR":
                                String itemId = obj.get("itemId").getAsString();
                                Value bzValue;
                                if ("ESSENCE_CRIMSON".equals(itemId)) {
                                    bzValue = essenceValue;
                                } else if ("KISMET_FEATHER".equals(itemId)) {
                                    bzValue = kismetValue;
                                } else if ("HEAVY_PEARL".equals(itemId) && !values.containsKey("HEAVY_PEARL")) {
                                    bzValue = heavyPearlValue;
                                } else {
                                    bzValue = values.get(itemId);
                                }
                                if (!(bzValue instanceof BazaarItemValue)) continue;
                                BazaarItemValue bazaarItemValue = (BazaarItemValue) bzValue;
                                long buyPriceBazaar = obj.get("buyPrice").getAsLong();
                                long sellPriceBazaar = obj.get("sellPrice").getAsLong();
                                bazaarItemValue.setPrice(buyPriceBazaar, sellPriceBazaar);
                                bazaarItemValue.setFetching(false);
                                bazaarItemValue.setCached(true);
                                bazaarItemValue.setTimestamp(System.currentTimeMillis());
                                if ("ESSENCE_CRIMSON".equals(itemId)) {
                                    KuudraProfitTracker.updateEssencePrice(bazaarItemValue.getSingleValue());
                                } else if ("KISMET_FEATHER".equals(itemId)) {
                                    KuudraProfitTracker.updateKismetPrice(bazaarItemValue.getSingleValue());
                                } else if ("KUUDRA_TEETH".equals(itemId)) {
                                    KuudraProfitTracker.updateTeethPrice(bazaarItemValue.getSingleValue());
                                }
                                if ("HEAVY_PEARL".equals(itemId) && values.containsKey("HEAVY_PEARL")) heavyPearlValue = bazaarItemValue;
                                if (valuables.contains(itemId)) {
                                    valuableItem = bazaarItemValue;
                                    Hologram.show(valuableItem);
                                }
                                break;
                            case "AUCTION":
                                String uuid = obj.get("uuid").getAsString();
                                Value ahValue = values.get(uuid);
                                if (!(ahValue instanceof AuctionItemValue)) continue;
                                AuctionItemValue auctionItemValue = (AuctionItemValue) ahValue;
                                long priceAuction = obj.get("price").getAsLong();
                                long avgAuction = obj.get("averagePrice").getAsLong();
                                auctionItemValue.setPrice(priceAuction, avgAuction);
                                auctionItemValue.setFetching(false);
                                auctionItemValue.setCached(true);
                                auctionItemValue.setTimestamp(System.currentTimeMillis());
                                if (valuables.contains(auctionItemValue.getItemId())) {
                                    valuableItem = auctionItemValue;
                                    Hologram.show(valuableItem);
                                }
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

    private void updateTotalValue() {
        long total = getEssence() * essenceValue.getValue();

        for (Value value : values.values()) {
            long valueToAdd;

            if (value instanceof AuctionItemValue) {
                AuctionItemValue ahValue = (AuctionItemValue) value;
                long currentValue = ahValue.getValue();

                if (isArmorPiece(ahValue.getItemId())) {
                    int stars = ahValue.getItem().getStars();
                    long salvageValue = calculateSalvageValue(stars);
                    ahValue.setSalvagePrice(salvageValue);

                    if (KuudraProfitCalculatorOptions.forceSalvageValue || salvageValue > currentValue) {
                        valueToAdd = salvageValue;
                    } else {
                        valueToAdd = currentValue;
                    }
                } else {
                    valueToAdd = ahValue.getValue();
                }
            } else {
                valueToAdd = value.getValue();
            }

            total += valueToAdd;
        }

        if (keyNeeded != null) {
            total -= keyNeeded.getPrice();
        }

        this.totalValue = total;
    }

    public long getTotalValue(boolean rerolled) {
        return rerolled ? this.totalValue - kismetValue.getValue() : this.totalValue;
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

        if (this == FREE) {
            KICLogger.info("Skipping reroll: Chest is FREE.");
            return false;
        }

        if (KICConfig.ACOnlyRerollInT5 && (keyNeeded == null || keyNeeded != KuudraKey.INFERNAL)) {
            KICLogger.info("Skipping reroll: onlyRerollInT5 is enabled.");
            return false;
        }

        if (hasValuable) {
            KICLogger.info("Skipping reroll: Item id found in valuables");
            return false;
        }

        Set<String> failsafeIds = KuudraRerollFailsafeOptions.getEnabled();
        KICLogger.info("Enabled failsafe ids: " + failsafeIds);

        for (Value value : values.values()) {
            if (failsafeIds.contains(value.getItemId())) return false;
        }

        long finalValue = values.values().stream()
                .filter(value -> !"KUUDRA_TEETH".equals(value.getItemId()))
                .mapToLong(Value::getValue)
                .sum();

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

        if (this == FREE) {
            KICLogger.info("Skipping buy: Chest is FREE.");
            return false;
        }

        if (KICConfig.ACAlwaysAutoBuy) {
            KICLogger.info("Auto Buy enabled: Buying chest regardless of value.");
            return true;
        }

        if (hasValuable) {
            KICLogger.info("Should buy: Chest contains a valuable item");
            return true;
        }

        KICLogger.info("Total Chest Value: " + this.totalValue);
        KICLogger.info("Auto Buy Minimum Profit: " + KICConfig.ACAutoBuyMinProfit);

        boolean shouldBuy = this.totalValue >= KICConfig.ACAutoBuyMinProfit;
        KICLogger.info("Should Buy: " + shouldBuy);
        return shouldBuy;
    }

    private String getType(ItemStack item) {
        if (!ItemUtils.hasItemId(item)) return null;

        if (ItemUtils.hasEnchants(item)) {
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

    private long calculateSalvageValue(int starLevel) {
        int upgradeEssence = (starLevel >= 1 && starLevel <= ESSENCE_TOTALS.length)
                ? ESSENCE_TOTALS[starLevel - 1] : 0;
        int heavyPearlCost = (starLevel >= 1 && starLevel <= HEAVY_PEARL_TOTALS.length)
                ? HEAVY_PEARL_TOTALS[starLevel - 1] : 0;

        long flatEssence = 100L * essenceValue.getSingleValue();
        long upgradeEssenceSalvage = (upgradeEssence / 2L) * essenceValue.getSingleValue();
        long heavyPearlSalvage = (heavyPearlCost / 2L) * heavyPearlValue.getSingleValue();

        return flatEssence + upgradeEssenceSalvage + heavyPearlSalvage;
    }

    public boolean hasValuable() {
        return hasValuable;
    }

    // Format: type;itemId;lbPrice/buyPrice;avgPrice/sellPrice
    public String getValuable() {
        if (valuableItem instanceof BazaarItemValue) {
            BazaarItemValue value = (BazaarItemValue) valuableItem;
            return "BAZAAR;" +
                    value.getItemId() + ";" +
                    value.getPrice(true) + ";" +
                    value.getPrice(false);
        } else if (valuableItem instanceof AuctionItemValue) {
            AuctionItemValue value = (AuctionItemValue) valuableItem;
            return "AUCTION;" +
                    value.getItemId() + ";" +
                    value.getPrice(true) + ";" +
                    value.getPrice(false);
        } else {
            return null;
        }
    }
}
