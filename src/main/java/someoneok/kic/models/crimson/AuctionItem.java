package someoneok.kic.models.crimson;

public class AuctionItem {
    private final String itemId;
    private final String name;
    private final String uuid;

    public AuctionItem(String itemId, String name, String uuid) {
        this.itemId = itemId;
        this.name = name;
        this.uuid = uuid;
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

    @Override
    public String toString() {
        return "AuctionItem{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
