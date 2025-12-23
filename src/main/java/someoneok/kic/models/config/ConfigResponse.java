package someoneok.kic.models.config;

public class ConfigResponse {
    private final String configId;
    private final String username;
    private final String config;
    private final String modVersion;
    private boolean hidden;
    private final long timestamp;

    public ConfigResponse(String configId, String username, String config, String modVersion, boolean hidden, long timestamp) {
        this.configId = configId;
        this.username = username;
        this.config = config;
        this.modVersion = modVersion;
        this.hidden = hidden;
        this.timestamp = timestamp;
    }

    public String getConfigId() { return configId; }
    public String getUsername() { return username; }
    public String getConfig() { return config; }
    public String getModVersion() { return modVersion; }
    public boolean isHidden() { return hidden; }
    public long getTimestamp() { return timestamp; }

    public void setHidden(boolean hidden) { this.hidden = hidden; }
}
