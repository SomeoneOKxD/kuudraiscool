package someoneok.kic.utils.mouse;

import someoneok.kic.models.misc.SensitivityState;

public class MouseSensitivityManager {
    private static float lastIn = Float.NaN;
    private static float lastOut = Float.NaN;

    public static SensitivityState state = SensitivityState.UNCHANGED;

    public static float getSensitivity(float original) {
        if (original != lastIn) {
            lastIn = original;
            lastOut = state.apply(original);
        }
        return lastOut;
    }

    public static void destroyCache() {
        lastIn = Float.NaN;
        lastOut = Float.NaN;
    }

    public static void setState(SensitivityState newState) {
        state = newState;
        destroyCache();
    }
}
