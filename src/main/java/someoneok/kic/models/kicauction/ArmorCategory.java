package someoneok.kic.models.kicauction;

import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;

import java.util.Optional;

public enum ArmorCategory {
    HELMETS("Helmets", null),
    CHESTPLATES("Chestplates", Items.leather_chestplate),
    LEGGINGS("Leggings", Items.leather_leggings),
    BOOTS("Boots", Items.leather_boots);

    private final String displayText;
    private final ItemArmor item;

    ArmorCategory(String displayText, ItemArmor item) {
        this.displayText = displayText;
        this.item = item;
    }

    public String getDisplayText() {
        return displayText;
    }

    public ItemArmor getItemArmor() {
        return item;
    }

    public static Optional<ArmorCategory> getFromName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        for (ArmorCategory category : values()) {
            if (category.displayText.equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }

    public static ArmorCategory getNext(ArmorCategory current) {
        if (current == null) return null;
        ArmorCategory[] values = ArmorCategory.values();
        return values[(current.ordinal() + 1) % values.length];
    }

    public static ArmorCategory getPrevious(ArmorCategory current) {
        if (current == null) return null;
        ArmorCategory[] values = ArmorCategory.values();
        return values[(current.ordinal() - 1 + values.length) % values.length];
    }
}
