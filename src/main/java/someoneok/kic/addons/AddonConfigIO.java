package someoneok.kic.addons;

import cc.polyfrost.oneconfig.config.annotations.Page;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import someoneok.kic.KIC;
import someoneok.kic.utils.dev.KICLogger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class AddonConfigIO {
    public static void loadStaticPage(String addonId, Class<?> clazz) {
        File f = AddonHelpers.configFileForAddon(addonId);
        if (!f.exists()) return;
        try (Reader r = new InputStreamReader(Files.newInputStream(f.toPath()), StandardCharsets.UTF_8)) {
            JsonObject json = new JsonParser().parse(r).getAsJsonObject();
            applyRecursive(clazz, null, json);
        } catch (Throwable t) {
            KICLogger.warn("Failed to load addon config " + f.getName() + ": " + t.getMessage());
        }
    }

    public static void saveStaticPage(String addonId, Class<?> clazz) {
        File f = AddonHelpers.configFileForAddon(addonId);
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            JsonObject json = dumpRecursive(clazz, null);
            KIC.PRETTY_GSON.toJson(json, w);
        } catch (Throwable t) {
            KICLogger.warn("Failed to save addon config " + f.getName() + ": " + t.getMessage());
        }
    }

    public static void saveAllAddons() {
        AddonRegistry.addons().forEach(addon -> {
            Class<?> pageKlass = addon.getConfigPageClass();
            if (pageKlass != null) saveStaticPage(addon.getConfigPersistenceId(), pageKlass);
        });
    }

    private static JsonObject dumpRecursive(Class<?> clazz, Object instance) {
        JsonObject json = new JsonObject();
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            boolean isStatic = Modifier.isStatic(modifiers);
            boolean isTransient = Modifier.isTransient(modifiers);
            boolean isPublic = Modifier.isPublic(modifiers);

            if (!isPublic || isTransient) continue;
            if (!isStatic && instance == null) continue;

            try {
                Object value = field.get(instance);
                if (value == null) continue;

                String fieldName = field.getName();

                if (field.isAnnotationPresent(Page.class)) {
                    JsonObject nested = dumpRecursive(field.getType(), value);
                    if (!nested.entrySet().isEmpty()) json.add(fieldName, nested);
                } else {
                    json.add(fieldName, KIC.GSON.toJsonTree(value));
                }
            } catch (IllegalAccessException e) {
                KICLogger.error("Failed to read field: " + field.getName() + " - " + e.getMessage());
            }
        }
        return json;
    }

    private static void applyRecursive(Class<?> clazz, Object instance, JsonObject json) {
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            boolean isStatic = Modifier.isStatic(modifiers);
            boolean isTransient = Modifier.isTransient(modifiers);
            boolean isPublic = Modifier.isPublic(modifiers);

            if (!isPublic || isTransient) continue;
            if (!isStatic && instance == null) continue;

            String fieldName = field.getName();
            if (!json.has(fieldName)) continue;

            try {
                JsonElement element = json.get(fieldName);

                if (field.isAnnotationPresent(Page.class)) {
                    Object nestedInstance = field.get(instance);
                    if (nestedInstance != null && element.isJsonObject()) {
                        applyRecursive(field.getType(), nestedInstance, element.getAsJsonObject());
                    }
                } else {
                    Object converted = KIC.GSON.fromJson(element, field.getType());
                    field.set(instance, converted);
                }
            } catch (Exception e) {
                KICLogger.error("Failed to apply field: " + fieldName + " - " + e.getMessage());
            }
        }
    }
}
