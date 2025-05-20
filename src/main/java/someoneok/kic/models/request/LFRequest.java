package someoneok.kic.models.request;

public class LFRequest {
    private final String ign;
    private final String search;
    private final boolean lore;

    public LFRequest(String ign, String search, boolean lore) {
        this.ign = ign;
        this.search = search;
        this.lore = lore;
    }

    public String getSearch() {
        return search;
    }

    public boolean isLore() {
        return lore;
    }
}
