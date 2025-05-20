package someoneok.kic.models;

public class SkytilsCompatibility {
    private static boolean isAuctionPriceScreenActive = false;

    public static boolean isAuctionPriceScreenActive() {
        return isAuctionPriceScreenActive;
    }

    public static void setAuctionPriceScreenActive(boolean isAuctionPriceScreenActive) {
        SkytilsCompatibility.isAuctionPriceScreenActive = isAuctionPriceScreenActive;
    }
}
