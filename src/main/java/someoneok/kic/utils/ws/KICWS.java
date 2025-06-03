package someoneok.kic.utils.ws;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import okhttp3.*;
import org.apache.commons.lang3.StringEscapeUtils;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.HypixelJoinEvent;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.concurrent.TimeUnit;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.LocationUtils.onHypixel;
import static someoneok.kic.utils.PlayerUtils.getPlayerName;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class KICWS {
    private static final String API_URL = "wss://api.sm0kez.com/ws";
    private static OkHttpClient client;
    private static WebSocket webSocket;
    private static boolean isReconnecting = false;
    private static final int RECONNECT_DELAY = 15;
    private static boolean isConnected = false;
    private static boolean isConnecting = false;
    private static boolean isClosing = false;

    public static void setupWS() {
        client = new OkHttpClient.Builder()
                .sslSocketFactory(KIC.CUSTOM_SSL_CONTEXT.getSocketFactory(), KIC.CUSTOM_TRUST_MANAGER)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }

    public static void connect() {
        if (!ApiUtils.isVerified() || isWebSocketOpen() || isConnecting || isClosing || !onHypixel) {
            KICLogger.info("[KIC-WS] Skipping connect — state: key verified?=" + ApiUtils.isVerified() + ", websocket null?=" + (webSocket == null) + ", connected=" + isConnected + ", connecting=" + isConnecting + ", closing=" + isClosing  + ", onHypixel=" + onHypixel);
            return;
        }
        if (client == null) setupWS();
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }

        isConnecting = true;
        KICLogger.info("[KIC-WS] Connecting to websocket server...");
        createWebsocket();
    }

    private static void createWebsocket() {
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("API-Key", KICConfig.apiKey)
                .addHeader("User-Agent", "kuudraiscool")
                .addHeader("KIC-Version", KIC.VERSION)
                .addHeader("IGN", getPlayerName())
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                KICLogger.info("[KIC-WS] WebSocket connected.");
                isReconnecting = false;
                isConnected = true;
                isConnecting = false;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                KICWSHandler.onWebSocketText(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                KICLogger.error("[KIC-WS] WebSocket failure.");

                if (t != null) {
                    KICLogger.error("[KIC-WS] Error cause: " + t.getClass().getSimpleName());
                    KICLogger.error("[KIC-WS] Error message: " + t.getMessage());

                    for (StackTraceElement element : t.getStackTrace()) {
                        KICLogger.error("    at " + element.toString());
                    }
                }

                if (response != null) {
                    KICLogger.error("[KIC-WS] HTTP Response Code: " + response.code());
                    try {
                        ResponseBody body = response.body();
                        if (body != null) {
                            String bodyText = body.string();
                            KICLogger.error("[KIC-WS] Response Body: " + bodyText);
                        }
                    } catch (Exception ex) {
                        KICLogger.error("[KIC-WS] Failed to read response body: " + ex.getMessage());
                    }
                } else {
                    KICLogger.info("[KIC-WS] No HTTP response (likely network-level failure or abrupt socket close)");
                }

                if (KICWS.webSocket != null) {
                    try {
                        KICLogger.info("[KIC-WS] Cancelling and clearing current websocket.");
                        KICWS.webSocket.cancel();
                    } catch (Exception cancelEx) {
                        KICLogger.error("[KIC-WS] Exception during websocket cancel: " + cancelEx.getMessage());
                    }
                    KICWS.webSocket = null;
                }

                isConnected = false;
                isConnecting = false;
                scheduleReconnect();
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                KICLogger.info("[KIC-WS] Websocket closing with reason: " + reason + " and code " + code);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                KICLogger.info("[KIC-WS] Websocket closed with reason: " + reason + " and code " + code);
                isClosing = false;
                isConnecting = false;
                isConnected = false;
                KICWS.webSocket = null;

                if (code == 1008) {
                    ApiUtils.reset();
                    ApiUtils.setRoleVariables();
                    sendMessageToPlayer(KIC.KICPrefix + " §cAPI key sharing detected. Your key has been revoked.");
                } else if (shouldReconnect(code)) {
                    scheduleReconnect();
                }
            }
        });
    }

    private static void sendMessage(String message) {
        if (isWebSocketOpen()) webSocket.send(message);
    }

    public static void sendChatMessage(String message, boolean premium) {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        if (isNullOrEmpty(message)) return;

        boolean canSend = premium
                ? (KICConfig.kicPlusChat && ApiUtils.hasPremium())
                : KICConfig.kicChat;

        if (canSend) {
            String type = premium ? "PREMIUM_CHAT" : "CHAT";
            sendMessage("{\"type\":\"" + type + "\",\"data\":{\"message\":\"" + StringEscapeUtils.escapeJson(message) + "\"}}");
        }
    }

    public static void sendLag(int tier, long totalTicks, long runtime) {
        if (!ApiUtils.isVerified() || (tier == 0 && totalTicks == 0 && runtime == 0)) return;
        sendMessage("{\"type\":\"LAG\",\"data\":{\"tier\":" + tier + ",\"totalTicks\":" + totalTicks + ",\"runTime\":" + runtime + "}}");
    }

    public static void close() {
        if (isWebSocketOpen()) {
            isClosing = true;
            webSocket.close(1000, "Goodbye!");
        } else {
            isConnected = false;
            isConnecting = false;
            isClosing = false;
            KICWS.webSocket = null;
        }
    }

    private static void scheduleReconnect() {
        if (!isReconnecting && onHypixel) {
            isReconnecting = true;
            KICLogger.info("[KIC-WS] Websocket attempting to reconnect in 30 seconds...");

            Multithreading.schedule(KICWS::connect, RECONNECT_DELAY, TimeUnit.SECONDS);
        }
    }

    private static boolean shouldReconnect(int code) {
        return code != 1000 && code != 1001 && code != 1008;
    }

    private static boolean isWebSocketOpen() {
        return isConnected && webSocket != null;
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        if (ApiUtils.isVerified()) {
            KICLogger.info("Connected to hypixel.");
            Multithreading.runAsync(KICWS::connect);
        }
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        KICLogger.info("Server disconnect triggered.");
        close();
    }
}
