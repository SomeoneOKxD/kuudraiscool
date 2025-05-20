package someoneok.kic.models;

public final class NEUCompatibility {
    private static boolean isStorageMenuActive = false;
    private static boolean isTradeWindowActive = false;

    public static boolean isStorageMenuActive() {
        return isStorageMenuActive;
    }

    public static void setStorageMenuActive(boolean isStorageMenuActive) {
        NEUCompatibility.isStorageMenuActive = isStorageMenuActive;
    }

    public static boolean isTradeWindowActive() {
        return isTradeWindowActive;
    }

    public static void setTradeWindowActive(boolean isTradeWindowActive) {
        NEUCompatibility.isTradeWindowActive = isTradeWindowActive;
    }
}
