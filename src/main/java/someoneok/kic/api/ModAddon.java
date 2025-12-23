package someoneok.kic.api;

public interface ModAddon {
    String getId();
    String getVersion();
    String getAddonApiVersion();
    String getName();

    default Class<?> getConfigPageClass() { return null; }
    default String getConfigPersistenceId() { return getId(); }

    void onLoad(AddonContext ctx) throws Exception;
    void onEnable() throws Exception;
    void onDisable() throws Exception;
    boolean isActive();
}
