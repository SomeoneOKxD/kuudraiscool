package someoneok.kic.models.kuudra;

public enum CrimsonFaction {
    MAGE("Mage"),
    BARBARIAN("Barbarian"),
    NONE("None");

    private final String name;

    CrimsonFaction(String name) {
        this.name = name;
    }

    public static CrimsonFaction fromString(String str) {
        if (str == null || str.isEmpty()) return NONE;
        try {
            return CrimsonFaction.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }

    public String getName() {
        return this.name;
    }
}
