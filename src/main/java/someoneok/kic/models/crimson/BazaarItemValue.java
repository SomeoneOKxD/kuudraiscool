package someoneok.kic.models.crimson;

import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.request.BazaarPriceRequest;

public class BazaarItemValue implements Value {
    private final BazaarItem item;
    private boolean cached;
    private boolean fetching;
    private long timestamp;
    private long buyPrice;
    private long sellPrice;

    public BazaarItemValue(BazaarItem bazaarItem) {
        item = bazaarItem;
        this.cached = false;
        this.fetching = false;
        this.timestamp = System.currentTimeMillis();
        this.buyPrice = 0;
        this.sellPrice = 0;
    }

    public String getItemId() {
        return item.getItemId();
    }

    public String getName() {
        return item.getName();
    }

    public boolean isCached() {
        return cached;
    }

    public boolean isFetching() {
        return fetching;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public void setFetching(boolean fetching) {
        this.fetching = fetching;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPrice(long buyPrice, long sellPrice) {
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public long getPrice(boolean buyPrice) {
        return buyPrice ? this.buyPrice : this.sellPrice;
    }

    public int getItemCount() {
        return this.item.getCount();
    }

    public long getValue() {
        if ("KUUDRA_TEETH".equals(item.getItemId()) && KuudraProfitCalculatorOptions.ignoreTeeth) return 0;
        if ("KISMET_FEATHER".equals(item.getItemId())) return getPrice(KuudraProfitCalculatorOptions.kismetPriceType == 1) * item.getCount();

        return getPrice(KuudraProfitCalculatorOptions.bazaarPriceType == 1) * item.getCount();
    }

    public long getSingleValue() {
        if ("KUUDRA_TEETH".equals(item.getItemId()) && KuudraProfitCalculatorOptions.ignoreTeeth) return 0;
        if ("KISMET_FEATHER".equals(item.getItemId())) return getPrice(KuudraProfitCalculatorOptions.kismetPriceType == 1);

        return getPrice(KuudraProfitCalculatorOptions.bazaarPriceType == 1);
    }

    public BazaarPriceRequest mapToRequest() {
        return new BazaarPriceRequest(item.getItemId());
    }

    public void setCount(int count) {
        item.setCount(count);
    }

    public void addCount(int count) {
        item.addCount(count);
    }

    @Override
    public String toString() {
        return "BazaarItemValue{" +
                "item=" + item.toString() +
                ", cached=" + cached +
                ", fetching=" + fetching +
                ", timestamp=" + timestamp +
                ", buyPrice=" + buyPrice +
                ", sellPrice=" + sellPrice +
                '}';
    }
}
