package someoneok.kic.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class HypixelJoinEvent extends Event {
    private final boolean isAlpha;

    public HypixelJoinEvent(boolean isAlpha) {
        this.isAlpha = isAlpha;
    }

    public boolean isAlpha() {
        return isAlpha;
    }
}
