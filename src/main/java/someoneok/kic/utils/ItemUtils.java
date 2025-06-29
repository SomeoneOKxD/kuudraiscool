package someoneok.kic.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.crimson.AuctionItemValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static someoneok.kic.utils.StringUtils.formatId;

public class ItemUtils {
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

    public static int getItemStars(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return 0;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null || !nbt.hasKey("ExtraAttributes")) return 0;

        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        return extraAttributes.hasKey("upgrade_level") ? extraAttributes.getInteger("upgrade_level") : 0;
    }

    public static boolean hasItemId(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return false;

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return false;

        if (!nbt.hasKey("ExtraAttributes")) return false;
        NBTTagCompound extraAttributes = nbt.getCompoundTag("ExtraAttributes");

        return extraAttributes.hasKey("id");
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

    public static boolean isArmorPiece(String item) {
        item = item.toUpperCase();
        return item.contains("HELMET")
                || item.contains("CHESTPLATE")
                || item.contains("LEGGINGS")
                || item.contains("BOOTS");
    }

    public static boolean useSalvageValue(AuctionItemValue value) {
        if (isArmorPiece(value.getItemId())) {
            return KuudraProfitCalculatorOptions.forceSalvageValue || value.getSalvagePrice() > value.getValue();
        }
        return false;
    }
}
