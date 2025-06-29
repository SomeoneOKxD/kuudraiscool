package someoneok.kic.models.crimson;

public class AuctionItem {
    private final String itemId;
    private final String name;
    private final String uuid;
    private final int stars;

    public AuctionItem(String itemId, String name, String uuid) {
        this.itemId = itemId;
        this.name = name;
        this.uuid = uuid;
        this.stars = 0;
    }

    public AuctionItem(String itemId, String name, String uuid, int stars) {
        this.itemId = itemId;
        this.name = name;
        this.uuid = uuid;
        this.stars = stars;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public int getStars() {
        return stars;
    }

    @Override
    public String toString() {
        return "AuctionItem{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", stars=" + stars +
                '}';
    }
}
