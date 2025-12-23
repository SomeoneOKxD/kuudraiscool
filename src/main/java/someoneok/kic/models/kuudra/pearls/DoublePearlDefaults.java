package someoneok.kic.models.kuudra.pearls;

import net.minecraft.util.Vec3;
import someoneok.kic.models.kuudra.PickupSpot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DoublePearlDefaults {
    public static final Map<String, DoublePearl> DEFAULTS;
    static {
        Map<String, DoublePearl> m = new LinkedHashMap<>();

        put(m, "SLASH->X_CANNON", new Vec3(-130, 78, -114),  PickupSpot.SLASH,    PickupSpot.X_CANNON);
        put(m, "SLASH->SQUARE",   new Vec3(-140, 76, -87),   PickupSpot.SLASH,    PickupSpot.SQUARE);
        put(m, "X->X_CANNON",     new Vec3(-135, 75, -123.5), PickupSpot.X,        PickupSpot.X_CANNON);
        put(m, "X->SQUARE",       new Vec3(-140, 76, -87),   PickupSpot.X,        PickupSpot.SQUARE);
        put(m, "TRIANGLE->SHOP",  new Vec3(-76, 77.5, -137), PickupSpot.TRIANGLE, PickupSpot.SHOP);
        put(m, "EQUALS->SHOP",    new Vec3(-76, 77.5, -137), PickupSpot.EQUALS,   PickupSpot.SHOP);

        DEFAULTS = Collections.unmodifiableMap(m);
    }

    private static void put(Map<String, DoublePearl> m, String id, Vec3 loc, PickupSpot pre, PickupSpot drop) {
        m.put(id, new DoublePearl(id, loc, pre, drop, true));
    }
}
