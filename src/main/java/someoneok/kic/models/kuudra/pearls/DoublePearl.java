package someoneok.kic.models.kuudra.pearls;

import net.minecraft.util.Vec3;
import someoneok.kic.models.kuudra.PickupSpot;

public final class DoublePearl {
    private final String id;
    private final Vec3 location;
    private final PickupSpot pre;
    private final PickupSpot drop;
    private final boolean isDefault;

    public DoublePearl(String id, Vec3 location, PickupSpot pre, PickupSpot drop, boolean isDefault) {
        this.id = id;
        this.location = location;
        this.pre = pre;
        this.drop = drop;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public Vec3 getLocation() { return location; }
    public PickupSpot getPre() { return pre; }
    public PickupSpot getDrop() { return drop; }
    public boolean isDefault() { return isDefault; }

    @Override public String toString() { return id; }
}
