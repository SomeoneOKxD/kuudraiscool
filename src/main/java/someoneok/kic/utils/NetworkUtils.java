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

        if (url.contains("sm0kez.com")) {
            if (KIC.CUSTOM_SSL_CONTEXT != null && connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(KIC.CUSTOM_SSL_CONTEXT.getSocketFactory());
            }
            connection.setRequestProperty("KIC-Version", KIC.VERSION);
            connection.setRequestProperty("IGN", getPlayerName());

            if (requiresKey) {
                connection.setRequestProperty("API-Key", KICConfig.apiKey);
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
                KICLogger.forceError("[API DOWN] URL: " + url + " | Code: " + responseCode + " | Error: API unreachable or blocked by Cloudflare.");
                throw new APIException("API is unreachable. It may be offline or blocked by Cloudflare.", responseCode);
            }

            switch (responseCode) {
                case 400:
                case 404:
                    KICLogger.forceError("[API ERROR] URL: " + url + " | Method: " + method + " | Code: " + responseCode + " | Body: " + requestBody + " | Message: " + errorMessage);
                    throw new APIException(errorMessage, responseCode);
                case 401:
                case 403:
                    KICLogger.forceError("[AUTH ERROR] URL: " + url + " | Method: " + method + " | Code: " + responseCode + " | Body: " + requestBody + " | Message: " + errorMessage);
                    throw new APIException("Access denied. Reason: " + errorMessage, responseCode);
                case 429:
                    String resetHeader = connection.getHeaderField("X-RateLimit-Reset");
                    String hardResetHeader = connection.getHeaderField("X-HardRateLimit-Reset");
                    String remainingHeader = connection.getHeaderField("X-RateLimit-Remaining");
                    long resetAt = -1L;
                    long hardResetAt = -1L;
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
                    KICLogger.forceError("[RATE LIMITED] URL: " + url + " | Method: " + method + " | Code: 429 | Reset: " + resetAt + " | Hard Reset: " + hardResetAt + " | Requests Remaining: " + remainingHeader + " | Body: " + requestBody + " | Message: " + errorMessage);
                    throw new APIException("Rate limit exceeded. Please try again later!", responseCode, resetAt, hardResetAt);
                case 500:
                    KICLogger.forceError("[SERVER ERROR] URL: " + url + " | Method: " + method + " | Code: 500 | Body: " + requestBody + " | Message: " + errorMessage);
                    throw new APIException("Server encountered an issue. Error: " + errorMessage, responseCode);
                default:
                    KICLogger.forceError("[UNEXPECTED ERROR] URL: " + url + " | Method: " + method + " | Code: " + responseCode + " | Body: " + requestBody + " | Message: " + errorMessage);
                    throw new APIException("Unexpected error encountered.", responseCode);
            }
        }
    }

    private static String sendRequest(String url, boolean requiresKey, boolean verifying, String method, String requestBody) throws APIException {
        if (!verifying && requiresKey && !ApiUtils.isVerified()) {
            throw new APIException("Your api key is not verified.", 0);
        }

        long startNanos = System.nanoTime();
        try (InputStream is = setupConnection(url, requiresKey, method, requestBody);
             InputStreamReader input = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            String result = IOUtils.toString(input);
            long tookMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            KICLogger.forceInfo(String.format("[HTTP] %s %s completed in %d ms", method, url, tookMs));
            return result;
        } catch (APIException e) {
            long tookMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            KICLogger.forceError(String.format("[HTTP] %s %s failed in %d ms (code=%d, msg=%s)",
                    method, url, tookMs, e.getStatus(), e.getMessage()));
            throw e;
        } catch (IOException e) {
            long tookMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            KICLogger.forceError(String.format("[HTTP] %s %s errored in %d ms (I/O exception, msg=%s)", method, url, tookMs, e.getMessage()));
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

    public static String sendDeleteRequest(String url, boolean requiresKey) throws APIException {
        return sendRequest(url, requiresKey, false, "DELETE", null);
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
            KICLogger.forceError("❌ SSLContext initialization failed: " + e.getMessage());
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
            KICLogger.forceError("❌ Truststore file not found: " + e.getMessage());
            return null;
        } catch (Exception e) {
            KICLogger.forceError("❌ Trust Manager initialization failed: " + e.getMessage());
            return null;
        }
    }
}