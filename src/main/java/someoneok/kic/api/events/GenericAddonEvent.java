package someoneok.kic.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class GenericAddonEvent extends Event {
    private final String channel;
    private final Object payload;

    public GenericAddonEvent(String channel, Object payload) {
        this.channel = channel;
        this.payload = payload;
    }

    public String getChannel() { return channel; }
    public Object getPayload() { return payload; }
}
