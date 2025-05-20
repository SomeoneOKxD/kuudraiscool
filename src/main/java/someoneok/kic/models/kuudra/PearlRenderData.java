package someoneok.kic.models.kuudra;

import net.minecraft.util.Vec3;
import someoneok.kic.modules.kuudra.Kuudra;
import someoneok.kic.modules.kuudra.Waypoints;
import someoneok.kic.utils.LocationUtils;

import static someoneok.kic.modules.kuudra.Waypoints.cachedDoubleDelay;
import static someoneok.kic.modules.kuudra.Waypoints.cachedInitialDelay;

public class PearlRenderData {
    public final Vec3 solution;
    public final float yaw;
    public final float pitch;
    private final long flightTimeMs;
    public final Vec3 target;
    public final boolean isDouble;
    public final boolean isSky;
    public String cachedDisplay;

    public void updateDisplay(boolean hasSupply, long progressStart, boolean tracking) {
        long timeLeft = getTimeUntilThrow(progressStart, tracking);
        long displayTime = Math.max(0, (timeLeft / 25) * 25);
        cachedDisplay = (displayTime <= 0 || hasSupply) ? "§a§lTHROW" : "§c" + displayTime + "ms";
    }

    public long getTimeUntilThrow(long progressStart, boolean tracking) {
        if (!tracking) return flightTimeMs;

        int base = Waypoints.KUUDRA_TIER_DELAY.getOrDefault(LocationUtils.kuudraTier, 3500);
        int delay = base + cachedInitialDelay;
        if (isDouble) delay += cachedDoubleDelay;

        return delay - flightTimeMs - (Kuudra.logicalTimeMs - progressStart);
    }

    public PearlRenderData(Vec3 solution, float yaw, float pitch, long flightTimeMs, Vec3 target, boolean isDouble, boolean isSky) {
        this.solution = solution;
        this.yaw = yaw;
        this.pitch = pitch;
        this.flightTimeMs = flightTimeMs;
        this.target = target;
        this.isDouble = isDouble;
        this.isSky = isSky;
    }
}
