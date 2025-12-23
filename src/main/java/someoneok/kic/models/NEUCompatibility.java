package someoneok.kic.models;

public final class NEUCompatibility {
    private static boolean isStorageMenuActive = false;

    public static boolean isStorageMenuActive() { return isStorageMenuActive; }

    public static void setStorageMenuActive(boolean isStorageMenuActive) {
        NEUCompatibility.isStorageMenuActive = isStorageMenuActive;
    }
}
