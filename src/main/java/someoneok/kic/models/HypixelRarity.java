package someoneok.kic.models;

public enum HypixelRarity {
    NONE("None", ""),
    COMMON("Common", "§f"),
    UNCOMMON("Uncommon", "§a"),
    RARE("Rare", "§9"),
    EPIC("Epic", "§5"),
    LEGENDARY("Legendary", "§6"),
    MYTHIC("Mythic", "§d"),
    DIVINE("Divine", "§b"),
    SPECIAL("Special", "§c"),
    VERY_SPECIAL("Very Special", "§4");

    private final String name;
    private final String colorCode;

    HypixelRarity(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
    }

    public String getName() {
        return name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public static HypixelRarity fromColorCode(String colorCode) {
        for (HypixelRarity rarity : values()) {
            if (rarity.colorCode.equals(colorCode)) {
                return rarity;
            }
        }
        return NONE;
    }
}
