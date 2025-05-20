package someoneok.kic.modules.kuudra;

import net.minecraft.util.Vec3;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.PearlSolution;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
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

    private static final double MIN_SKY_THETA = Math.toRadians(34);
    private static final double MAX_FLAT_THETA = Math.toRadians(32);

    public static void updateDistances() {
        SKY_DISTANCE = KICConfig.advancedPearlSettings ? KICConfig.APSkyDistance : 30;
        FLAT_DISTANCE = KICConfig.advancedPearlSettings ? KICConfig.APFlatDistance : 15;
    }

    public static PearlSolution solvePearl(boolean sky, Vec3 start, Vec3 target) {
        double dx = target.xCoord - start.xCoord;
        double dz = target.zCoord - start.zCoord;
        double horizontalDist = Math.hypot(dx, dz);

        if (horizontalDist < 1.0) {
            if (!sky) return null;
            return new PearlSolution(
                    new Vec3(start.xCoord, start.yCoord + SKY_DISTANCE, start.zCoord),
                    4500, 0.0f, -90.0f
            );
        }

        double ux = dx / horizontalDist;
        double uz = dz / horizontalDist;

        SearchResult gridResult = searchInitialBestAngle(start, target, ux, uz, sky);
        if (gridResult == null) return null;

        double refineRange = Math.toRadians(INITIAL_REFINE_DEG);
        for (int iter = 0; iter < REFINE_ITERATIONS; iter++) {
            double lower = Math.max(MIN_THETA, gridResult.theta - refineRange);
            double upper = Math.min(MAX_THETA, gridResult.theta + refineRange);

            SearchResult refined = searchRefinedBestAngle(lower, upper, start, target, ux, uz, sky);
            if (refined == null || refined.errorSq >= gridResult.errorSq) break;

            gridResult = refined;
            if (gridResult.errorSq < TOLERANCE_SQ) break;

            refineRange *= 0.5;
        }

        Vec3 velocity = computeVelocity(gridResult.theta, ux, uz);
        Vec3 aimPoint = computeAimPoint(start, velocity, sky ? SKY_DISTANCE : FLAT_DISTANCE);
        long flightTimeMs = Math.round(gridResult.tick * TICK_MS);

        double flatSpeed = Math.hypot(velocity.xCoord, velocity.zCoord);
        double yaw = Math.toDegrees(Math.atan2(velocity.zCoord, velocity.xCoord)) - 90.0;
        double pitch = -Math.toDegrees(Math.atan2(velocity.yCoord, flatSpeed));

        return new PearlSolution(aimPoint, flightTimeMs, (float) yaw, (float) pitch);
    }

    private static SearchResult searchRefinedBestAngle(double minTheta, double maxTheta, Vec3 start, Vec3 target,
                                                double ux, double uz, boolean sky) {
        Comparator<SearchResult> comparator = getResultComparator(sky);
        SearchResult best = null;

        for (int i = 0; i <= REFINE_STEPS; i++) {
            double theta = minTheta + (maxTheta - minTheta) * i / REFINE_STEPS;
            if (sky && theta < MIN_SKY_THETA) continue;
            if (!sky && theta > MAX_FLAT_THETA) continue;
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

        Optional<SearchResult> result = IntStream.rangeClosed(0, GRID_STEPS).parallel()
                .mapToObj(i -> {
                    double theta = MIN_THETA + (MAX_THETA - MIN_THETA) * i / GRID_STEPS;
                    if (sky && theta < MIN_SKY_THETA) return null;
                    if (!sky && theta > MAX_FLAT_THETA) return null;
                    Vec3 velocity = computeVelocity(theta, ux, uz);
                    SimResult sim = simulateTrajectory(start, velocity, target);
                    return sim.hit ? new SearchResult(theta, sim.errorSq, sim.hitTick) : null;
                })
                .filter(Objects::nonNull)
                .min(comparator);

        return result.orElse(null);
    }

    private static Comparator<SearchResult> getResultComparator(boolean sky) {
        return Comparator
                .comparingDouble((SearchResult r) -> r.errorSq)
                .thenComparingDouble(r -> sky ? -r.theta : r.theta);
    }

    private static SimResult simulateTrajectory(Vec3 start, Vec3 initialVelocity, Vec3 target) {
        Vec3 pos = start;
        Vec3 vel = initialVelocity;

        double bestErrorSq = distanceSquared(pos, target);
        int bestTick = -1;

        double maxDistanceSq = distanceSquared(start, target) * 4;
        double minY = target.yCoord - 5;

        for (int tick = 0; tick < MAX_TICKS; tick++) {
            pos = pos.addVector(vel.xCoord, vel.yCoord, vel.zCoord);
            double errorSq = distanceSquared(pos, target);

            if (errorSq < bestErrorSq) {
                bestErrorSq = errorSq;
                bestTick = tick;
            }

            if (errorSq < HIT_RADIUS_SQ)
                return new SimResult(true, errorSq, tick);

            if (pos.yCoord < minY || distanceSquared(start, pos) > maxDistanceSq)
                break;

            vel = new Vec3(
                    vel.xCoord * DRAG,
                    (vel.yCoord - GRAVITY) * DRAG,
                    vel.zCoord * DRAG
            );
        }

        return new SimResult(false, bestErrorSq, bestTick);
    }

    private static Vec3 computeVelocity(double theta, double ux, double uz) {
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        return new Vec3(SPEED * cos * ux, SPEED * sin, SPEED * cos * uz);
    }

    private static Vec3 computeAimPoint(Vec3 start, Vec3 velocity, double distance) {
        double norm = velocity.lengthVector();
        return new Vec3(
                start.xCoord + (velocity.xCoord / norm) * distance,
                start.yCoord + (velocity.yCoord / norm) * distance,
                start.zCoord + (velocity.zCoord / norm) * distance
        );
    }

    private static double distanceSquared(Vec3 a, Vec3 b) {
        double dx = b.xCoord - a.xCoord;
        double dy = b.yCoord - a.yCoord;
        double dz = b.zCoord - a.zCoord;
        return dx * dx + dy * dy + dz * dz;
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