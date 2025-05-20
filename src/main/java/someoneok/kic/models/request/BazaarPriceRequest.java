package someoneok.kic.models.request;

public class BazaarPriceRequest implements Request {
    private final String type;
    private final String uuid;
    private final String itemId;

    public BazaarPriceRequest(String itemId) {
        this.type = "BAZAAR";
        this.itemId = itemId;
        this.uuid = itemId;
    }
}
