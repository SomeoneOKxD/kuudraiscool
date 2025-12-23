package someoneok.kic.utils.data;

import someoneok.kic.models.data.EmptySlotData;
import someoneok.kic.models.data.ProfitTrackerData;
import someoneok.kic.models.data.UserData;
import someoneok.kic.models.kuudra.CrimsonFaction;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;

public class DataManager {
    private static ProfitTrackerData profitTrackerData = new ProfitTrackerData();
    private static UserData userData = new UserData(CrimsonFaction.MAGE, 0L);
    private static EmptySlotData emptySlotData = new EmptySlotData();

    public static void setProfitTrackerData(ProfitTrackerData profitTrackerData) {
        DataManager.profitTrackerData = profitTrackerData;
        KuudraProfitTracker.updateTracker();
    }

    public static ProfitTrackerData getProfitTrackerData() {
        return profitTrackerData;
    }

    public static void setUserData(UserData userData) {
        DataManager.userData = userData;
    }

    public static UserData getUserData() {
        return userData;
    }

    public static EmptySlotData getEmptySlotData() {
        return emptySlotData;
    }

    public static void setEmptySlotData(EmptySlotData emptySlotData) {
        DataManager.emptySlotData = emptySlotData;
    }
}
