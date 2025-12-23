package someoneok.kic.addons;

import someoneok.kic.api.ModAddon;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;

import static someoneok.kic.addons.AddonHelpers.ADDON_DIR;

public final class AddonRegistry {
    private static final Map<String, AddonHandle> byId = new HashMap<>();
    private static final Set<Object> caps = new HashSet<>();

    private static final Map<String, IncompatibleAddon> incompatibleById = new HashMap<>();

    public static synchronized boolean register(AddonHandle handle) {
        if (byId.containsKey(handle.id)) return false;
        byId.put(handle.id, handle);
        caps.add(handle.addon);
        return true;
    }

    public static synchronized void registerIncompatible(AddonHandle handle, String reason, String hostApi, String addonConstraint) {
        if (!byId.containsKey(handle.id)) incompatibleById.put(handle.id, new IncompatibleAddon(handle, reason, hostApi, addonConstraint));
    }

    public static synchronized AddonHandle get(String id) { return byId.get(id); }
    public static synchronized Set<String> ids() { return Collections.unmodifiableSet(byId.keySet()); }
    public static synchronized Collection<AddonHandle> handles() { return Collections.unmodifiableCollection(byId.values()); }
    public static synchronized Collection<IncompatibleAddon> incompatibles() { return Collections.unmodifiableCollection(incompatibleById.values()); }
    public static boolean noAddons() { return caps.isEmpty(); }

    public static synchronized Collection<ModAddon> addons() {
        List<ModAddon> list = new ArrayList<>();
        for (AddonHandle h : byId.values()) list.add(h.addon);
        return Collections.unmodifiableList(list);
    }

    public static <T> Optional<T> getCapabilityById(String id, Class<T> type) {
        AddonHandle handle = byId.get(id);
        if (handle == null || !handle.isEnabled()) return Optional.empty();
        ModAddon addon = handle.addon;
        if (type.isInstance(addon)) return Optional.of(type.cast(addon));
        return Optional.empty();
    }

    public static synchronized void disableAll() {
        for (AddonHandle h : byId.values()) h.safeDisable();
    }

    public static synchronized boolean exists(String id) {
        AddonHandle h = byId.get(id);
        return h != null;
    }

    public static synchronized boolean isEnabled(String id) {
        AddonHandle h = byId.get(id);
        if (h == null) return false;
        return h.isEnabled();
    }

    public static synchronized boolean disable(String id) {
        AddonHandle h = byId.get(id); if (h == null) return false;
        return h.safeDisable();
    }

    public static synchronized boolean enable(String id) {
        AddonHandle h = byId.get(id); if (h == null) return false;
        return h.safeEnable();
    }

    public static synchronized AddonHandle remove(String id) {
        AddonHandle h = byId.get(id);
        if (h == null) return null;

        if (h.isCoreMixinAddon()) {
            KICLogger.warn("Cannot remove core mixin addon '" + id + "' at runtime.");
            return h;
        }

        if (h.isEnabled()) h.safeDisable();
        byId.remove(id);
        byId.remove(id);
        caps.remove(h.addon);
        incompatibleById.remove(id);
        return h;
    }

    public static synchronized void shutdownAll() {
        for (AddonHandle h : new ArrayList<>(byId.values())) {
            savePage(h.addon);
            h.safeDisable();
            try { AddonLoader.unload(h); } catch (Throwable ignored) {}
        }
        try { AddonLoader.cleanupOrphanShadows(ADDON_DIR); } catch (Throwable ignored) {}

        byId.clear();
        caps.clear();
        incompatibleById.clear();
    }

    public static synchronized void logLoadedAddons() {
        Collection<AddonHandle> list = handles();
        if (list.isEmpty()) {
            KICLogger.forceInfo("Loaded addons: none");
        } else {
            StringBuilder sb = new StringBuilder("Loaded addons (").append(list.size()).append("): ");
            int i = 0, n = list.size();
            for (AddonHandle h : list) {
                ModAddon a = h.addon;
                sb.append(a.getId()).append("@").append(a.getVersion())
                        .append(" (api ").append(a.getAddonApiVersion()).append(")");
                if (!a.isActive()) sb.append(" (disabled)");
                if (++i < n) sb.append(", ");
            }
            KICLogger.forceInfo(sb.toString());
        }

        if (!incompatibleById.isEmpty()) {
            StringBuilder sbBad = new StringBuilder("Incompatible addons (")
                    .append(incompatibleById.size()).append("): ");
            int i = 0, n = incompatibleById.size();
            for (IncompatibleAddon ia : incompatibleById.values()) {
                ModAddon a = ia.handle.addon;
                sbBad.append(a.getId()).append("@").append(a.getVersion())
                        .append(" (api ").append(a.getAddonApiVersion()).append(")");
                if (ia.hostApi != null || ia.addonConstraint != null) {
                    sbBad.append(" [hostApi=").append(ia.hostApi)
                            .append(", requires=").append(ia.addonConstraint).append("]");
                }
                sbBad.append(" - ").append(ia.reason == null ? "no reason" : ia.reason);
                if (++i < n) sbBad.append("; ");
            }
            KICLogger.forceWarn(sbBad.toString());
        }
    }

    private static void savePage(ModAddon addon) {
        Class<?> pageKlass = addon.getConfigPageClass();
        if (pageKlass != null) AddonConfigIO.saveStaticPage(addon.getConfigPersistenceId(), pageKlass);
    }

    public static final class IncompatibleAddon {
        public final AddonHandle handle;
        public final String reason;
        public final String hostApi;
        public final String addonConstraint;

        public IncompatibleAddon(AddonHandle handle, String reason,
                                 String hostApi, String addonConstraint) {
            this.handle = handle;
            this.reason = reason;
            this.hostApi = hostApi;
            this.addonConstraint = addonConstraint;
        }
    }
}
