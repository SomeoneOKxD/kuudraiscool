package someoneok.kic.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class LocationUpdateEvent extends Event {
    private someoneok.kic.models.Island island;
    private String serverName;
    private String subArea;

    protected LocationUpdateEvent(someoneok.kic.models.Island island, String serverName, String subArea) {
        this.island = island;
        this.serverName = serverName;
        this.subArea = subArea;
    }

    public static class Island extends LocationUpdateEvent {
        public Island(someoneok.kic.models.Island island, String serverName) {
            super(island, serverName, null);
        }
    }

    public static class SubArea extends LocationUpdateEvent {
        public SubArea(someoneok.kic.models.Island island, String serverName, String subArea) {
            super(island, serverName, subArea);
        }
    }

    public someoneok.kic.models.Island getIsland() {
        return island;
    }

    public String getServerName() {
        return serverName;
    }

    public String getSubArea() {
        return subArea;
    }
}
