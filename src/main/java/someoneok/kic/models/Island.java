package someoneok.kic.models;

public enum Island {
    PRIVATE_ISLAND("Private Island"),
    THE_HUB("Hub"),
    THE_PARK("The Park"),
    THE_FARMING_ISLANDS("The Farming Islands"),
    SPIDER_DEN("Spider's Den"),
    THE_END("The End"),
    CRIMSON_ISLE("Crimson Isle"),
    GOLD_MINE("Gold Mine"),
    DEEP_CAVERNS("Deep Caverns"),
    DWARVEN_MINES("Dwarven Mines"),
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    JERRY_WORKSHOP("Jerry's Workshop"),
    DUNGEON_HUB("Dungeon Hub"),
    LIMBO("UNKNOWN"),
    LOBBY("PROTOTYPE"),
    DUNGEON("Dungeon"),
    GARDEN("Garden"),
    THE_RIFT("The Rift"),
    KUUDRA("Kuudra"),
    BACKWATER_BAYOU("Backwater Bayou"),
    NO_CLUE("Nothing"),
    ALL("All");

    private final String name;

    public String getName() {
        return name;
    }

    Island(String name) {
        this.name = name;
    }
}
