package someoneok.kic.events.hooks;

import someoneok.kic.utils.mouse.MouseSensitivityManager;

public class MouseSensitivityHook {
    public static final MouseSensitivityHook INSTANCE = new MouseSensitivityHook();

    private MouseSensitivityHook() {}

    public float remapSensitivity(float original) {
        float actualSensitivity = (original - 0.2f) / 0.6f;
        return MouseSensitivityManager.getSensitivity(actualSensitivity) * 0.6f + 0.2f;
    }
}
