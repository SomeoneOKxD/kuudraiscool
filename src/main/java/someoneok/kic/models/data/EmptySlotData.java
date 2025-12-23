package someoneok.kic.models.data;

import java.util.HashMap;
import java.util.Map;

public class EmptySlotData {
    private final Map<Integer, Integer> enderChestPages = new HashMap<>();
    private final Map<Integer, Integer> backpacks = new HashMap<>();

    public Map<Integer, Integer> getEnderChestPages() {
        return enderChestPages;
    }

    public Map<Integer, Integer> getBackpacks() {
        return backpacks;
    }
}
