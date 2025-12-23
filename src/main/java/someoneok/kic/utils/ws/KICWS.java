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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
    private static WebSocket closingWs;

    private static final AtomicBoolean isConnected    = new AtomicBoolean(false);
    private static final AtomicBoolean isConnecting   = new AtomicBoolean(false);
    private static final AtomicBoolean isReconnecting = new AtomicBoolean(false);

    private static final int RECONNECT_BASE_DELAY_SEC = 3;
    private static final int RECONNECT_MAX_DELAY_SEC  = 30;
    private static final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private static final long HB_INTERVAL_MS = 25_000L;
    private static final ScheduledExecutorService HB_EXEC =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "KIC-WS-HB");
                t.setDaemon(true);
                return t;
            });
    private static volatile ScheduledFuture<?> hbFuture;

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
        if (!ApiUtils.isVerified() || !onHypixel) return;

        synchronized (WS_LOCK) {
            if (isConnected.get() && webSocket != null) return;
        }

        if (!isConnecting.compareAndSet(false, true)) return;
        if (client == null) setupWS();

        stopHeartbeat();

        WebSocket old;
        synchronized (WS_LOCK) {
            old = webSocket;
            webSocket = null;
        }
        if (old != null) {
            try { old.cancel(); } catch (Throwable ignored) {}
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("API-Key", KICConfig.apiKey)
                .addHeader("User-Agent", "kuudraiscool")
                .addHeader("KIC-Version", KIC.VERSION)
                .addHeader("IGN", getPlayerName())
                .build();

        WebSocket ws = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                if (!isCurrent(ws)) { ws.close(1000, "stale"); return; }

                isConnected.set(true);
                isConnecting.set(false);
                isReconnecting.set(false);
                reconnectAttempts.set(0);

                startHeartbeat();
                KICLogger.info("[KIC-WS] WebSocket connected.");
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                if (!isCurrent(ws)) return;
                KICWSHandler.onWebSocketText(text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                if (isIntentional(ws)) {
                    clearClosing(ws);
                    return;
                }

                if (!clearIfCurrent(ws)) return;

                stopHeartbeat();
                isConnected.set(false);
                isConnecting.set(false);

                KICLogger.error("[KIC-WS] WebSocket failure: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                scheduleReconnect();
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                if (isIntentional(ws)) {
                    clearClosing(ws);
                    stopHeartbeat();
                    isConnected.set(false);
                    isConnecting.set(false);
                    KICLogger.info("[KIC-WS] WebSocket closed (intentional): " + code + " " + reason);
                    return;
                }

                if (!clearIfCurrent(ws)) return;

                stopHeartbeat();
                isConnected.set(false);
                isConnecting.set(false);

                KICLogger.info("[KIC-WS] WebSocket closed: " + code + " " + reason);

                if (code == 1008) {
                    ApiUtils.reset();
                    ApiUtils.setRoleVariables();
                    sendMessageToPlayer(KIC.KICPrefix + " §cAPI key sharing detected. Your key has been revoked.");
                    return;
                }

                if (shouldReconnect(code)) scheduleReconnect();
            }
        });

        synchronized (WS_LOCK) {
            webSocket = ws;
        }
    }

    private static boolean isCurrent(WebSocket ws) {
        synchronized (WS_LOCK) { return webSocket == ws; }
    }

    private static boolean clearIfCurrent(WebSocket ws) {
        synchronized (WS_LOCK) {
            if (webSocket != ws) return false;
            webSocket = null;
            return true;
        }
    }

    private static boolean isIntentional(WebSocket ws) {
        synchronized (WS_LOCK) { return closingWs == ws && ws != null; }
    }

    private static void clearClosing(WebSocket ws) {
        synchronized (WS_LOCK) { if (closingWs == ws) closingWs = null; }
    }

    private static void startHeartbeat() {
        stopHeartbeat();
        hbFuture = HB_EXEC.scheduleAtFixedRate(() -> {
            if (!isConnected.get()) return;
            WebSocket ws;
            synchronized (WS_LOCK) { ws = webSocket; }
            if (ws == null) return;
            try { ws.send("ping"); } catch (Throwable ignored) {}
        }, HB_INTERVAL_MS, HB_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private static void stopHeartbeat() {
        ScheduledFuture<?> f = hbFuture;
        hbFuture = null;
        if (f != null) f.cancel(false);
    }

    private static void scheduleReconnect() {
        if (!onHypixel) return;
        if (!isReconnecting.compareAndSet(false, true)) return;

        int attempt = reconnectAttempts.getAndIncrement();
        int delaySec = (int) Math.min(RECONNECT_BASE_DELAY_SEC * Math.pow(2, attempt), RECONNECT_MAX_DELAY_SEC);

        KICLogger.info("[KIC-WS] Reconnecting in " + delaySec + "s (attempt " + attempt + ").");

        Multithreading.schedule(() -> {
            isReconnecting.set(false);
            connect();
        }, delaySec, TimeUnit.SECONDS);
    }

    private static boolean shouldReconnect(int code) {
        return code != 1000 && code != 1001 && code != 1008;
    }

    private static void sendMessage(String message) {
        if (!isConnected.get()) return;

        WebSocket ws;
        synchronized (WS_LOCK) { ws = webSocket; }
        if (ws != null) {
            try { ws.send(message); } catch (Throwable ignored) {}
        }
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

        if (!canSend) return;

        String type = premium ? "PREMIUM_CHAT" : "CHAT";
        sendMessage("{\"type\":\"" + type + "\",\"data\":{\"message\":\"" + StringEscapeUtils.escapeJson(message) + "\"}}");
    }

    public static void sendLag(int tier, long totalTicks, long runtime) {
        if (!ApiUtils.isVerified() || (tier == 0 && totalTicks == 0 && runtime == 0)) return;
        sendMessage("{\"type\":\"LAG\",\"data\":{\"tier\":" + tier + ",\"totalTicks\":" + totalTicks + ",\"runTime\":" + runtime + "}}");
    }

    public static void close() {
        stopHeartbeat();
        isConnected.set(false);
        isConnecting.set(false);
        isReconnecting.set(false);

        WebSocket ws;
        synchronized (WS_LOCK) {
            ws = webSocket;
            webSocket = null;
            closingWs = ws;
        }

        if (ws != null) {
            try { ws.close(1000, "Goodbye!"); }
            catch (Throwable ignored) {
                try { ws.cancel(); } catch (Throwable ignored2) {}
                clearClosing(ws);
            }
        }
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        if (ApiUtils.isVerified()) Multithreading.runAsync(KICWS::connect);
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        close();
    }
}
