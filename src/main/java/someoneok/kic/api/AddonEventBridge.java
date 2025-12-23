package someoneok.kic.api;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.api.events.AddonSubscribe;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AddonEventBridge {
    private static final class Handler {
        final Object target;
        final Method method;
        final Class<?> eventType;
        final boolean receiveCanceled;

        Handler(Object t, Method m, Class<?> et, boolean rc) {
            target = t;
            method = m;
            eventType = et;
            receiveCanceled = rc;
            method.setAccessible(true);
        }

        void call(Event e) throws Exception {
            method.invoke(target, e);
        }
    }

    private static final Map<EventPriority, Map<Class<?>, List<Handler>>> REG = new EnumMap<>(EventPriority.class);
    private static final Map<Class<?>, Handler[]> DISPATCH_CACHE_HIGHEST = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Handler[]> DISPATCH_CACHE_HIGH    = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Handler[]> DISPATCH_CACHE_NORMAL  = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Handler[]> DISPATCH_CACHE_LOW     = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Handler[]> DISPATCH_CACHE_LOWEST  = new ConcurrentHashMap<>();

    private static final Handler[] EMPTY_HANDLERS = new Handler[0];

    static { for (EventPriority p : EventPriority.values()) REG.put(p, new ConcurrentHashMap<>()); }

    public static void register(Object listener) {
        Class<?> cls = listener.getClass();

        for (Method m : cls.getMethods()) {
            AddonSubscribe ann = m.getAnnotation(AddonSubscribe.class);
            if (ann == null) continue;

            Class<?>[] pts = m.getParameterTypes();
            if (pts.length != 1) continue;

            Class<?> evt = pts[0];
            if (!Event.class.isAssignableFrom(evt)) continue;

            EventPriority pr = ann.priority();
            Map<Class<?>, List<Handler>> byType = REG.get(pr);
            List<Handler> list = byType.computeIfAbsent(evt, k -> new CopyOnWriteArrayList<>());
            list.add(new Handler(listener, m, evt, ann.receiveCanceled()));
        }
        clearCaches();
    }

    public static void unregister(Object listener) {
        for (Map<Class<?>, List<Handler>> byType : REG.values()) {
            for (List<Handler> list : byType.values()) {
                if (list != null) list.removeIf(handler -> handler.target == listener);
            }
        }
        clearCaches();
    }

    private static void clearCaches() {
        DISPATCH_CACHE_HIGHEST.clear();
        DISPATCH_CACHE_HIGH.clear();
        DISPATCH_CACHE_NORMAL.clear();
        DISPATCH_CACHE_LOW.clear();
        DISPATCH_CACHE_LOWEST.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onAnyHighest(Event e) { dispatch(e, EventPriority.HIGHEST, DISPATCH_CACHE_HIGHEST); }
    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public void onAnyHigh(Event e)    { dispatch(e, EventPriority.HIGH,    DISPATCH_CACHE_HIGH); }
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onAnyNormal(Event e)  { dispatch(e, EventPriority.NORMAL,  DISPATCH_CACHE_NORMAL); }
    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onAnyLow(Event e)     { dispatch(e, EventPriority.LOW,     DISPATCH_CACHE_LOW); }
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onAnyLowest(Event e)  { dispatch(e, EventPriority.LOWEST,  DISPATCH_CACHE_LOWEST); }

    private static void dispatch(Event e, EventPriority pr, Map<Class<?>, Handler[]> cache) {
        if (e == null) return;
        final Class<?> ec = e.getClass();

        Handler[] hs = cache.computeIfAbsent(ec, k -> buildArrayFor(k, pr));
        if (hs.length == 0) return;

        final boolean checkCancel = e.isCancelable();
        boolean canceled = checkCancel && e.isCanceled();

        for (Handler h : hs) {
            if (checkCancel && canceled && !h.receiveCanceled) continue;
            try { h.call(e); } catch (Throwable ignored) {}
            if (checkCancel && !canceled && e.isCanceled()) canceled = true;
        }
    }

    private static Handler[] buildArrayFor(Class<?> ec, EventPriority pr) {
        if (ec == null) return EMPTY_HANDLERS;

        List<Handler> out = new ArrayList<>(8);
        Map<Class<?>, List<Handler>> byType = REG.get(pr);

        Class<?> k = ec;
        while (k != null) {
            add(out, byType.get(k));
            k = k.getSuperclass();
        }

        Set<Class<?>> ifaces = new LinkedHashSet<>();
        collectAllInterfaces(ec, ifaces);
        for (Class<?> itf : ifaces) add(out, byType.get(itf));

        return out.isEmpty() ? new Handler[0] : out.toArray(new Handler[0]);
    }

    private static void collectAllInterfaces(Class<?> c, Set<Class<?>> out) {
        if (c == null) return;
        for (Class<?> itf : c.getInterfaces()) if (out.add(itf)) collectAllInterfaces(itf, out);
    }

    private static void add(List<Handler> dst, List<Handler> src) {
        if (src != null && !src.isEmpty()) dst.addAll(src);
    }
}
