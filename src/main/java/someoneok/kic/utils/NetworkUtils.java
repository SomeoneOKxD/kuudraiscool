package someoneok.kic.utils;

import org.apache.commons.io.IOUtils;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.APIException;
import someoneok.kic.utils.dev.KICLogger;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;

import static someoneok.kic.utils.PlayerUtils.getPlayerName;

public class NetworkUtils {
    private static InputStream setupConnection(String url, boolean requiresKey, String method, String requestBody) throws IOException, APIException {
        if (requiresKey && KICConfig.apiKey.isEmpty()) {
            throw new APIException("No API key set!", 0);
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        if (url.contains("api.sm0kez.com")) {
            if (KIC.CUSTOM_SSL_CONTEXT != null && connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(KIC.CUSTOM_SSL_CONTEXT.getSocketFactory());
            }
            connection.setRequestProperty("KIC-Version", KIC.VERSION);
            connection.setRequestProperty("IGN", getPlayerName());

            if (requiresKey) {
                connection.setRequestProperty("API-Key", KICConfig.apiKey.trim());
            }
        }

        connection.setRequestMethod(method);
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(5000);
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent", "kuudraiscool");
        connection.setRequestProperty("Content-Type", "application/json");

        if ("POST".equalsIgnoreCase(method) && requestBody != null) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();

        if (responseCode >= 200 && responseCode < 300) {
            return connection.getInputStream();
        } else {
            String errorMessage = "Unknown error encountered.";
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                errorMessage = IOUtils.toString(errorStream, StandardCharsets.UTF_8);
            }

            if (errorMessage.startsWith("<!DOCTYPE html>") || errorMessage.startsWith("<html>") ||
                    (responseCode >= 520 && responseCode <= 526)) {
                throw new APIException("API is unreachable. It may be offline or blocked by Cloudflare.", responseCode);
            }

            switch (responseCode) {
                case 400:
                case 404:
                    throw new APIException(errorMessage, responseCode);
                case 401:
                case 403:
                    throw new APIException("Access denied. Reason: " + errorMessage, responseCode);
                case 429:
                    String resetHeader = connection.getHeaderField("X-RateLimit-Reset");
                    String hardResetHeader = connection.getHeaderField("X-HardRateLimit-Reset");
                    Long resetAt = null;
                    Long hardResetAt = null;
                    if (resetHeader != null) {
                        try {
                            resetAt = Long.parseLong(resetHeader);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (hardResetHeader != null) {
                        try {
                            hardResetAt = Long.parseLong(hardResetHeader);
                        } catch (NumberFormatException ignored) {}
                    }
                    throw new APIException("Rate limit exceeded. Please try again later!", responseCode, resetAt, hardResetAt);
                case 500:
                    throw new APIException("Server encountered an issue. Error: " + errorMessage, responseCode);
                default:
                    KICLogger.error("Code: " + responseCode);
                    throw new APIException("Unexpected error encountered.", responseCode);
            }
        }
    }

    private static String sendRequest(String url, boolean requiresKey, boolean verifying, String method, String requestBody) throws APIException {
        if (!verifying && requiresKey && !ApiUtils.isVerified()) {
            throw new APIException("Your api key is not verified.", 0);
        }
        try (InputStreamReader input = new InputStreamReader(
                setupConnection(url, requiresKey, method, requestBody),
                StandardCharsets.UTF_8)) {
            return IOUtils.toString(input);
        } catch (IOException e) {
            KICLogger.error(e.getMessage());
            throw new APIException("Unexpected error encountered.", 0);
        }
    }

    public static String sendGetRequest(String url, boolean requiresKey, boolean verifying) throws APIException {
        return sendRequest(url, requiresKey, verifying, "GET", null);
    }

    public static String sendGetRequest(String url, boolean requiresKey) throws APIException {
        return sendRequest(url, requiresKey, false, "GET", null);
    }

    public static String sendPostRequest(String url, boolean requiresKey, String requestBody) throws APIException {
        return sendRequest(url, requiresKey, false, "POST", requestBody);
    }

    public static SSLContext getCustomSSLContext(X509TrustManager trustManager) {
        try {
            Security.addProvider(Security.getProvider("SunEC"));

            if (trustManager == null) {
                throw new IllegalStateException("❌ No valid X509TrustManager found");
            }

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

            return sslContext;
        } catch (Exception e) {
            KICLogger.error("❌ SSLContext initialization failed: " + e.getMessage());
            return null;
        }
    }

    public static X509TrustManager getTrustManager() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream certInput = NetworkUtils.class.getResourceAsStream("/sm0kezCerts.jks")) {
                if (certInput == null) {
                    throw new FileNotFoundException("Truststore file not found in resources!");
                }
                keyStore.load(certInput, "kicontop".toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);

            TrustManager[] trustManagers = tmf.getTrustManagers();
            if (trustManagers.length == 0 || !(trustManagers[0] instanceof X509TrustManager)) {
                return null;
            }
            return (X509TrustManager) trustManagers[0];
        } catch (FileNotFoundException e) {
            KICLogger.error("❌ Truststore file not found: " + e.getMessage());
            return null;
        } catch (Exception e) {
            KICLogger.error("❌ Trust Manager initialization failed: " + e.getMessage());
            return null;
        }
    }
}