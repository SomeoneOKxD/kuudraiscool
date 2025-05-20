package someoneok.kic.models.kicauction;

public enum FishingType {
    MAGMA_LORD("Magma Lord"),
    THUNDER("Thunder");

    private final String displayText;

    FishingType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static FishingType getNext(FishingType type) {
        if (type == null) return null;
        FishingType[] values = FishingType.values();
        return values[(type.ordinal() + 1) % values.length];
    }

    public static FishingType getPrevious(FishingType type) {
        if (type == null) return null;
        FishingType[] values = FishingType.values();
        return values[(type.ordinal() - 1 + values.length) % values.length];
    }
}
