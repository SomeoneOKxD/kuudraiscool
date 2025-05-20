package someoneok.kic.models;

public class Pet {
    private final int level;
    private final String name;
    private final HypixelRarity rarity;

    public Pet(int level, String name, HypixelRarity rarity) {
        this.level = level;
        this.name = name;
        this.rarity = rarity;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public HypixelRarity getRarity() {
        return rarity;
    }
}
