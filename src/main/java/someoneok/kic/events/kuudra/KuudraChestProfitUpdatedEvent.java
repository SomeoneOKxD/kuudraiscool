package someoneok.kic.events.kuudra;

import net.minecraftforge.fml.common.eventhandler.Event;
import someoneok.kic.models.kuudra.chest.KuudraChest;

public class KuudraChestProfitUpdatedEvent extends Event {
    private final KuudraChest chest;
    private final boolean inInstance;

    public KuudraChestProfitUpdatedEvent(KuudraChest chest, boolean inInstance) {
        this.chest = chest;
        this.inInstance = inInstance;
    }

    public KuudraChest getChest() { return chest; }
    public boolean isInInstance() { return inInstance; }
}
