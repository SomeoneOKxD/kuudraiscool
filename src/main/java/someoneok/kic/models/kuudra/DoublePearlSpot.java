package someoneok.kic.models.kuudra;

import net.minecraft.util.Vec3;
import someoneok.kic.modules.kuudra.Kuudra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DoublePearlSpot {
    SLASH_XCANNON(new Vec3(-130, 78, -114), "X Cannon", SupplyPickUpSpot.X_CANNON),
    SLASH_SQUARE(new Vec3(-140, 76, -87), "Square", SupplyPickUpSpot.SQUARE),
    X_XCANNON(new Vec3(-135, 75, -123.5), "X Cannon", SupplyPickUpSpot.X_CANNON),
    X_SQUARE(new Vec3(-140, 76, -87), "Square", SupplyPickUpSpot.SQUARE),
    TRIANGLE_SHOP(new Vec3(-76, 77.5, -137), "Shop", SupplyPickUpSpot.SHOP),
    EQUALS_SHOP(new Vec3(-76, 77.5, -137), "Shop", SupplyPickUpSpot.SHOP);

    private final Vec3 location;
    private final String displayText;
    private final SupplyPickUpSpot spot;

    DoublePearlSpot(Vec3 location, String displayText, SupplyPickUpSpot spot) {
        this.location = location;
        this.displayText = displayText;
        this.spot = spot;
    }

    public Vec3 getLocation() {
        return location;
    }

    public String getDisplayText() {
        return displayText;
    }

    public SupplyPickUpSpot getSpot() {
        return spot;
    }

    public static List<DoublePearlSpot> getSpot(SupplyPickUpSpot spot) {
        List<DoublePearlSpot> result;

        switch (spot) {
            case SLASH:
                result = new ArrayList<>(Arrays.asList(SLASH_XCANNON, SLASH_SQUARE));
                break;
            case X:
                result = new ArrayList<>(Arrays.asList(X_XCANNON, X_SQUARE));
                break;
            case TRIANGLE:
                result = new ArrayList<>(Collections.singletonList(TRIANGLE_SHOP));
                break;
            case EQUALS:
                result = new ArrayList<>(Collections.singletonList(EQUALS_SHOP));
                break;
            default:
                return new ArrayList<>();
        }

        result.removeIf(dps -> dps.getSpot() == Kuudra.noPre);

        return result;
    }
}
