package someoneok.kic.utils.ws;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import okhttp3.*;
import org.apache.commons.lang3.StringEscapeUtils;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.HypixelJoinEvent;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.LocationUtils.onHypixel;
import static someoneok.kic.utils.PlayerUtils.getPlayerName;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class KICWS {
    private static final String API_URL = "wss://api.sm0kez.com/ws";

    private static final Object WS_LOCK = new Object();

    private static OkHttpClient client;
    private static WebSocket webSocket;

    private static final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    private static final AtomicBoolean isConnected    = new AtomicBoolean(false);
    private static final AtomicBoolean isConnecting   = new AtomicBoolean(false);
    private static final AtomicBoolean isClosing      = new AtomicBoolean(false);

    private static final int RECONNECT_BASE_DELAY_SEC = 3;
    private static final int RECONNECT_MAX_DELAY_SEC  = 30;
    private static final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private static final int HB_INTERVAL_TICKS = 20 * 25; // 25s
    private static int hbTickCounter = 0;

    public static void setupWS() {
        if (client != null) return;

        client = new OkHttpClient.Builder()
                .sslSocketFactory(KIC.CUSTOM_SSL_CONTEXT.getSocketFactory(), KIC.CUSTOM_TRUST_MANAGER)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }

    public static void connect() {
        if (!ApiUtils.isVerified() || !onHypixel) {
            KICLogger.info("[KIC-WS] Skipping connect — not verified or not on Hypixel.");
            return;
        }

        if (isConnected.get() || isClosing.get()) {
            KICLogger.info("[KIC-WS] Skipping connect — already connected or closing. " +
                    "connected=" + isConnected.get() + ", closing=" + isClosing.get());
            return;
        }

        if (!isConnecting.compareAndSet(false, true)) {
            KICLogger.info("[KIC-WS] Skipping connect — already connecting on another thread.");
            return;
        }

        KICLogger.info("[KIC-WS] Connecting to websocket server...");

        if (client == null) setupWS();

        synchronized (WS_LOCK) {
            if (webSocket != null) {
                try {
                    KICLogger.info("[KIC-WS] Cancelling stale websocket before creating a new one.");
                    webSocket.cancel();
                } catch (Exception e) {
                    KICLogger.error("[KIC-WS] Error while cancelling stale websocket: " + e.getMessage());
                } finally {
                    webSocket = null;
                }
            }

            createWebsocket();
        }
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
            public void onOpen(WebSocket ws, Response response) {
                KICLogger.info("[KIC-WS] WebSocket connected.");

                isReconnecting.set(false);
                isConnected.set(true);
                isConnecting.set(false);
                reconnectAttempts.set(0);
                hbTickCounter = 0;
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                KICWSHandler.onWebSocketText(text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                KICLogger.error("[KIC-WS] WebSocket failure.");
                KICLogger.error("[KIC-WS] Error cause: " + t.getClass().getSimpleName());
                KICLogger.error("[KIC-WS] Error message: " + t.getMessage());

                for (StackTraceElement element : t.getStackTrace()) {
                    KICLogger.error("    at " + element.toString());
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

                synchronized (WS_LOCK) {
                    if (KICWS.webSocket == ws) {
                        try {
                            KICLogger.info("[KIC-WS] Cancelling and clearing current websocket (onFailure).");
                            KICWS.webSocket.cancel();
                        } catch (Exception cancelEx) {
                            KICLogger.error("[KIC-WS] Exception during websocket cancel: " + cancelEx.getMessage());
                        } finally {
                            KICWS.webSocket = null;
                        }
                    }
                }

                isConnected.set(false);
                isConnecting.set(false);
                hbTickCounter = 0;

                if (!isClosing.get()) scheduleReconnect();
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                KICLogger.info("[KIC-WS] Websocket closing with reason: " + reason + " and code " + code);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                KICLogger.info("[KIC-WS] Websocket closed with reason: " + reason + " and code " + code);

                synchronized (WS_LOCK) {
                    if (KICWS.webSocket == ws) {
                        KICWS.webSocket = null;
                    }
                }

                isClosing.set(false);
                isConnecting.set(false);
                isConnected.set(false);
                hbTickCounter = 0;

                if (code == 1008) {
                    ApiUtils.reset();
                    ApiUtils.setRoleVariables();
                    sendMessageToPlayer(KIC.KICPrefix + " §cAPI key sharing detected. Your key has been revoked.");
                } else if (!isClosing.get() && shouldReconnect(code)) {
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
        if (!isClosing.compareAndSet(false, true)) {
            KICLogger.info("[KIC-WS] close() called but already closing.");
            return;
        }

        WebSocket wsToClose;
        synchronized (WS_LOCK) {
            wsToClose = webSocket;
        }

        if (wsToClose != null) {
            KICLogger.info("[KIC-WS] Closing websocket with code 1000.");
            wsToClose.close(1000, "Goodbye!");
        } else {
            KICLogger.info("[KIC-WS] close() called but websocket is null.");
            isConnected.set(false);
            isConnecting.set(false);
            isClosing.set(false);
            isReconnecting.set(false);
        }
    }

    private static void scheduleReconnect() {
        if (!onHypixel) {
            KICLogger.info("[KIC-WS] Not scheduling reconnect — not on Hypixel.");
            return;
        }

        if (!isReconnecting.compareAndSet(false, true)) {
            KICLogger.info("[KIC-WS] Reconnect already scheduled, skipping.");
            return;
        }

        int attempt = reconnectAttempts.getAndIncrement();
        int delaySec = (int) Math.min(
                RECONNECT_BASE_DELAY_SEC * Math.pow(2, attempt),
                RECONNECT_MAX_DELAY_SEC
        );

        KICLogger.info("[KIC-WS] Websocket attempting to reconnect in " + delaySec + " seconds (attempt " + attempt + ").");

        Multithreading.schedule(() -> {
            isReconnecting.set(false);
            connect();
        }, delaySec, TimeUnit.SECONDS);
    }

    private static boolean shouldReconnect(int code) {
        return code != 1000 && code != 1001 && code != 1008;
    }

    private static boolean isWebSocketOpen() {
        return isConnected.get() && webSocket != null;
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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!isConnected.get() || isClosing.get()) {
            hbTickCounter = 0;
            return;
        }

        hbTickCounter++;
        if (hbTickCounter < HB_INTERVAL_TICKS) return;
        hbTickCounter = 0;

        WebSocket ws;
        synchronized (WS_LOCK) { ws = webSocket; }

        if (ws == null) return;
        try { ws.send("ping"); } catch (Exception ignored) {}
    }
}
