package someoneok.kic.models.request;

import someoneok.kic.KIC;

public class ShareConfigRequest {
    private final String config;
    private final String modVersion;
    private final boolean hidden;

    public ShareConfigRequest(String config, boolean hidden) {
        this.config = config;
        this.modVersion = KIC.VERSION;
        this.hidden = hidden;
    }
}
