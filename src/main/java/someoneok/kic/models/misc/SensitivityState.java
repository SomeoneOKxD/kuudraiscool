package someoneok.kic.models.misc;

import someoneok.kic.utils.mouse.MouseSensitivityManager;

public enum SensitivityState {
    UNCHANGED(Float.NaN),
    LOCKED(-1f / 3f);

    private final float value;

    SensitivityState(float value) {
        this.value = value;
    }

    public float apply(float original) {
        return Float.isNaN(value) ? original : value;
    }

    public boolean isActive() {
        return MouseSensitivityManager.state == this;
    }
}
