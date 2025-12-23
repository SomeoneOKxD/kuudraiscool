package someoneok.kic.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.events.InventoryOpenEvent;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.events.PacketEvent;

import java.util.HashMap;
import java.util.Map;

public class InventoryTracker {
    private Inventory currentInventory = null;
    private boolean acceptItems = false;
    private InventoryOpenEvent.Updated lateEvent = null;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (lateEvent != null) {
            KICEventBus.post(lateEvent);
            lateEvent = null;
        }
    }

    @SubscribeEvent
    public void onInventoryDataReceiveEvent(PacketEvent.Received event) {
        if (event.getPacket() instanceof S2DPacketOpenWindow) {
            S2DPacketOpenWindow p = (S2DPacketOpenWindow) event.getPacket();

            final String title = p.getWindowTitle().getUnformattedText();
            final int windowId = p.getWindowId();
            final int slotCount = p.getSlotCount();

            currentInventory = new Inventory(windowId, title, slotCount);
            acceptItems = true;
            return;
        }

        if (event.getPacket() instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot p = (S2FPacketSetSlot) event.getPacket();

            if (!acceptItems) {
                if (currentInventory == null) return;
                if (currentInventory.windowId != p.func_149175_c()) return;

                int slot = p.func_149173_d();
                if (slot < currentInventory.slotCount) {
                    ItemStack stack = p.func_149174_e();
                    if (stack != null) {
                        currentInventory.items.put(slot, stack);
                        lateEvent = new InventoryOpenEvent.Updated(currentInventory);
                    }
                }
                return;
            }

            if (currentInventory == null) return;
            if (currentInventory.windowId != p.func_149175_c()) return;

            int slot = p.func_149173_d();
            if (slot < currentInventory.slotCount) {
                ItemStack stack = p.func_149174_e();
                if (stack != null) {
                    currentInventory.items.put(slot, stack);
                }
            } else {
                done(currentInventory);
                return;
            }

            if (currentInventory.items.size() == currentInventory.slotCount) {
                done(currentInventory);
            }
        }
    }

    private void done(Inventory inventory) {
        KICEventBus.post(new InventoryOpenEvent.FullyOpened(inventory));
        inventory.fullyOpenedOnce = true;
        KICEventBus.post(new InventoryOpenEvent.Updated(inventory));
        acceptItems = false;
    }

    public static final class Inventory {
        public final int windowId;
        public final String title;
        public final int slotCount;
        public final Map<Integer, ItemStack> items;
        public boolean fullyOpenedOnce;

        public Inventory(int windowId, String title, int slotCount) {
            this.windowId = windowId;
            this.title = title;
            this.slotCount = slotCount;
            this.items = new HashMap<>();
            this.fullyOpenedOnce = false;
        }
    }
}
