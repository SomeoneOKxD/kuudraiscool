package someoneok.kic.events;

import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import someoneok.kic.models.misc.TitleType;

public class TitleEvent extends Event {
    private final TitleType type;
    private final IChatComponent component;

    public TitleEvent(S45PacketTitle.Type type, IChatComponent component) {
        this.type = fromVanillaType(type);
        this.component = component;
    }

    public TitleType getType() {
        return type;
    }

    public IChatComponent getComponent() {
        return component;
    }

    @Cancelable
    public static class Incoming extends TitleEvent {
        public Incoming(S45PacketTitle.Type type, IChatComponent component) {
            super(type, component);
        }
    }

    private TitleType fromVanillaType(S45PacketTitle.Type vanilla) {
        switch (vanilla) {
            case TITLE: return TitleType.TITLE;
            case SUBTITLE: return TitleType.SUBTITLE;
            case TIMES: return TitleType.TIMES;
            case CLEAR: return TitleType.CLEAR;
            case RESET: return TitleType.RESET;
            default: throw new IllegalArgumentException("Unknown title type: " + vanilla);
        }
    }
}
