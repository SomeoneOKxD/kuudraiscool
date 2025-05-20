package someoneok.kic.models.request;

import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.config.pages.KuudraProfitTrackerOptions;
import someoneok.kic.models.overlay.ProfitTrackerData;
import someoneok.kic.utils.overlay.OverlayDataManager;

import static someoneok.kic.utils.GeneralUtils.colors;

public class ShareTrackerRequest {
    // ---- Data ----
    private final long profit;
    private final int runs;
    private final int freeChests;
    private final int basicChests;
    private final int hotChests;
    private final int burningChests;
    private final int fieryChests;
    private final int infernalChests;
    private final int rerolls;
    private final long time;
    private final int godRolls;
    private final long godRollValue;
    private final long essence;

    // ---- Settings ----
    private final Boolean lifetimeView;
    private final String sessionColor;
    private final String kicColor;
    private final String profitTrackerColor;
    private final Boolean showProfit;
    private final String profitColor;
    private final Boolean showRuns;
    private final String runsColor;
    private final Boolean showChests;
    private final String chestsColor;
    private final Boolean showKeyCosts;
    private final Boolean showAverageProfitPerChest;
    private final String averagePerChestColor;
    private final Boolean showRerolls;
    private final String rerollsColor;
    private final Boolean showRerollCosts;
    private final Boolean showTime;
    private final String timeColor;
    private final Boolean showAverageTimePerRun;
    private final String averageTimePerRunColor;
    private final Boolean showRate;
    private final String rateColor;
    private final Boolean showGodRolls;
    private final String godRollColor;
    private final Boolean showGodRollValue;
    private final Boolean showEssence;
    private final String essenceColor;
    private final Boolean showEssenceValue;
    private final Boolean bazaarInsta;
    private final Boolean keyInsta;
    private final Boolean kismetInsta;

    private final String faction;

    public ShareTrackerRequest(boolean lifetime) {
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();
        ProfitTrackerData.ProfitTrackerSession session = lifetime ? data.getLifetime() : data.getCurrent();

        this.lifetimeView = lifetime;
        this.profit = session.getProfit();
        this.runs = session.getRuns();
        this.freeChests = session.getFreeChests();
        this.basicChests = session.getBasicChests();
        this.hotChests = session.getHotChests();
        this.burningChests = session.getBurningChests();
        this.fieryChests = session.getFieryChests();
        this.infernalChests = session.getInfernalChests();
        this.rerolls = session.getRerolls();
        this.time = session.getTime();
        this.godRolls = session.getTotalGodRolls();
        this.godRollValue = session.getGodRollValue();
        this.essence = session.getEssence();

        this.sessionColor = colors[KuudraProfitTrackerOptions.sessionColor];
        this.kicColor = colors[KuudraProfitTrackerOptions.kicColor];
        this.profitTrackerColor = colors[KuudraProfitTrackerOptions.profitTrackerColor];
        this.showProfit = KuudraProfitTrackerOptions.showProfit;
        this.profitColor = colors[KuudraProfitTrackerOptions.profitColor];
        this.showRuns = KuudraProfitTrackerOptions.showRuns;
        this.runsColor = colors[KuudraProfitTrackerOptions.runsColor];
        this.showChests = KuudraProfitTrackerOptions.showChests;
        this.chestsColor = colors[KuudraProfitTrackerOptions.chestsColor];
        this.showKeyCosts = KuudraProfitTrackerOptions.showKeyCosts;
        this.showAverageProfitPerChest = KuudraProfitTrackerOptions.showAverageProfitPerChest;
        this.averagePerChestColor = colors[KuudraProfitTrackerOptions.averagePerChestColor];
        this.showRerolls = KuudraProfitTrackerOptions.showRerolls;
        this.rerollsColor = colors[KuudraProfitTrackerOptions.rerollsColor];
        this.showRerollCosts = KuudraProfitTrackerOptions.showRerollCosts;
        this.showTime = KuudraProfitTrackerOptions.showTime;
        this.timeColor = colors[KuudraProfitTrackerOptions.timeColor];
        this.showAverageTimePerRun = KuudraProfitTrackerOptions.showAverageTimePerRun;
        this.averageTimePerRunColor = colors[KuudraProfitTrackerOptions.averageTimePerRunColor];
        this.showRate = KuudraProfitTrackerOptions.showRate;
        this.rateColor = colors[KuudraProfitTrackerOptions.rateColor];
        this.showGodRolls = KuudraProfitTrackerOptions.showGodRolls;
        this.godRollColor = colors[KuudraProfitTrackerOptions.godRollColor];
        this.showGodRollValue = KuudraProfitTrackerOptions.showGodRollValue;
        this.showEssence = KuudraProfitTrackerOptions.showEssence;
        this.essenceColor = colors[KuudraProfitTrackerOptions.essenceColor];
        this.showEssenceValue = KuudraProfitTrackerOptions.showEssenceValue;

        this.bazaarInsta = KuudraProfitCalculatorOptions.bazaarPriceType == 0;
        this.keyInsta = KuudraProfitCalculatorOptions.keyPriceType == 0;
        this.kismetInsta = KuudraProfitCalculatorOptions.kismetPriceType == 0;

        this.faction = OverlayDataManager.getUserData().getFaction().getName();
    }
}
