package someoneok.kic.utils.overlay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import someoneok.kic.models.UserData;
import someoneok.kic.models.overlay.OverlayLocationData;
import someoneok.kic.models.overlay.ProfitTrackerData;
import someoneok.kic.utils.dev.KICLogger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static someoneok.kic.utils.overlay.OverlayManager.getOverlay;
import static someoneok.kic.utils.overlay.OverlayManager.getOverlays;

public class OverlayDataHandler {
    private static final File OVERLAYS_CONFIG_FILE = new File("config/kuudraiscool/overlays.json");
    private static final File OVERLAYS_DATA_CONFIG_FILE = new File("config/kuudraiscool/overlays_data.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveOverlays() {
        try {
            if (!OVERLAYS_CONFIG_FILE.exists()) {
                OVERLAYS_CONFIG_FILE.getParentFile().mkdirs();
                OVERLAYS_CONFIG_FILE.createNewFile();
            }

            try (Writer writer = new FileWriter(OVERLAYS_CONFIG_FILE)) {
                Map<String, OverlayLocationData> data = new HashMap<>();
                for (MovableOverlay overlay : getOverlays()) {
                    data.put(overlay.name, new OverlayLocationData(overlay.x, overlay.y, overlay.scale));
                }
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            KICLogger.error(e.getMessage());
        }
    }

    public static void loadOverlays() {
        if (!OVERLAYS_CONFIG_FILE.exists()) return;

        try (Reader reader = new FileReader(OVERLAYS_CONFIG_FILE)) {
            Type type = new TypeToken<Map<String, OverlayLocationData>>() {}.getType();
            Map<String, OverlayLocationData> data = GSON.fromJson(reader, type);

            for (Map.Entry<String, OverlayLocationData> entry : data.entrySet()) {
                MovableOverlay overlay = getOverlay(entry.getKey());
                if (overlay != null) {
                    int x = entry.getValue().getX();
                    int y = entry.getValue().getY();
                    double scale = entry.getValue().getScale();

                    overlay.x = x;
                    overlay.y = y;
                    overlay.scale = scale;

                    if (overlay instanceof DualOverlay) {
                        ((DualOverlay) overlay).setPositionAndScale(x, y, scale);
                    } else {
                        overlay.updateScale();
                    }
                }
            }
        } catch (IOException e) {
            KICLogger.error(e.getMessage());
        }
    }

    public static void saveOverlaysData() {
        try {
            if (!OVERLAYS_DATA_CONFIG_FILE.exists()) {
                OVERLAYS_DATA_CONFIG_FILE.getParentFile().mkdirs();
                OVERLAYS_DATA_CONFIG_FILE.createNewFile();
            }

            DataWrapper dataWrapper = new DataWrapper(
                    OverlayDataManager.getProfitTrackerData(),
                    OverlayDataManager.getUserData()
            );

            try (Writer writer = new FileWriter(OVERLAYS_DATA_CONFIG_FILE)) {
                GSON.toJson(dataWrapper, writer);
            }
        } catch (IOException e) {
            KICLogger.error("Failed to save overlay data: " + e.getMessage());
        }
    }

    public static void loadOverlaysData() {
        if (!OVERLAYS_DATA_CONFIG_FILE.exists()) return;

        try (Reader reader = new FileReader(OVERLAYS_DATA_CONFIG_FILE)) {
            DataWrapper dataWrapper = GSON.fromJson(reader, DataWrapper.class);
            if (dataWrapper != null) {
                OverlayDataManager.setProfitTrackerData(dataWrapper.profitTrackerData);
                OverlayDataManager.setUserData(dataWrapper.userData);
            }
        } catch (IOException e) {
            KICLogger.error("Failed to load overlay data: " + e.getMessage());
        }
    }

    private static class DataWrapper {
        private final ProfitTrackerData profitTrackerData;
        private final UserData userData;

        public DataWrapper(ProfitTrackerData profitTrackerData, UserData userData) {
            this.profitTrackerData = profitTrackerData;
            this.userData = userData;
        }
    }
}
