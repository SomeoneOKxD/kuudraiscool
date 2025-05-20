package someoneok.kic.utils.overlay;

import someoneok.kic.models.UserData;
import someoneok.kic.models.kuudra.CrimsonFaction;
import someoneok.kic.models.overlay.ProfitTrackerData;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;

public class OverlayDataManager {
    private static ProfitTrackerData profitTrackerData = new ProfitTrackerData();
    private static UserData userData = new UserData(CrimsonFaction.MAGE, 0L);

    public static void setProfitTrackerData(ProfitTrackerData profitTrackerData) {
        OverlayDataManager.profitTrackerData = profitTrackerData;
        KuudraProfitTracker.updateTracker();
    }

    public static ProfitTrackerData getProfitTrackerData() {
        return profitTrackerData;
    }

    public static void setUserData(UserData userData) {
        OverlayDataManager.userData = userData;
    }

    public static UserData getUserData() {
        return userData;
    }
}
