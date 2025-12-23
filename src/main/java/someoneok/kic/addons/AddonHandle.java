package someoneok.kic.addons;

import someoneok.kic.api.ModAddon;

import java.io.File;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AddonHandle {
    public final String id;
    public final ModAddon addon;
    public final URLClassLoader loader;
    public final File sourceJar;
    public final File runtimeJar;
    private final boolean coreMixinAddon;
    private final String bootstrapMixinVersion;
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public AddonHandle(ModAddon addon,
                       URLClassLoader loader,
                       File sourceJar,
                       File runtimeJar,
                       boolean coreMixinAddon,
                       String bootstrapMixinVersion) {
        this.id = addon.getId();
        this.addon = addon;
        this.loader = loader;
        this.sourceJar = sourceJar;
        this.runtimeJar = runtimeJar;
        this.coreMixinAddon = coreMixinAddon;
        this.bootstrapMixinVersion = bootstrapMixinVersion;
    }

    public boolean isCoreMixinAddon() {
        return coreMixinAddon;
    }

    public String getBootstrapMixinVersion() {
        return bootstrapMixinVersion;
    }

    public boolean safeEnable() {
        if (enabled.compareAndSet(false, true)) {
            try {
                addon.onEnable();
                return true;
            } catch (Throwable t) {
                enabled.set(false);
                return false;
            }
        }
        return false;
    }

    public boolean safeDisable() {
        if (enabled.compareAndSet(true, false)) {
            try {
                addon.onDisable();
                return true;
            } catch (Throwable t) {
                return false;
            }
        }
        return false;
    }

    public boolean isEnabled() { return enabled.get(); }
}
