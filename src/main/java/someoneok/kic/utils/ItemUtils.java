package someoneok.kic.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static someoneok.kic.utils.StringUtils.formatId;

public class ItemUtils {
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_LORE    = "Lore";
    public static final int NBT_STRING = 8;
    public static final int NBT_LIST = 9;
    public static final int NBT_COMPOUND = 10;

    private static NBTTagList getLoreTag(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return null;

        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(TAG_DISPLAY, NBT_COMPOUND)) return null;

        NBTTagCompound display = root.getCompoundTag(TAG_DISPLAY);
        if (!display.hasKey(TAG_LORE, NBT_LIST)) return null;

        return display.getTagList(TAG_LORE, NBT_STRING);
    }

    public static List<String> getItemLore(ItemStack stack) {
        NBTTagList lore = getLoreTag(stack);
        if (lore == null || lore.tagCount() == 0) return Collections.emptyList();

        int n = lore.tagCount();
        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(lore.getStringTagAt(i));
        }
        return Collections.unmodifiableList(out);
    }

    public static String getItemLoreString(ItemStack stack) {
        List<String> lines = getItemLore(stack);
        return lines.isEmpty() ? "" : String.join("\n", lines);
    }

    public static NBTTagList getNbtTagList(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) return null;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (!tag.hasKey("display", NBT_COMPOUND)) return null;

        NBTTagCompound display = tag.getCompoundTag("display");
        if (!display.hasKey("Lore", NBT_LIST)) return null;

        return display.getTagList("Lore", NBT_STRING);
    }

    public static String getItemUuid(ItemStack itemStack) {
        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null) return null;

        return extraAttributes.hasKey("uuid") ? extraAttributes.getString("uuid") : null;
    }

    public static String getItemId(ItemStack itemStack) {
        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null) return null;

        return extraAttributes.hasKey("id") ? extraAttributes.getString("id").toUpperCase() : null;
    }

    public static NBTTagCompound getExtraAttributes(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null || !nbt.hasKey("ExtraAttributes")) return null;

        return nbt.getCompoundTag("ExtraAttributes");
    }

    public static int getItemStars(ItemStack itemStack) {
        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null) return 0;

        return extraAttributes.hasKey("upgrade_level") ? extraAttributes.getInteger("upgrade_level") : 0;
    }

    public static boolean hasItemId(ItemStack itemStack) {
        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null) return false;

        return extraAttributes.hasKey("id");
    }

    public static boolean hasEnchants(ItemStack itemStack) {
        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null || !extraAttributes.hasKey("enchantments")) return false;

        NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");
        return !enchantments.hasNoTags();
    }

    public static String[] getFirstEnchant(ItemStack itemStack) {
        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null || !extraAttributes.hasKey("enchantments")) return null;

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

    public static boolean isWandOrStaff(String itemId) {
        itemId = itemId.toUpperCase();
        return itemId.equals("RUNIC_STAFF") || itemId.equals("HOLLOW_WAND");
    }

    public static boolean isArmorPiece(String itemId) {
        itemId = itemId.toUpperCase();
        return itemId.contains("HELMET")
                || itemId.contains("CHESTPLATE")
                || itemId.contains("LEGGINGS")
                || itemId.contains("BOOTS");
    }

    public static boolean isMolten(String itemId) {
        itemId = itemId.toUpperCase();
        return itemId.contains("MOLTEN");
    }

    public static boolean hasAbility(ItemStack stack) {
        if (stack == null) return false;

        List<String> lore = getItemLore(stack);

        if (lore != null) {
            for (String line : lore) {
                if (line.contains("Ability:") && line.endsWith("RIGHT CLICK")) {
                    return true;
                }
            }
        }

        return false;
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

    public static boolean isNonEmpty(ItemStack stack) {
        return stack != null && stack.stackSize > 0;
    }
}
