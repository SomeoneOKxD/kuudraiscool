package someoneok.kic.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;

public class KICEventBus {
    public static final EventBus BUS = new EventBus();

    public static boolean post(Event e) { return BUS.post(e); }
}
