package someoneok.kic.models.kuudra.chest;

public enum KuudraChestType {
    FREE("Free Chest"),
    PAID("Paid Chest");

    private final String displayText;

    KuudraChestType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
