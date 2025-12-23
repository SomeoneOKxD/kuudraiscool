package someoneok.kic.events;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;
import someoneok.kic.utils.InventoryTracker;

import java.util.LinkedHashMap;
import java.util.Map;

public class InventoryOpenEvent extends Event {
    private final InventoryTracker.Inventory inventory;

    public InventoryOpenEvent(InventoryTracker.Inventory inventory) {
        this.inventory = inventory;
    }

    public int getInventoryId() {
        return inventory.windowId;
    }

    public String getInventoryName() {
        return inventory.title;
    }

    public int getInventorySize() {
        return inventory.slotCount;
    }

    public Map<Integer, ItemStack> getInventoryItems() {
        Map<Integer, ItemStack> out = new LinkedHashMap<>();
        for (Map.Entry<Integer, ItemStack> e : inventory.items.entrySet()) {
            ItemStack stack = e.getValue();
            if (stack != null) {
                out.put(e.getKey(), stack);
            }
        }
        return out;
    }

    public static class FullyOpened extends InventoryOpenEvent {
        public FullyOpened(InventoryTracker.Inventory inventory) { super(inventory); }
    }

    public static class Updated extends InventoryOpenEvent {
        public Updated(InventoryTracker.Inventory inventory) { super(inventory); }
    }
}
