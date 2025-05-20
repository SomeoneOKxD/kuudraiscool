package someoneok.kic.models.request;

public class AuctionPriceRequest implements Request {
    private final String type;
    private final String uuid;
    private final String itemId;

    public AuctionPriceRequest(String itemId, String uuid) {
        this.type = "AUCTION";
        this.itemId = itemId;
        this.uuid = uuid;
    }
}
