package someoneok.kic.models.crimson;

import java.util.Objects;

public class BazaarItem {
    private final String itemId;
    private final String name;
    private int count;

    public BazaarItem(String itemId, String name) {
        this.itemId = itemId;
        this.name = name;
        this.count = 1;
    }

    public BazaarItem(String itemId, String name, int count) {
        this.itemId = itemId;
        this.name = name;
        this.count = count;
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

    public void addCount(int count) {
        this.count += count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BazaarItem that = (BazaarItem) o;
        return count == that.count && Objects.equals(itemId, that.itemId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, name, count);
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
