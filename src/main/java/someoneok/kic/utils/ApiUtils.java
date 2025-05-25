package someoneok.kic.utils;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import someoneok.kic.commands.KICCommand;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.APIException;
import someoneok.kic.models.ApiRole;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.ws.KICWS;

import java.util.EnumSet;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.NetworkUtils.sendGetRequest;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.StringUtils.isValidUUIDv4;

public class ApiUtils {
    private static final EnumSet<ApiRole> roles = EnumSet.noneOf(ApiRole.class);
    private static boolean verified = false;
    private static boolean apiKeyError = false;
    private static String apiKeyMessage = "";

    public static boolean hasPremium() {
        return roles.contains(ApiRole.PREMIUM);
    }

    public static boolean isDev() {
        return roles.contains(ApiRole.DEV);
    }

    public static boolean isAdmin() {
        return roles.contains(ApiRole.ADMIN);
    }

    public static boolean isBeta() {
        return roles.contains(ApiRole.BETA);
    }

    public static boolean isTester() {
        return roles.contains(ApiRole.TESTER);
    }

    public static boolean isVerified() {
        return verified;
    }

    public static boolean isApiKeyError() {
        return apiKeyError;
    }

    public static String getApiKeyMessage() {
        return apiKeyMessage;
    }

    public static void setApiKeyMessage(String message) {
        apiKeyMessage = message;
    }

    public static void reset() {
        verified = false;
        apiKeyError = false;
        apiKeyMessage = "";
        roles.clear();
    }

    public static void addRole(String roleName) {
        try {
            ApiRole role = ApiRole.valueOf(roleName.toUpperCase());
            roles.add(role);
        } catch (IllegalArgumentException e) {
            KICLogger.info("Error adding role: " + e.getMessage());
        }
    }

    public static void setRoleVariables() {
        if (hasPremium()) {
            KICCommand.addPremiumCommands();
        } else {
            KICCommand.removePremiumCommands();
        }

        disableConfigOptions();
    }

    private static void disableConfigOptions() {
        if (!hasPremium()) {
            KICConfig.partyFinderGuiStats = false;
            KICConfig.kicPlusChat = false;
        }

        if (!isAdmin()) {
            KICConfig.autoPearls = false;
        }

        if (!isTester() || !isBeta() || !isDev()) {
            KICConfig.testerMode = false;
            KICConfig.testerModeLogInChat = false;
        }
    }

    public static void verifyApiKey(boolean manual) {
        KICLogger.info("Verifying API key");
        reset();

        if (isNullOrEmpty(KICConfig.apiKey.trim())) {
            markVerificationFailed("No API key set.", manual);
            return;
        }

        if (!isValidUUIDv4(KICConfig.apiKey.trim())) {
            markVerificationFailed("Invalid API key format!", manual);
            return;
        }

        try {
            JsonObject jsonResponse = JsonUtils.parseString(sendGetRequest("https://api.sm0kez.com/key", true, true)).getAsJsonObject();

            if (jsonResponse == null) {
                markVerificationFailed("There was an error while verifying your API key!", manual);
                return;
            }

            String status = jsonResponse.get("status").getAsString();
            if (!"ACTIVE".equals(status)) {
                markVerificationFailed(String.format("Your API key is %s!", status.toLowerCase()), manual);
                return;
            }

            JsonArray jsonRoles = jsonResponse.getAsJsonArray("roles");
            jsonRoles.forEach(role -> addRole(role.getAsString()));
            setRoleVariables();

            verified = true;
            apiKeyError = false;
            apiKeyMessage = KICPrefix + " §aYour API key has been verified and is active.";
            KICLogger.info("Your API key has been verified and is active.");
            if (manual && !apiKeyMessage.isEmpty()) {
                sendMessageToPlayer(apiKeyMessage);
                apiKeyMessage = "";
            }
            if (LocationUtils.onHypixel) {
                KICWS.connect();
            }
        } catch (APIException e) {
            markVerificationFailed("There was an error while verifying your API key!", manual);
            KICLogger.error(e.getMessage());
        }
    }

    private static void markVerificationFailed(String reason, boolean manual) {
        KICLogger.info("Key Verification failed: " + reason);
        apiKeyError = true;
        verified = false;
        apiKeyMessage = String.format("%s §c%s", KICPrefix, reason);
        if (manual && !apiKeyMessage.isEmpty()) {
            sendMessageToPlayer(apiKeyMessage);
            apiKeyMessage = "";
        }
        setRoleVariables();
    }
}
