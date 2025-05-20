package someoneok.kic.models.crimson;

public class BazaarItem {
    private final String itemId;
    private final String name;
    private int count;

    public BazaarItem(String itemId, String name) {
        this.itemId = itemId;
        this.name = name;
        this.count = 1;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "BazaarItem{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}
