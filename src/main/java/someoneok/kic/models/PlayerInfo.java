package someoneok.kic.models;

public class PlayerInfo {
    private final String username;
    private final String uuid;

    public PlayerInfo(String username, String uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }
}
