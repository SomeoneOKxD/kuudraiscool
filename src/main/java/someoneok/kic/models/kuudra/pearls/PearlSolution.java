package someoneok.kic.models.kuudra.pearls;

import net.minecraft.util.Vec3;

public class PearlSolution {
    public final Vec3 solution;
    public final long flightTimeMs;
    public final float yaw;
    public final float pitch;

    public PearlSolution(Vec3 solution, long flightTimeMs, float yaw, float pitch) {
        this.solution = solution;
        this.flightTimeMs = flightTimeMs;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
