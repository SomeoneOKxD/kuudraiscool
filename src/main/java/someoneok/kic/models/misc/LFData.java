package someoneok.kic.models.misc;

import someoneok.kic.models.PlayerInfo;

import java.util.List;

public class LFData {
    private final PlayerInfo playerInfo;
    private final List<LFItemData> result;

    public LFData(PlayerInfo playerInfo, List<LFItemData> result) {
        this.playerInfo = playerInfo;
        this.result = result;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public List<LFItemData> getResult() {
        return result;
    }
}
