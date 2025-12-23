package someoneok.kic.models.kuudra;

import net.minecraft.util.Vec3;

public enum PickupSpot {
    SHOP(    new Vec3(-81.0, 76.0, -143.0),  "Shop", 1),
    X(       new Vec3(-142.5, 77.0, -148.0), "X", 2),
    X_CANNON(new Vec3(-143.0, 76.0, -125.0), "X Cannon", 3),
    EQUALS(  new Vec3(-65.5, 76.0, -87.5),   "Equals", 4),
    SLASH(   new Vec3(-113.5, 77.0, -68.5),  "Slash", 5),
    TRIANGLE(new Vec3(-67.5, 77.0, -122.5),  "Triangle", 6),
    SQUARE(  new Vec3(-143.0, 76.0, -80.0),  "Square", -1),
    NONE(    new Vec3(0.0, 0.0, 0.0),        "None", -1);

    private final Vec3 location;
    private final String displayText;
    private final int supplyId;

    PickupSpot(Vec3 location, String displayText, int supplyId) {
        this.location = location;
        this.displayText = displayText;
        this.supplyId = supplyId;
    }

    public Vec3 getLocation() { return location; }
    public String getDisplayText() { return displayText; }
    public int getSupplyId() { return supplyId; }

    private static final PickupSpot[] SPOTS = values();

    public static PickupSpot getClosestSpot(Vec3 eyePos) {
        PickupSpot closest = NONE;
        double bestDistSq = Double.MAX_VALUE;

        for (PickupSpot spot : SPOTS) {
            if (spot == NONE) continue;
            double distSq = eyePos.squareDistanceTo(spot.location);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                closest = spot;
            }
        }

        return closest;
    }
}
