package someoneok.kic.models.kicauction;

import java.util.Optional;

public enum ArmorType {
    CRIMSON("Crimson"),
    AURORA("Aurora"),
    TERROR("Terror"),
    HOLLOW("Hollow"),
    FERVOR("Fervor");

    private final String displayText;

    ArmorType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static Optional<ArmorType> getFromName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        for (ArmorType type : values()) {
            if (type.displayText.equalsIgnoreCase(name)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static ArmorType getNext(ArmorType current) {
        if (current == null) return null;
        ArmorType[] values = ArmorType.values();
        return values[(current.ordinal() + 1) % values.length];
    }

    public static ArmorType getPrevious(ArmorType current) {
        if (current == null) return null;
        ArmorType[] values = ArmorType.values();
        return values[(current.ordinal() - 1 + values.length) % values.length];
    }

    public static boolean isArmorType(String id) {
        if (id == null || id.trim().isEmpty()) return false;

        String upperId = id.toUpperCase();
        for (ArmorType type : values()) {
            if (upperId.contains(type.displayText.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
