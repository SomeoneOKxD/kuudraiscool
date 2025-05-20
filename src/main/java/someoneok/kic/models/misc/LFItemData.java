package someoneok.kic.models.misc;

public class LFItemData {
    private final String name;
    private final String lore;
    private int count;
    private final String source;
    private final int page;

    public LFItemData(String name, String lore, int count, String source, int page) {
        this.name = name;
        this.lore = lore;
        this.count = count;
        this.source = source;
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public String getLore() {
        return lore;
    }

    public int getCount() {
        return count;
    }

    public String getSource() {
        return source;
    }

    public int getPage() {
        return page;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
