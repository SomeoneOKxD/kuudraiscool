package someoneok.kic.models.data;

import someoneok.kic.models.kuudra.CrimsonFaction;

public class UserData {
    private CrimsonFaction faction;
    private Long kuudraPersonalBest;

    public UserData(CrimsonFaction faction, Long kuudraPersonalBest) {
        this.faction = faction;
        this.kuudraPersonalBest = kuudraPersonalBest;
    }

    public CrimsonFaction getFaction() {
        return faction;
    }

    public void setFaction(CrimsonFaction faction) {
        this.faction = faction;
    }

    public Long getKuudraPersonalBest() {
        return kuudraPersonalBest;
    }

    public void setKuudraPersonalBest(Long kuudraPersonalBest) {
        this.kuudraPersonalBest = kuudraPersonalBest;
    }
}
