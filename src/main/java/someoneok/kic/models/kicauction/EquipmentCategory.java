package someoneok.kic.models.kicauction;

import java.util.Optional;

public enum EquipmentCategory {
    NECKLACES("Necklaces"),
    CLOAKS("Cloaks"),
    BELTS("Belts"),
    BRACELETS("Bracelets");

    private final String displayText;

    EquipmentCategory(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static Optional<EquipmentCategory> getFromName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        for (EquipmentCategory category : values()) {
            if (category.displayText.equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }

    public static EquipmentCategory getNext(EquipmentCategory category) {
        if (category == null) return null;
        EquipmentCategory[] values = EquipmentCategory.values();
        return values[(category.ordinal() + 1) % values.length];
    }

    public static EquipmentCategory getPrevious(EquipmentCategory category) {
        if (category == null) return null;
        EquipmentCategory[] values = EquipmentCategory.values();
        return values[(category.ordinal() - 1 + values.length) % values.length];
    }
}
