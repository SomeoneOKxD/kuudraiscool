package someoneok.kic.models.crimson;

import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.request.AuctionPriceRequest;

public class AuctionItemValue implements Value {
    private final AuctionItem item;
    private boolean cached;
    private boolean fetching;
    private long timestamp;
    private long price;
    private long avgPrice;
    private long salvagePrice;

    public AuctionItemValue(AuctionItem auctionItem) {
        item = auctionItem;
        this.cached = false;
        this.fetching = false;
        this.timestamp = System.currentTimeMillis();
        this.price = 0;
        this.avgPrice = 0;
        this.salvagePrice = 0;
    }

    public String getItemId() {
        return item.getItemId();
    }

    public String getUuid() {
        return item.getUuid();
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

    public void setPrice(long price, long avgPrice) {
        this.price = price;
        this.avgPrice = avgPrice;
    }

    public long getPrice(boolean lb) {
        return lb ? this.price : this.avgPrice;
    }

    public long getValue() {
        if ("RUNIC_STAFF".equals(item.getItemId()) && KuudraProfitCalculatorOptions.ignoreAuroraStaff) return 0;
        if ("HOLLOW_WAND".equals(item.getItemId()) && KuudraProfitCalculatorOptions.ignoreHollowWand) return 0;
        return getPrice(KuudraProfitCalculatorOptions.auctionPriceType == 0);
    }

    public AuctionItem getItem() {
        return item;
    }

    public long getSalvagePrice() {
        return salvagePrice;
    }

    public void setSalvagePrice(long salvagePrice) {
        this.salvagePrice = salvagePrice;
    }

    public AuctionPriceRequest mapToRequest() {
        return new AuctionPriceRequest(item.getItemId(), item.getUuid());
    }

    @Override
    public String toString() {
        return "AuctionItemValue{" +
                "item=" + item.toString() +
                ", cached=" + cached +
                ", fetching=" + fetching +
                ", timestamp=" + timestamp +
                ", price=" + price +
                ", avgPrice=" + avgPrice +
                '}';
    }
}
