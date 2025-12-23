package someoneok.kic.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import someoneok.kic.models.kuudra.pearls.PearlRenderData;

public class ThrowPearlEvent extends Event {
    private final PearlRenderData pearlData;

    public ThrowPearlEvent(PearlRenderData pearlData) {
        this.pearlData = pearlData;
    }

    public PearlRenderData getPearlData() { return pearlData; }
}
