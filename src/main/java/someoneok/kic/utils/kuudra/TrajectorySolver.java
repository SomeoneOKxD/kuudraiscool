package someoneok.kic.utils.kuudra;

import net.minecraft.util.Vec3;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.pearls.PearlSolution;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class TrajectorySolver {
    private static final double GRAVITY = 0.03;
    private static final double SPEED = 1.5;
    private static final double DRAG = 0.99;
    private static final double TICK_MS = 50.0;

    private static final int MAX_TICKS = 100;
    private static final int REFINE_STEPS = 100;
    private static final int REFINE_ITERATIONS = 50;
    private static final int GRID_STEPS = 100;

    private static final double HIT_RADIUS_SQ = 0.7 * 0.7;
    private static final double TOLERANCE_SQ = 0.1 * 0.1;

    private static int SKY_DISTANCE = 30;
    private static int FLAT_DISTANCE = 15;

    private static final double MIN_THETA = 0.01;
    private static final double MAX_THETA = Math.PI / 2 - 0.01;
    private static final double INITIAL_REFINE_DEG = 1.0;

    private static final double FLAT_ANGLE_BIAS_DEG = 0.4;

    private static final double MIN_SKY_THETA = Math.toRadians(34);
    private static final double MAX_FLAT_THETA = Math.toRadians(32);

    public static void updateDistances() {
        SKY_DISTANCE = KICConfig.advancedPearlSettings ? KICConfig.APSkyDistance : 30;
        FLAT_DISTANCE = KICConfig.advancedPearlSettings ? KICConfig.APFlatDistance : 15;
    }

    public static PearlSolution solvePearl(boolean sky, Vec3 start, Vec3 target) {
        final double dx = target.xCoord - start.xCoord;
        final double dz = target.zCoord - start.zCoord;
        final double horizontalDist = Math.hypot(dx, dz);

        if (horizontalDist < 1.0) {
            if (!sky) return null;
            return new PearlSolution(
                    new Vec3(start.xCoord, start.yCoord + SKY_DISTANCE, start.zCoord),
                    4500, 0.0f, -90.0f
            );
        }

        final double invLength = 1.0 / horizontalDist;
        final double ux = dx * invLength;
        final double uz = dz * invLength;

        SearchResult bestResult = searchInitialBestAngle(start, target, ux, uz, sky);
        if (bestResult == null) return null;

        double refineRange = Math.toRadians(INITIAL_REFINE_DEG);
        for (int iter = 0; iter < REFINE_ITERATIONS; iter++) {
            final double lower = Math.max(MIN_THETA, bestResult.theta - refineRange);
            final double upper = Math.min(MAX_THETA, bestResult.theta + refineRange);

            SearchResult refined = searchRefinedBestAngle(lower, upper, start, target, ux, uz, sky);
            if (refined == null || refined.errorSq >= bestResult.errorSq) break;

            bestResult = refined;
            if (bestResult.errorSq < TOLERANCE_SQ) break;

            refineRange *= 0.5;
        }

        double finalTheta = bestResult.theta;
        int finalTick = bestResult.tick;

        if (!sky) {
            finalTheta = Math.min(
                    MAX_FLAT_THETA,
                    Math.max(MIN_THETA, finalTheta + Math.toRadians(FLAT_ANGLE_BIAS_DEG))
            );
        }

        Vec3 velocity = computeVelocity(finalTheta, ux, uz);

        if (!sky) {
            SimResult finalSim = simulateTrajectory(start, velocity, target);
            if (finalSim.hit && finalSim.hitTick >= 0) finalTick = finalSim.hitTick;
        }

        final Vec3 aimPoint = computeAimPoint(start, velocity, sky ? SKY_DISTANCE : FLAT_DISTANCE);
        final long flightTimeMs = Math.round(finalTick * TICK_MS);

        final double flatSpeed = Math.hypot(velocity.xCoord, velocity.zCoord);
        final float yaw = (float) (Math.toDegrees(Math.atan2(velocity.zCoord, velocity.xCoord)) - 90.0);
        final float pitch = (float) -Math.toDegrees(Math.atan2(velocity.yCoord, flatSpeed));

        return new PearlSolution(aimPoint, flightTimeMs, yaw, pitch);
    }

    private static SearchResult searchRefinedBestAngle(double minTheta, double maxTheta, Vec3 start, Vec3 target,
                                                double ux, double uz, boolean sky) {
        Comparator<SearchResult> comparator = getResultComparator(sky);
        SearchResult best = null;

        double clampedMin = Math.max(minTheta, sky ? MIN_SKY_THETA : MIN_THETA);
        double clampedMax = Math.min(maxTheta, sky ? MAX_THETA : MAX_FLAT_THETA);

        for (int i = 0; i <= REFINE_STEPS; i++) {
            double theta = clampedMin + (clampedMax - clampedMin) * i / REFINE_STEPS;

            Vec3 velocity = computeVelocity(theta, ux, uz);
            SimResult sim = simulateTrajectory(start, velocity, target);
            if (!sim.hit) continue;

            SearchResult current = new SearchResult(theta, sim.errorSq, sim.hitTick);
            if (best == null || comparator.compare(current, best) < 0) best = current;
        }

        return best;
    }

    private static SearchResult searchInitialBestAngle(Vec3 start, Vec3 target, double ux, double uz, boolean sky) {
        Comparator<SearchResult> comparator = getResultComparator(sky);

        final double minTheta = sky ? MIN_SKY_THETA : MIN_THETA;
        final double maxTheta = sky ? MAX_THETA : MAX_FLAT_THETA;

        AtomicReference<SearchResult> best = new AtomicReference<>(null);

        IntStream.rangeClosed(0, GRID_STEPS).parallel().forEach(i -> {
            double theta = minTheta + (maxTheta - minTheta) * i / GRID_STEPS;

            Vec3 velocity = computeVelocity(theta, ux, uz);
            SimResult sim = simulateTrajectory(start, velocity, target);
            if (!sim.hit) return;

            SearchResult candidate = new SearchResult(theta, sim.errorSq, sim.hitTick);

            best.updateAndGet(currentBest ->
                    (currentBest == null || comparator.compare(candidate, currentBest) < 0) ? candidate : currentBest
            );
        });

        return best.get();
    }

    private static Comparator<SearchResult> getResultComparator(boolean sky) {
        return Comparator
                .comparingDouble((SearchResult r) -> r.errorSq)
                .thenComparingDouble(r -> sky ? -r.theta : r.theta);
    }

    private static SimResult simulateTrajectory(Vec3 start, Vec3 initialVelocity, Vec3 target) {
        double x = start.xCoord;
        double y = start.yCoord;
        double z = start.zCoord;

        double vx = initialVelocity.xCoord;
        double vy = initialVelocity.yCoord;
        double vz = initialVelocity.zCoord;

        final double sx = start.xCoord;
        final double sy = start.yCoord;
        final double sz = start.zCoord;

        final double tx = target.xCoord;
        final double ty = target.yCoord;
        final double tz = target.zCoord;

        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;

        double bestErrorSq = dx * dx + dy * dy + dz * dz;
        int bestTick = -1;

        double maxDistanceSq = bestErrorSq * 4;
        double minY = ty - 5;

        for (int tick = 0; tick < MAX_TICKS; tick++) {
            x += vx;
            y += vy;
            z += vz;

            dx = tx - x;
            dy = ty - y;
            dz = tz - z;

            double errorSq = dx * dx + dy * dy + dz * dz;

            if (errorSq < bestErrorSq) {
                bestErrorSq = errorSq;
                bestTick = tick;
            }

            if (errorSq < HIT_RADIUS_SQ) return new SimResult(true, errorSq, tick);

            double sxDiff = x - sx;
            double syDiff = y - sy;
            double szDiff = z - sz;
            double distSqFromStart = sxDiff * sxDiff + syDiff * syDiff + szDiff * szDiff;

            if (y < minY || distSqFromStart > maxDistanceSq) break;

            vx *= DRAG;
            vy = (vy - GRAVITY) * DRAG;
            vz *= DRAG;
        }

        return new SimResult(false, bestErrorSq, bestTick);
    }

    private static Vec3 computeVelocity(double theta, double ux, double uz) {
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        return new Vec3(SPEED * cos * ux, SPEED * sin, SPEED * cos * uz);
    }

    private static Vec3 computeAimPoint(Vec3 start, Vec3 velocity, double distance) {
        double invNorm = 1.0 / velocity.lengthVector();
        return new Vec3(
                start.xCoord + velocity.xCoord * invNorm * distance,
                start.yCoord + velocity.yCoord * invNorm * distance,
                start.zCoord + velocity.zCoord * invNorm * distance
        );
    }

    private static class SimResult {
        final boolean hit;
        final double errorSq;
        final int hitTick;

        SimResult(boolean hit, double errorSq, int hitTick) {
            this.hit = hit;
            this.errorSq = errorSq;
            this.hitTick = hitTick;
        }
    }

    private static class SearchResult {
        final double theta;
        final double errorSq;
        final int tick;

        SearchResult(double theta, double errorSq, int tick) {
            this.theta = theta;
            this.errorSq = errorSq;
            this.tick = tick;
        }
    }
}