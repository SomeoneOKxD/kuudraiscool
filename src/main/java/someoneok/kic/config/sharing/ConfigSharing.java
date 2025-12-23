package someoneok.kic.config.sharing;

import cc.polyfrost.oneconfig.config.annotations.Page;
import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.event.ClickEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.APIException;
import someoneok.kic.models.config.ConfigResponse;
import someoneok.kic.models.request.ShareConfigRequest;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.GeneralUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.apiHost;
import static someoneok.kic.utils.ChatUtils.createHoverAndClickComponent;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;

public class ConfigSharing {
    private static JsonObject buildSharedConfigRecursive(Class<?> clazz, Object instance) {
        JsonObject json = new JsonObject();

        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            boolean isStatic = Modifier.isStatic(modifiers);
            boolean isTransient = Modifier.isTransient(modifiers);
            boolean isPublic = Modifier.isPublic(modifiers);

            if (!isPublic || isTransient || field.isAnnotationPresent(DoNotShare.class)) continue;
            if (!isStatic && instance == null) continue;

            try {
                Object value = field.get(instance);
                if (value == null) continue;

                String fieldName = field.getName();

                if (field.isAnnotationPresent(Page.class)) {
                    JsonObject nested = buildSharedConfigRecursive(field.getType(), value);
                    if (!nested.entrySet().isEmpty()) {
                        json.add(fieldName, nested);
                    }
                } else {
                    json.add(fieldName, KIC.GSON.toJsonTree(value));
                }
            } catch (IllegalAccessException e) {
                KICLogger.error("Failed to access config field: " + field.getName() + " - " + e.getMessage());
            }
        }

