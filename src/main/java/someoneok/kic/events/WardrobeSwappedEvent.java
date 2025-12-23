package someoneok.kic.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class WardrobeSwappedEvent extends Event {
    private final int page;
    private final int slot;

    public WardrobeSwappedEvent(int page, int slot) {
        this.page = page;
        this.slot = slot;
    }

    public int getPage() { return page; }
    public int getSlot() { return slot; }
}
