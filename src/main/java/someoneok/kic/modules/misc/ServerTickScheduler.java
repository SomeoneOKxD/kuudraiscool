package someoneok.kic.modules.misc;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.events.ServerTickEvent;
import someoneok.kic.utils.ApiUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static someoneok.kic.KIC.mc;

public class ServerTickScheduler {
    private static final List<Runnable> ON_SERVER_TICK = new ArrayList<>();
    private static final List<Task> SCHEDULED = new ArrayList<>();
    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();

    @SubscribeEvent(receiveCanceled = true)
    public void onServerTick(ServerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !ApiUtils.isVerified()) return;
        for (Runnable r : ON_SERVER_TICK) try { r.run(); } catch (Throwable ignored) {}

        for (Iterator<Task> it = SCHEDULED.iterator(); it.hasNext(); ) {
            Task t = it.next();
            t.delay--;
            if (t.delay <= 0) {
                try { t.fn.run(); } catch (Throwable ignored) {}
                it.remove();
            }
        }

        Runnable r = QUEUE.poll();
        if (r != null) try { r.run(); } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        ON_SERVER_TICK.clear();
        SCHEDULED.clear();
        QUEUE.clear();
    }

    public static void onTick(Runnable fn) {
        if (fn == null) return;
        ON_SERVER_TICK.add(fn);
    }

    public static void scheduleTask(Runnable fn, int delayTicks) {
        if (fn == null) return;
        if (delayTicks <= 0) delayTicks = 1;
        SCHEDULED.add(new Task(fn, delayTicks));
    }

    public static void queueTask(Runnable fn) {
        if (fn == null) return;
        QUEUE.add(fn);
    }

    private static final class Task {
        final Runnable fn;
        int delay;
        Task(Runnable fn, int delay) {
            this.fn = Objects.requireNonNull(fn);
            this.delay = Math.max(0, delay);
        }
    }
}
