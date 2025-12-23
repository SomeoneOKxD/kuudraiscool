package someoneok.kic.utils.kuudra;

import net.minecraft.util.Vec3;
import someoneok.kic.models.kuudra.PickupSpot;
import someoneok.kic.models.kuudra.pearls.DoublePearl;
import someoneok.kic.models.kuudra.pearls.DoublePearlConfig;
import someoneok.kic.models.kuudra.pearls.DoublePearlDefaults;
import someoneok.kic.modules.kuudra.NoPre;
import someoneok.kic.utils.data.DataHandler;

import java.util.*;

public class DoublePearlRegistry {
    private static final EnumMap<PickupSpot, List<DoublePearl>> byPre = new EnumMap<>(PickupSpot.class);
    private static final Map<String, DoublePearl> activeById = new LinkedHashMap<>();
    private static DoublePearlConfig lastLoaded = new DoublePearlConfig();

    static { for (PickupSpot s : PickupSpot.values()) byPre.put(s, new ArrayList<>()); }

    public static List<DoublePearl> getRoutesFrom(PickupSpot pre) {
        if (pre == null) return Collections.emptyList();
        PickupSpot missing = NoPre.getMissing();
        List<DoublePearl> base = byPre.getOrDefault(pre, Collections.emptyList());
        if (missing == PickupSpot.NONE || base.isEmpty()) return base;
        List<DoublePearl> out = new ArrayList<>(base.size());
        for (DoublePearl r : base) if (r.getDrop() != missing) out.add(r);
        return out;
    }

    public static Collection<DoublePearl> getAllActive() {
        return Collections.unmodifiableCollection(activeById.values());
    }

    public static void upsertCustom(String id, Vec3 location, PickupSpot pre, PickupSpot drop) {
        DoublePearl dp = new DoublePearl(id, location, pre, drop, false);
        DoublePearl prev = activeById.put(id, dp);
        if (prev != null) removeFromByPre(prev);
        byPre.get(pre).add(dp);

        putOrReplaceCustomInConfig(id, location, pre, drop);
        save();
    }

    public static boolean removeCustom(String id) {
        boolean removedCfg = lastLoaded.custom.removeIf(c -> c.id.equals(id));
        boolean removedActive = removeActive(id);
        if (removedCfg || removedActive) save();
        return removedActive;
    }

    public static boolean disableDefault(String defaultId) {
        if (!DoublePearlDefaults.DEFAULTS.containsKey(defaultId)) return false;
        lastLoaded.disabledDefaults.add(defaultId);
        boolean changed = removeActive(defaultId);
        save();
        return changed;
    }

    public static boolean enableDefault(String defaultId) {
        if (!DoublePearlDefaults.DEFAULTS.containsKey(defaultId)) return false;
        boolean changed = lastLoaded.disabledDefaults.remove(defaultId);
        if (changed) {
            addActive(DoublePearlDefaults.DEFAULTS.get(defaultId));
            save();
        }
        return changed;
    }

    public static void resetToDefaults() {
        lastLoaded.custom.clear();
        lastLoaded.disabledDefaults.clear();
        rebuildActiveFromConfig();
        save();
    }

    public static void addAllDefaults() {
        boolean changed = false;
        for (Map.Entry<String, DoublePearl> e : DoublePearlDefaults.DEFAULTS.entrySet()) {
            String id = e.getKey();
            if (!lastLoaded.disabledDefaults.contains(id) && !activeById.containsKey(id)) {
                addActive(e.getValue());
                changed = true;
            }
        }
        if (changed) save();
    }

    public static void load() {
        lastLoaded = DataHandler.readDoublePearlConfig();
        rebuildActiveFromConfig();
    }

    public static void save() {
        DataHandler.writeDoublePearlConfig(lastLoaded);
    }

    private static void rebuildActiveFromConfig() {
        activeById.clear();
        for (List<DoublePearl> list : byPre.values()) list.clear();

        // 1) defaults (skip disabled)
        for (Map.Entry<String, DoublePearl> e : DoublePearlDefaults.DEFAULTS.entrySet()) {
            if (lastLoaded.disabledDefaults.contains(e.getKey())) continue;
            addActive(e.getValue());
        }

        // 2) customs
        for (DoublePearlConfig.CustomEntry c : lastLoaded.custom) {
            PickupSpot pre = PickupSpot.valueOf(c.pre);
            PickupSpot drop = PickupSpot.valueOf(c.drop);
            addActive(new DoublePearl(c.id, new Vec3(c.x, c.y, c.z), pre, drop, false));
        }
    }

    private static void addActive(DoublePearl dp) {
        activeById.put(dp.getId(), dp);
        byPre.get(dp.getPre()).add(dp);
    }

    private static boolean removeActive(String id) {
        DoublePearl prev = activeById.remove(id);
        if (prev == null) return false;
        removeFromByPre(prev);
        return true;
    }

    private static void removeFromByPre(DoublePearl dp) {
        List<DoublePearl> list = byPre.get(dp.getPre());
        if (list != null) list.removeIf(x -> x.getId().equals(dp.getId()));
    }

    private static void putOrReplaceCustomInConfig(String id, Vec3 loc, PickupSpot pre, PickupSpot drop) {
        DoublePearlConfig.CustomEntry entry = null;
        for (DoublePearlConfig.CustomEntry c : lastLoaded.custom) {
            if (c.id.equals(id)) { entry = c; break; }
        }
        if (entry == null) {
            entry = new DoublePearlConfig.CustomEntry();
            entry.id = id;
            lastLoaded.custom.add(entry);
        }
        entry.x = loc.xCoord; entry.y = loc.yCoord; entry.z = loc.zCoord;
        entry.pre = pre.name(); entry.drop = drop.name();
    }
}
