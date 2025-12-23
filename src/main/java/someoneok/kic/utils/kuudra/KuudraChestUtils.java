package someoneok.kic.utils.kuudra;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import someoneok.kic.KIC;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.HypixelRarity;
import someoneok.kic.models.crimson.AuctionItem;
import someoneok.kic.models.crimson.AuctionItemValue;
import someoneok.kic.models.crimson.BazaarItem;
import someoneok.kic.models.crimson.BazaarItemValue;
import someoneok.kic.models.kuudra.chest.ItemKind;
import someoneok.kic.models.kuudra.chest.KuudraChest;
import someoneok.kic.models.kuudra.chest.KuudraKey;
import someoneok.kic.utils.ItemUtils;
import someoneok.kic.utils.PlayerUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.utils.ItemUtils.*;
import static someoneok.kic.utils.StringUtils.*;

public class KuudraChestUtils {
    private static final Pattern ESSENCE_REGEX = Pattern.compile("§d(?<type>\\w+) Essence §8x(?<count>\\d+)");
    private static final Pattern SHARD_REGEX = Pattern.compile("(?<name>§.?(?<type>.+?) Shard) §8x(?<count>\\d+)");

    private static final int[] ESSENCE_TOTALS    = {30, 65, 105, 150, 200, 255, 315, 385, 465, 555};
    private static final int[] HEAVY_PEARL_TOTALS= {0,  0,  0,   0,   0,   0,   0,   2,   5,   9};

    public static final Set<String> BAZAAR_IDS = new HashSet<>(Arrays.asList(
            "KUUDRA_TEETH","MANDRAA","KUUDRA_MANDIBLE","ESSENCE_CRIMSON",
            "KISMET_FEATHER","HEAVY_PEARL","TOXIC_ARROW_POISON","TWILIGHT_ARROW_POISON",
            "KUUDRA_TENTACLE"
    ));

    public static final Set<String> VALUABLES = new HashSet<>(Arrays.asList(
            "BURNING_KUUDRA_CORE","TORMENTOR","HELLSTORM_STAFF","TENTACLE_DYE",
            "ENCHANTMENT_ULTIMATE_FATAL_TEMPO_1","ENCHANTMENT_ULTIMATE_INFERNO_1",
            "ANANKE_FEATHER"
    ));

    public static boolean processChest(KuudraChest chest, Map<Integer, ItemStack> inventory) {
        try {
            ItemStack openChest = inventory.get(31);
            KuudraKey key = getKeyNeeded(openChest);
            chest.setKeyNeeded(key);

            ItemStack rerollItem = inventory.get(50);
            if (validRerollItem(rerollItem)) {
                String lore = removeFormatting(ItemUtils.getItemLoreString(rerollItem));
                if (lore.contains("You already rerolled this chest")) chest.setRerolled(true);
            }

            ItemStack shardRerollItem = inventory.get(51);
            if (validShardRerollItem(shardRerollItem)) {
                String lore = removeFormatting(ItemUtils.getItemLoreString(shardRerollItem));
                if (lore.contains("You already rerolled this shard")) chest.setShardRerolled(true);
            }

            for (int i = 9; i <= 17; i++) {
                ItemStack lootSlot = inventory.get(i);
                if (lootSlot == null) continue;

                String displayName = lootSlot.getDisplayName();

                if (removeFormatting(displayName).equals("Dusty Travel Scroll to the Kuudra Skull")) {
                    chest.addAuctionItem(new AuctionItem("NETHER_FORTRESS_BOSS_TRAVEL_SCROLL", displayName, "TravelScroll", 0));
                    continue;
                }

                int essence = getCrimsonEssenceCount(displayName);
                if (essence != -1) {
                    chest.addEssence(essence);
                    continue;
                }

                Matcher matcher = SHARD_REGEX.matcher(displayName);
                if (matcher.find()) {
                    try {
                        String name = matcher.group("name");
                        String type = matcher.group("type");
                        int count = Integer.parseInt(matcher.group("count"));

                        String shardId = "SHARD_" + type.toUpperCase().replace(" ", "_");
                        chest.addShard(name, shardId, count);
                    } catch (NumberFormatException ignored) {}
                    continue;
                }

                NBTTagCompound tag = lootSlot.writeToNBT(new NBTTagCompound());
                if (!tag.hasKey("id")) continue;

                String id = tag.getString("id");
                if (id == null || id.isEmpty() ||
                        id.equals("minecraft:stained_glass_pane") ||
                        id.equals("minecraft:chest") ||
                        id.equals("minecraft:barrier")) {
                    continue;
                }

                chest.addItem(lootSlot.copy());
            }
        } catch (Exception e) {
            KICLogger.error("[GUI] Failed to parse chest contents: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static KuudraKey getKeyNeeded(ItemStack itemStack) {
        NBTTagList lore = getNbtTagList(itemStack);
        if (lore != null) {
            for (int i = 0; i < lore.tagCount() - 1; i++) {
                String line = lore.getStringTagAt(i);

                if (!"§7Cost".equals(line)) continue;

                String costLine = lore.getStringTagAt(i + 1);
                if ("§aThis Chest is Free!".equals(costLine) || "§aFREE".equals(costLine)) return null;

                String strippedCost = removeFormatting(costLine);
                return Arrays.stream(KuudraKey.values())
                        .filter(key -> key.getDisplayName().equals(strippedCost))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Unknown Kuudra key: " + strippedCost));
            }
        }

        throw new IllegalStateException("Could not find key needed for chest");
    }

    public static int getCrimsonEssenceCount(String text) {
        Matcher matcher = ESSENCE_REGEX.matcher(text);
        if (!matcher.matches()) return -1;

        String type = matcher.group("type");
        if (!"crimson".equalsIgnoreCase(type)) return -1;

        String countStr = matcher.group("count");
        try { return Integer.parseInt(countStr); } catch (NumberFormatException e) { return -1; }
    }

    public static boolean canBuy(KuudraChest chest, ItemStack itemStack) {
        if (chest.isBought()) return false;
        String lore = removeFormatting(ItemUtils.getItemLoreString(itemStack));
        if (lore.contains("Can't open another chest") || lore.contains("Already opened")) return false;
        return hasRequiredKey(chest.getKeyNeeded());
    }

    private static boolean hasRequiredKey(KuudraKey key) {
        if (key == null) return true;
        return PlayerUtils.getInventoryItemStack(key.getDisplayName()) != null;
    }

    private static boolean validRerollItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getDisplayName().contains("Reroll Chest");
    }

    public static boolean canReroll(ItemStack itemStack) {
        if (!validRerollItem(itemStack)) return false;
        String lore = removeFormatting(ItemUtils.getItemLoreString(itemStack));
        return !(lore.contains("You already rerolled this chest")
                || lore.contains("Bring a Kismet Feather")
                || lore.contains("Chest already opened"));
    }

    private static boolean validShardRerollItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getDisplayName().contains("Reroll Shard");
    }

