package someoneok.kic.models.kicauction;

import java.util.Optional;

public enum FishingCategory {
    RODS("Rods"),
    ARMOR("Armor");

    private final String displayText;

    FishingCategory(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static Optional<FishingCategory> getFromName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        for (FishingCategory category : values()) {
            if (category.displayText.equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }

    public static FishingCategory getNext(FishingCategory category) {
        if (category == null) return null;
        FishingCategory[] values = FishingCategory.values();
        return values[(category.ordinal() + 1) % values.length];
    }

    public static FishingCategory getPrevious(FishingCategory category) {
        if (category == null) return null;
        FishingCategory[] values = FishingCategory.values();
        return values[(category.ordinal() - 1 + values.length) % values.length];
    }
}
