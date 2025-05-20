package someoneok.kic.models.kuudra;

import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.HypixelRarity;
import someoneok.kic.models.request.KuudraKeyPriceRequest;
import someoneok.kic.utils.overlay.OverlayDataManager;

public enum KuudraKey {
    BASIC("Kuudra Key", HypixelRarity.RARE),
    HOT("Hot Kuudra Key", HypixelRarity.EPIC),
    BURNING("Burning Kuudra Key", HypixelRarity.EPIC),
    FIERY("Fiery Kuudra Key", HypixelRarity.EPIC),
    INFERNAL("Infernal Kuudra Key", HypixelRarity.LEGENDARY);

    private final String displayName;
    private final HypixelRarity rarity;
    private long buyPrice;
    private long sellPrice;

    KuudraKey(String displayName, HypixelRarity rarity) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.buyPrice = 0;
        this.sellPrice = 0;
    }

    public String getDisplayName() {
        return displayName;
    }

    public HypixelRarity getRarity() {
        return rarity;
    }

    public KuudraKeyPriceRequest getRequest() {
        return new KuudraKeyPriceRequest(this, OverlayDataManager.getUserData().getFaction());
    }

    public long getPrice() {
        return KuudraProfitCalculatorOptions.keyPriceType == 1 ? this.buyPrice : this.sellPrice;
    }

    public void setPrice(long buyPrice, long sellPrice) {
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    @Override
    public String toString() {
        return "KuudraKey{" +
                "displayName='" + displayName + '\'' +
                ", rarity=" + rarity +
                ", buyPrice=" + buyPrice +
                ", sellPrice=" + sellPrice +
                '}';
    }
}