    public static boolean canRerollShard(ItemStack itemStack) {
        if (!validShardRerollItem(itemStack)) return false;
        String lore = removeFormatting(ItemUtils.getItemLoreString(itemStack));
        return !lore.contains("You already rerolled this shard");
    }

    public static boolean validBuyItem(ItemStack itemStack) {
        return itemStack != null && "§aOpen Reward Chest".equals(itemStack.getDisplayName());
    }

    public static String getProfitText(KuudraChest chest) {
        if (chest == null) return KIC.KICPrefix + " §cError getting profit details!";

        StringBuilder text = new StringBuilder(String.format("%s §a§lChest Profit\n", KIC.KICPrefix));

        // === Total Profit ===
        long totalProfit = chest.getTotalValue();
        text.append("§eTotal: §r")
                .append(totalProfit > 0 ? "§a+" : "§c")
                .append(parseToShorthandNumber(totalProfit))
                .append("\n");

        // === Key Cost (if applicable) ===
        KuudraKey key = chest.getKeyNeeded();
        if (key != null) {
            long keyPrice = KuudraValueCache.getKeyPrice(key);
            text.append("\n§c-")
                    .append(parseToShorthandNumber(keyPrice))
                    .append(" §7| ")
                    .append(key.getRarity().getColorCode())
                    .append(key.getDisplayName());
        }

        // === Item Value Collection ===
        Map<String, Long> items = new HashMap<>();

        for (Map.Entry<String, BazaarItem> e : chest.getBazaarItems().entrySet()) {
            String itemId = e.getKey();
            BazaarItem meta = e.getValue();

            if ("KUUDRA_TEETH".equals(itemId) && KuudraProfitCalculatorOptions.ignoreTeeth) continue;

            BazaarItemValue v = KuudraValueCache.getBazaar(itemId);
            if (v == null) continue;

            long itemValue = (long) meta.getCount() * v.getValue();
            String name = v.getName();
            if (meta.getCount() > 1) name += " §ex" + meta.getCount();

            if (!isNullOrEmpty(name)) items.put(name, itemValue);
        }

        for (Map.Entry<String, AuctionItem> e : chest.getAuctionItems().entrySet()) {
            String itemId = e.getKey();
            AuctionItem meta = e.getValue();

            AuctionItemValue v = KuudraValueCache.getAuction(itemId);
            if (v == null) continue;

            long current = v.getValue();
            long add;

            if (isArmorPiece(itemId)) {
                long salvage = KuudraChestUtils.calculateSalvageValueArmor(meta.getStars(), KuudraValueCache.essence(), KuudraValueCache.heavyPearl());
                v.setSalvagePrice(salvage);
                boolean useSalvage = KuudraProfitCalculatorOptions.forceSalvageValueArmor || salvage > current;
                add = useSalvage ? salvage : current;
            } else if (isWandOrStaff(itemId) || isMolten(itemId)) {
                long salvage = KuudraChestUtils.calculateSalvageValueMoltenWandStaff(KuudraValueCache.essence());
                v.setSalvagePrice(salvage);
                boolean useSalvage = KuudraProfitCalculatorOptions.forceSalvageValueStaffWand
                        || KuudraProfitCalculatorOptions.forceSalvageValueEquipment
                        || salvage > current;
                add = useSalvage ? salvage : current;
            } else {
                add = current;
            }

            String name = meta.getName();
            if (add != current) name = "§e§m" + removeFormatting(name);

            if (!isNullOrEmpty(name)) items.put(name, add);
        }

        // === Essence Handling ===
        int essenceCount = chest.getEssence();
        if (essenceCount > 0) {
            BazaarItemValue essence = KuudraValueCache.essence();
            long totalEssenceValue = essence.getValue() * essenceCount;
            String essenceName = essence.getName() + " §ex" + essenceCount;
            items.put(essenceName, totalEssenceValue);
        }

        // === Sorted Output ===
        items.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    long value = entry.getValue();
                    String name = entry.getKey();
                    String prefix = value > 0 ? "§a+" : "§c";

                    text.append("\n").append(prefix)
                            .append(parseToShorthandNumber(value))
                            .append(" §7| ")
                            .append(name);
                });

        // === Kismet Cost (only if rerolled) ===
        if (chest.isRerolled()) {
            BazaarItemValue kismet = KuudraValueCache.kismet();
            text.append("\n§c-")
                    .append(parseToShorthandNumber(kismet.getValue()))
                    .append(" §7| ")
                    .append(kismet.getName());
        }

        // === Wheel Cost (only if shard rerolled) ===
        if (chest.isShardRerolled()) {
            AuctionItemValue wof = KuudraValueCache.wheelOfFate();
            text.append("\n§c-")
                    .append(parseToShorthandNumber(wof.getValue()))
                    .append(" §7| ")
                    .append(wof.getName());
        }

        return text.toString();
    }

    public static int applyPetBoostIfEnabled(int baseEssence) {
        if (!KuudraProfitCalculatorOptions.includeKuudraPetPerk) return baseEssence;
        int rarIdx = KuudraProfitCalculatorOptions.kuudraPetRarity;
        int lvl    = KuudraProfitCalculatorOptions.kuudraPetLevel;
        if (rarIdx == 0 || lvl == 0) return baseEssence;
        HypixelRarity rarity = HypixelRarity.values()[rarIdx];
        double pct = getKuudraPetEssenceBoost(rarity, lvl);
        return baseEssence + (int)Math.round(baseEssence * (pct / 100.0));
    }

    public static double getKuudraPetEssenceBoost(HypixelRarity rarity, int level) {
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

    public static ItemKind classify(ItemStack item) {
        if (!ItemUtils.hasItemId(item)) return ItemKind.NONE;
        if (ItemUtils.hasEnchants(item)) return ItemKind.BAZAAR_ENCHANT;

        String id = ItemUtils.getItemId(item);
        if (id == null) return ItemKind.NONE;
        return BAZAAR_IDS.contains(id) ? ItemKind.BAZAAR : ItemKind.AUCTION;
    }

    public static long calculateSalvageValueArmor(int starLevel, BazaarItemValue essenceValue, BazaarItemValue heavyPearlValue) {
        int upgradeEssence = (starLevel >= 1 && starLevel <= ESSENCE_TOTALS.length)
                ? ESSENCE_TOTALS[starLevel - 1] : 0;
        int heavyPearlCost = (starLevel >= 1 && starLevel <= HEAVY_PEARL_TOTALS.length)
                ? HEAVY_PEARL_TOTALS[starLevel - 1] : 0;

        long flatEssence = 100L * essenceValue.getSingleValue();
        long upgradeEssenceSalvage = (upgradeEssence / 2L) * essenceValue.getSingleValue();
        long heavyPearlSalvage     = (heavyPearlCost / 2L) * heavyPearlValue.getSingleValue();
        return flatEssence + upgradeEssenceSalvage + heavyPearlSalvage;
    }

    public static long calculateSalvageValueMoltenWandStaff(BazaarItemValue essenceValue) {
        return 500L * essenceValue.getSingleValue();
    }
}
