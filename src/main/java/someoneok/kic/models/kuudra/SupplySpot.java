package someoneok.kic.models.kuudra;

import net.minecraft.util.Vec3;

public enum SupplySpot {
    SUPPLY1(new Vec3(-98.0, 79.0, -112.94)),
    SUPPLY2(new Vec3(-106.0, 79.0, -112.94)),
    SUPPLY3(new Vec3(-110.0, 79.0, -106.0)),
    SUPPLY4(new Vec3(-106.0, 79.0, -99.06)),
    SUPPLY5(new Vec3(-98.0, 79.0, -99.06)),
    SUPPLY6(new Vec3(-94.0, 79.0, -106.0));

    private final Vec3 location;
    private final int[] intLocation;

    SupplySpot(Vec3 location) {
        this.location = location;
        this.intLocation = new int[]{(int) location.xCoord, (int) location.yCoord, (int) location.zCoord};
    }

    public Vec3 getLocation() { return location; }
    public int getX() { return intLocation[0]; }
    public int getY() { return intLocation[1]; }
    public int getZ() { return intLocation[2]; }
}
