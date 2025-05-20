package someoneok.kic.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import someoneok.kic.models.Color;
import someoneok.kic.models.HypixelRarity;
import someoneok.kic.models.Pet;
import someoneok.kic.models.crimson.AttributeItem;
import someoneok.kic.models.crimson.Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.utils.StringUtils.formatId;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class ItemUtils {
    private static final Pattern PET_PATTERN = Pattern.compile(
            "(?:§e⭐ )?§7\\[Lvl (?<level>\\d+)](?: §8\\[.*])? (?<color>§[0-9a-fk-or])(?<name>.+)"
    );
    private static final int NBT_STRING = 8;
    private static final int NBT_LIST = 9;
    private static final int NBT_COMPOUND = 10;

    public static List<String> getItemLore(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tagCompound = itemStack.getTagCompound();
            if (tagCompound.hasKey("display", NBT_COMPOUND)) {
                NBTTagCompound display = tagCompound.getCompoundTag("display");
                if (display.hasKey("Lore", NBT_LIST)) {
                    NBTTagList lore = display.getTagList("Lore", NBT_STRING);
                    List<String> loreAsList = new ArrayList<>(lore.tagCount());

                    for (int i = 0; i < lore.tagCount(); i++) {
                        loreAsList.add(lore.getStringTagAt(i));
                    }

                    return Collections.unmodifiableList(loreAsList);
                }
            }
        }
        return Collections.emptyList();
    }

    public static Pet getPetInfo(String petName) {
        Matcher matcher = PET_PATTERN.matcher(petName);
        if (matcher.matches()) {
            int level = Integer.parseInt(matcher.group("level"));
            String color = matcher.group("color");
            String name = matcher.group("name");
            HypixelRarity rarity = HypixelRarity.fromColorCode(color);
            return new Pet(level, name, rarity);
        }
        return null;
    }

    public static String getItemUuid(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null || !nbt.hasKey("ExtraAttributes")) return null;

        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        return extraAttributes.hasKey("uuid") ? extraAttributes.getString("uuid") : null;
    }

    public static String getItemId(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null || !nbt.hasKey("ExtraAttributes")) return null;

        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        return extraAttributes.hasKey("id") ? extraAttributes.getString("id") : null;
    }

    public static Attributes getAttributes(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null || !nbt.hasKey("ExtraAttributes")) return null;

        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");
        if (!extraAttributes.hasKey("attributes")) return null;

        NBTTagCompound attributesTag = extraAttributes.getCompoundTag("attributes");

        String name1 = null;
        int level1 = 0;
        String name2 = null;
        int level2 = 0;

        for (String key : attributesTag.getKeySet()) {
            if (name1 == null) {
                name1 = key;
                level1 = attributesTag.getInteger(key);
            } else {
                name2 = key;
                level2 = attributesTag.getInteger(key);
                break;
            }
        }

        return new Attributes(name1, level1, name2, level2);
    }

    public static boolean hasAttributes(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return false;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return false;

        if (!nbt.hasKey("ExtraAttributes")) return false;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        if (!extraAttributes.hasKey("attributes")) return false;
        NBTTagCompound attributes = extraAttributes.getCompoundTag("attributes");

        return !attributes.hasNoTags();
    }

    public static boolean hasItemId(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return false;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return false;

        if (!nbt.hasKey("ExtraAttributes")) return false;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        return extraAttributes.hasKey("id");
    }

    public static boolean hasUuid(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return false;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return false;

        if (!nbt.hasKey("ExtraAttributes")) return false;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        return extraAttributes.hasKey("uuid");
    }

    public static boolean hasUuidAndItemId(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return false;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return false;

        if (!nbt.hasKey("ExtraAttributes")) return false;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        return extraAttributes.hasKey("uuid") && extraAttributes.hasKey("id");
    }

    public static boolean hasEnchants(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return false;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return false;

        if (!nbt.hasKey("ExtraAttributes")) return false;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        if (!extraAttributes.hasKey("enchantments")) return false;
        NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");

        return !enchantments.hasNoTags();
    }

    public static AttributeItem mapToAttributeItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return null;

        if (!nbt.hasKey("ExtraAttributes")) return null;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        if (!extraAttributes.hasKey("uuid") || !extraAttributes.hasKey("id")) return null;
        String uuid = extraAttributes.getString("uuid");
        String itemId = extraAttributes.getString("id");
        String name = itemStack.hasDisplayName() ? itemStack.getDisplayName() : null;

        if (isNullOrEmpty(uuid) || isNullOrEmpty(itemId) || isNullOrEmpty(name)) return null;

        return new AttributeItem(uuid, name, itemId, getAttributes(itemStack));
    }

    public static AttributeItem mapAhItemToAttributeItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return null;

        if (!nbt.hasKey("ExtraAttributes")) return null;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        if (!extraAttributes.hasKey("uuid") || !extraAttributes.hasKey("id")) return null;
        String uuid = extraAttributes.getString("uuid");
        String itemId = extraAttributes.getString("id");

        String name = null;
        if (nbt.hasKey("display")) {
            NBTTagCompound display = nbt.getCompoundTag("display");
            if (display.hasKey("Lore")) {
                NBTTagList loreList = display.getTagList("Lore", 8);
                if (loreList.tagCount() > 1) {
                    name = loreList.getStringTagAt(1).trim();
                }
            }
        }

        if (isNullOrEmpty(uuid) || isNullOrEmpty(itemId) || isNullOrEmpty(name)) return null;

        return new AttributeItem(uuid, name, itemId, getAttributes(itemStack));
    }

    public static String[] getFirstEnchant(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return null;

        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");
        if (!extraAttributes.hasKey("enchantments")) return null;

        NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");
        if (enchantments.hasNoTags()) return null;

        for (String enchantmentName : enchantments.getKeySet()) {
            int enchantmentLevel = enchantments.getInteger(enchantmentName);
            String id = enchantmentName.toUpperCase() + "_" + enchantmentLevel;
            String name = formatId(id);
            return new String[]{"ENCHANTMENT_" + id, name, enchantmentName};
        }

        return null;
    }

    public static ItemStack createItemWithDamage(ItemStack stack, int damage) {
        stack.setItemDamage(damage);
        stack.setStackDisplayName("");
        return stack;
    }

    public static ItemStack createItem(ItemStack stack, String displayName) {
        stack.setStackDisplayName(displayName);
        return stack;
    }

    public static void setItemLore(ItemStack stack, List<String> lore) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound display = stack.getTagCompound().getCompoundTag("display");
        if (!stack.getTagCompound().hasKey("display")) {
            stack.getTagCompound().setTag("display", display);
        }

        NBTTagList loreTag = new NBTTagList();
        for (String line : lore) {
            loreTag.appendTag(new net.minecraft.nbt.NBTTagString(line));
        }

        display.setTag("Lore", loreTag);
    }

    public static void updatePaneColor(ItemStack pane, int configColor) {
        pane.setItemDamage(getStainedGlassMetaFromChatColor(Color.getColorCode(configColor)));
    }

    private static int getStainedGlassMetaFromChatColor(String colorCode) {
        switch (colorCode) {
            case "§0": return 15; // Black
            case "§1": return 11; // Dark Blue
            case "§2": return 13; // Dark Green
            case "§3": return 9;  // Dark Aqua -> Cyan
            case "§4": return 14; // Dark Red
            case "§5": return 10; // Dark Purple
            case "§6": return 1;  // Gold -> Orange
            case "§7": return 8;  // Gray -> Light Gray
            case "§8": return 7;  // Dark Gray
            case "§9": return 3;  // Blue -> Light Blue
            case "§a": return 5;  // Green -> Lime
            case "§b": return 3;  // Aqua -> Light Blue
            case "§c": return 14; // Red
            case "§d": return 2;  // Light Purple -> Magenta
            case "§e": return 4;  // Yellow
            case "§f": return 0;  // White
            default: return 0;   // Default White
        }
    }
}
