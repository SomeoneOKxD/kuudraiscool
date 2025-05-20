package someoneok.kic.utils;

import net.minecraft.util.MathHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static someoneok.kic.KIC.mc;

public class RotationUtils {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Smoothly rotates the players head to the given yaw and pitch.
     *
     * @param yaw The yaw to rotate to
     * @param pitch The pitch to rotate to
     * @param rotTime how long the rotation should take. In **milliseconds**.
     */
    public static void smoothRotateTo(float yaw, float pitch, int rotTime, Runnable functionToRunWhenDone) {
        float initialYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);
        float initialPitch = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch);
        float targetYaw = wrapAngle(yaw);
        float targetPitch = wrapAngle(pitch);

        long startTime = System.currentTimeMillis();
        int duration = Math.max(10, Math.min(rotTime, 10000));

        final ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
        futureHolder[0] = executor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            float progress = Math.min(1f, (currentTime - startTime) / (float) duration);
            float amount = bezier(progress);

            if (mc.thePlayer != null) {
                mc.thePlayer.rotationYaw = initialYaw + (targetYaw - initialYaw) * amount;
                mc.thePlayer.rotationPitch = initialPitch + (targetPitch - initialPitch) * amount;
            }

            if (progress >= 1f) {
                futureHolder[0].cancel(false);
                if (mc.thePlayer != null) {
                    mc.thePlayer.rotationYaw = yaw;
                    mc.thePlayer.rotationPitch = pitch;
                }
                functionToRunWhenDone.run();
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    public static void smoothRotateTo(float yaw, float pitch, int rotTime) {
        smoothRotateTo(yaw, pitch, rotTime, () -> {});
    }

    private static float wrapAngle(float angle) {
        while (angle >= 180f) angle -= 360f;
        while (angle < -180f) angle += 360f;
        return angle;
    }

    private static float bezier(float t) {
        float p0 = 0f;
        float p1 = 0.42f;
        float p2 = 0.58f;
        float p3 = 1f;

        return (float) (
                Math.pow(1 - t, 3) * p0 +
                        3 * Math.pow(1 - t, 2) * t * p1 +
                        3 * (1 - t) * Math.pow(t, 2) * p2 +
                        Math.pow(t, 3) * p3
        );
    }
}