        return json;
    }

    private static void applySharedConfigRecursive(Class<?> clazz, Object instance, JsonObject json) {
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            boolean isStatic = Modifier.isStatic(modifiers);
            boolean isTransient = Modifier.isTransient(modifiers);
            boolean isPublic = Modifier.isPublic(modifiers);

            if (!isPublic || isTransient || field.isAnnotationPresent(DoNotShare.class)) continue;
            if (!isStatic && instance == null) continue;

            String fieldName = field.getName();
            if (!json.has(fieldName)) continue;

            try {
                JsonElement element = json.get(fieldName);

                if (field.isAnnotationPresent(Page.class)) {
                    Object nestedInstance = field.get(instance);
                    if (nestedInstance != null && element.isJsonObject()) {
                        applySharedConfigRecursive(field.getType(), nestedInstance, element.getAsJsonObject());
                    }
                } else {
                    Object converted = KIC.GSON.fromJson(element, field.getType());
                    field.set(instance, converted);
                }
            } catch (IllegalAccessException | JsonSyntaxException e) {
                KICLogger.error("Failed to apply config field: " + fieldName + " - " + e.getMessage());
            }
        }
    }

    private static void applySharedConfig(JsonObject json) {
        applySharedConfigRecursive(KICConfig.class, null, json);
    }

    private static String exportCompressedJsonConfig() {
        JsonObject json = buildSharedConfigRecursive(KICConfig.class, null);
        String jsonText = KIC.GSON.toJson(json);
        try {
            return GeneralUtils.compressJson(jsonText);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean importCompressedJsonConfig(String jsonText) {
        String decompressedJson;
        try {
            decompressedJson = GeneralUtils.decompressJson(jsonText);
        } catch (IOException e) {
            return false;
        }
        try {
            JsonObject json = JsonUtils.parseString(decompressedJson).getAsJsonObject();
            applySharedConfig(json);
            return true;
        } catch (Exception e) {
            KICLogger.error("Invalid config JSON import: " + e.getMessage());
            return false;
        }
    }

    public static void shareConfig(Runnable callback) {
        if (!ApiUtils.isVerified()) return;
        String compressedConfig = exportCompressedJsonConfig();
        if (compressedConfig == null) {
            sendMessageToPlayer(KIC.KICPrefix + " §cThere was an error while sharing your config.");
            if (callback != null) callback.run();
            return;
        }

        Multithreading.runAsync(() -> {
            ShareConfigRequest shareConfigRequest = new ShareConfigRequest(compressedConfig, true);
            String requestBody = KIC.GSON.toJson(shareConfigRequest);
            try {
                String configId = NetworkUtils.sendPostRequest(apiHost() + "/config/share", true, requestBody);

                sendMessageToPlayer(createHoverAndClickComponent(
                        true,
                        String.format("%s §aYour config has been shared successfully! §7(ID: §2§l%s§r§7)", KICPrefix, configId),
                        "§7Click to suggest the import command:\n§f/kic importconfig " + configId,
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/kic importconfig " + configId
                ));
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            } catch (Exception e) {
                sendMessageToPlayer(KICPrefix + " §cUnexpected error during config sharing.");
                KICLogger.error("Config sharing error: " + e.getMessage());
            }
            if (callback != null) callback.run();
        });
    }

    public static void importConfig(ConfigResponse config) {
        if (config == null) return;
        String username = config.getUsername();
        String configStr = config.getConfig();

        boolean success = importCompressedJsonConfig(configStr);
        if (success) {
            sendMessageToPlayer(String.format(
                    "%s §aSuccessfully imported §2§l%s§r§a's §aconfig.",
                    KICPrefix, username));
        } else {
            sendMessageToPlayer(KICPrefix + " §cFailed to import the config.");
        }
    }

    public static void importConfig(String id) {
        if (!ApiUtils.isVerified()) return;
        Multithreading.runAsync(() -> {
            try {
                String response = NetworkUtils.sendGetRequest(apiHost() + "/config/" + id, true);
                JsonObject configJson = JsonUtils.parseString(response).getAsJsonObject();

                if (configJson == null || !configJson.has("config")) {
                    sendMessageToPlayer(KICPrefix + " §cInvalid or incomplete config received.");
                    return;
                }

                String username = configJson.has("username") ? configJson.get("username").getAsString() : "Unknown";
                String config = configJson.get("config").getAsString();

                boolean success = importCompressedJsonConfig(config);
                if (success) {
                    sendMessageToPlayer(String.format(
                            "%s §aSuccessfully imported §2§l%s§r§a's §aconfig.",
                            KICPrefix, username));
                } else {
                    sendMessageToPlayer(KICPrefix + " §cFailed to import the config.");
                }
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            } catch (Exception e) {
                sendMessageToPlayer(KICPrefix + " §cUnexpected error during config import.");
                KICLogger.error("Config import error: " + e.getMessage());
            }
        });
    }

    public static void myConfigs() {
        if (!ApiUtils.isVerified()) return;
        Multithreading.runAsync(() -> {
            try {
                String response = NetworkUtils.sendGetRequest(apiHost() + "/config/my", true);
                JsonArray configs = JsonUtils.parseString(response).getAsJsonArray();

                if (configs.isJsonNull() || configs.size() == 0) {
                    sendMessageToPlayer(KICPrefix + " §aYou don't have any shared configs yet. Use §e/kic shareconfig §ato share one.");
                    return;
                }

                sendMessageToPlayer(KICPrefix + " §aYour shared configs:");
                for (JsonElement el : configs) {
                    JsonObject obj = el.getAsJsonObject();
                    String configId = obj.get("configId").getAsString();
                    boolean hidden = obj.get("hidden").getAsBoolean();
                    long timestamp = obj.get("timestamp").getAsLong();

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));
                    String text = String.format(" §b• §2%s §7(%s) §7(%s§7)", configId, formattedDate, (hidden ? "§cPRIVATE" : "§aPUBLIC"));

                    sendMessageToPlayer(createHoverAndClickComponent(
                            true,
                            text,
                            "§aClick to import this config",
                            ClickEvent.Action.SUGGEST_COMMAND,
                            "/kic importconfig " + configId
                    ));
                }
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            } catch (Exception e) {
                sendMessageToPlayer(KICPrefix + " §cUnexpected error while getting your configs.");
                KICLogger.error("Config loading error: " + e.getMessage());
            }
        });
    }

    public static void deleteConfig(String id) {
        if (!ApiUtils.isVerified()) return;
        Multithreading.runAsync(() -> {
            try {
                NetworkUtils.sendDeleteRequest(apiHost() + "/config/delete/" + id, true);
                sendMessageToPlayer(String.format(
                        "%s §aSuccessfully deleted your config. §7(ID: §2§l%s§r§7)",
                        KICPrefix, id));
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            } catch (Exception e) {
                sendMessageToPlayer(KICPrefix + " §cUnexpected error while getting deleting your config.");
                KICLogger.error("Config delete error: " + e.getMessage());
            }
        });
    }
}
