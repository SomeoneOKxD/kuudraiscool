package someoneok.kic.utils.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import someoneok.kic.addons.AddonHelpers;
import someoneok.kic.models.data.EmptySlotData;
import someoneok.kic.models.data.OverlayLocationData;
import someoneok.kic.models.data.ProfitTrackerData;
import someoneok.kic.models.data.UserData;
import someoneok.kic.models.kuudra.pearls.DoublePearlConfig;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.DualOverlay;
import someoneok.kic.utils.overlay.MovableOverlay;
import someoneok.kic.utils.overlay.OverlayManager;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DataHandler {
    private static final File OVERLAYS_CONFIG_FILE = new File("config/kuudraiscool/overlays.json");
    private static final File DATA_CONFIG_FILE = new File("config/kuudraiscool/overlays_data.json");
    private static final File DOUBLE_PEARLS_FILE = new File("config/kuudraiscool/double_pearls.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static synchronized void saveOverlays() {
        Map<String, Map<String, OverlayLocationData>> updatesByOwner = new HashMap<>();
        for (MovableOverlay o : OverlayManager.getOverlays()) {
            String owner = OverlayManager.getOwnerAddonId(o.getName());
            updatesByOwner
                    .computeIfAbsent(owner, __ -> new HashMap<>())
                    .put(o.getName(), new OverlayLocationData(o.getX(), o.getY(), o.getScale()));
        }

        for (Map.Entry<String, Map<String, OverlayLocationData>> e : updatesByOwner.entrySet()) {
            String owner = e.getKey();
            File f = (owner == null) ? OVERLAYS_CONFIG_FILE : AddonHelpers.overlayFileForAddon(owner);

            Map<String, OverlayLocationData> existing = readOverlayFile(f);
            existing.putAll(e.getValue());

            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            writeOverlayFile(f, existing);
        }
    }

    public static void loadOverlays() {
        if (!OVERLAYS_CONFIG_FILE.exists()) return;
        Map<String, OverlayLocationData> data = readOverlayFile(OVERLAYS_CONFIG_FILE);
        applyOverlayPositions(data);
    }

    public static void saveOverlaysForAddon(String addonId) {
        File f = AddonHelpers.overlayFileForAddon(addonId);

        Map<String, OverlayLocationData> existing = readOverlayFile(f);

        for (MovableOverlay overlay : OverlayManager.getOverlays()) {
            String owner = OverlayManager.getOwnerAddonId(overlay.getName());
            if (addonId.equals(owner)) {
                existing.put(overlay.getName(),
                        new OverlayLocationData(overlay.getX(), overlay.getY(), overlay.getScale()));
            }
        }

        writeOverlayFile(f, existing);
    }

    public static void loadOverlaysForAddon(String addonId) {
        File f = AddonHelpers.overlayFileForAddon(addonId);
        if (!f.exists()) return;

        Map<String, OverlayLocationData> data = readOverlayFile(f);
        applyOverlayPositions(data);
    }

    public static void saveData() {
        try {
            if (!DATA_CONFIG_FILE.exists()) {
                DATA_CONFIG_FILE.getParentFile().mkdirs();
                DATA_CONFIG_FILE.createNewFile();
            }

            DataWrapper dataWrapper = new DataWrapper(
                    DataManager.getProfitTrackerData(),
                    DataManager.getUserData(),
                    DataManager.getEmptySlotData()
            );

            try (Writer writer = new FileWriter(DATA_CONFIG_FILE)) {
                GSON.toJson(dataWrapper, writer);
            }
        } catch (IOException e) {
            KICLogger.error("Failed to save overlay data: " + e.getMessage());
        }
    }

    public static void loadData() {
        if (!DATA_CONFIG_FILE.exists()) return;

        try (Reader reader = new FileReader(DATA_CONFIG_FILE)) {
            DataWrapper dataWrapper = GSON.fromJson(reader, DataWrapper.class);
            if (dataWrapper != null) {
                DataManager.setProfitTrackerData(dataWrapper.profitTrackerData);
                DataManager.setUserData(dataWrapper.userData);
                DataManager.setEmptySlotData(dataWrapper.emptySlotData != null ? dataWrapper.emptySlotData : new EmptySlotData());
            }
        } catch (IOException e) {
            KICLogger.error("Failed to load overlay data: " + e.getMessage());
        }
    }

    private static Map<String, OverlayLocationData> readOverlayFile(File f) {
        try {
            if (!f.exists()) return new HashMap<>();
            try (Reader r = new FileReader(f)) {
                Type type = new TypeToken<Map<String, OverlayLocationData>>() {}.getType();
                Map<String, OverlayLocationData> map = GSON.fromJson(r, type);
                return map != null ? map : new HashMap<>();
            }
        } catch (IOException e) {
            KICLogger.error("Failed to read overlays file " + f.getName() + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    private static void writeOverlayFile(File f, Map<String, OverlayLocationData> data) {
        try {
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            try (Writer w = new FileWriter(f)) {
                GSON.toJson(data, w);
            }
        } catch (IOException e) {
            KICLogger.error("Failed to write overlays file " + f.getName() + ": " + e.getMessage());
        }
    }

    private static void applyOverlayPositions(Map<String, OverlayLocationData> data) {
        for (Map.Entry<String, OverlayLocationData> entry : data.entrySet()) {
            MovableOverlay overlay = OverlayManager.getOverlay(entry.getKey());
            if (overlay == null) continue;

            OverlayLocationData loc = entry.getValue();
            int x = loc.getX();
            int y = loc.getY();
            double scale = loc.getScale();

            overlay.setX(x);
            overlay.setY(y);
            overlay.setScale(scale);

            if (overlay instanceof DualOverlay) {
                ((DualOverlay) overlay).setPositionAndScale(x, y, scale);
            } else {
                overlay.updateScale();
            }
        }
    }

    public static DoublePearlConfig readDoublePearlConfig() {
        try {
            if (!DOUBLE_PEARLS_FILE.exists()) {
                return new DoublePearlConfig();
            }
            try (Reader r = new FileReader(DOUBLE_PEARLS_FILE)) {
                DoublePearlConfig cfg = GSON.fromJson(r, DoublePearlConfig.class);
                return (cfg != null) ? cfg : new DoublePearlConfig();
            }
        } catch (IOException e) {
            KICLogger.error("Failed to read double pearls file " + DOUBLE_PEARLS_FILE.getName() + ": " + e.getMessage());
            return new DoublePearlConfig();
        }
    }

    public static void writeDoublePearlConfig(DoublePearlConfig cfg) {
        try {
            if (!DOUBLE_PEARLS_FILE.exists()) {
                File parent = DOUBLE_PEARLS_FILE.getParentFile();
                if (parent != null) parent.mkdirs();
                DOUBLE_PEARLS_FILE.createNewFile();
            }
            try (Writer w = new FileWriter(DOUBLE_PEARLS_FILE)) {
                GSON.toJson(cfg, w);
            }
        } catch (IOException e) {
            KICLogger.error("Failed to write double pearls file " + DOUBLE_PEARLS_FILE.getName() + ": " + e.getMessage());
        }
    }

    private static class DataWrapper {
        private final ProfitTrackerData profitTrackerData;
        private final UserData userData;
        private final EmptySlotData emptySlotData;

        public DataWrapper(ProfitTrackerData profitTrackerData, UserData userData, EmptySlotData emptySlotData) {
            this.profitTrackerData = profitTrackerData;
            this.userData = userData;
            this.emptySlotData = emptySlotData;
        }
    }
}
