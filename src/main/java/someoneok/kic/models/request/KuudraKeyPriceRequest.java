package someoneok.kic.models.request;

import someoneok.kic.models.kuudra.CrimsonFaction;
import someoneok.kic.models.kuudra.chest.KuudraKey;

public class KuudraKeyPriceRequest implements Request  {
    private final String type;
    private final KuudraKey tier;
    private final CrimsonFaction faction;

    public KuudraKeyPriceRequest(KuudraKey tier, CrimsonFaction faction) {
        this.type = "KEY";
        this.tier = tier;
        this.faction = faction == CrimsonFaction.NONE ? CrimsonFaction.MAGE : faction;
    }
}
