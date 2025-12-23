package someoneok.kic.models.request;

import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.config.pages.KuudraProfitTrackerOptions;
import someoneok.kic.models.data.ProfitTrackerData;
import someoneok.kic.utils.data.DataManager;

public class ShareTrackerRequest {
    public final String[] COLORS = {"Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
            "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
            "Yellow", "White"};

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
    private final int shardRerolls;
    private final long time;
    private final int valuables;
    private final long valuablesValue;
    private final long essence;
    private final int tap;
    private final int twap;
    private final long tapCost;

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
    private final Boolean showValuables;
    private final String valuablesColor;
    private final Boolean showValuablesValue;
    private final Boolean showEssence;
    private final String essenceColor;
    private final Boolean showEssenceValue;
    private final Boolean showTap;
    private final String tapColor;
    private final Boolean showTapCosts;
    private final Boolean bazaarInsta;
    private final Boolean keyInsta;
    private final Boolean kismetInsta;
    private final Boolean wofLb;

    private final String faction;

    public ShareTrackerRequest(boolean lifetime) {
        ProfitTrackerData data = DataManager.getProfitTrackerData();
        ProfitTrackerData.ProfitTrackerSession session = lifetime ? data.getLifetime() : data.getCurrent();

        this.lifetimeView = lifetime;
        this.profit = session.getProfit(data);
        this.runs = session.getRuns();
        this.freeChests = session.getFreeChests();
        this.basicChests = session.getBasicChests();
        this.hotChests = session.getHotChests();
        this.burningChests = session.getBurningChests();
        this.fieryChests = session.getFieryChests();
        this.infernalChests = session.getInfernalChests();
        this.rerolls = session.getRerolls();
        this.shardRerolls = session.getShardRerolls();
        this.time = session.getTime();
        this.valuables = session.getTotalValuables();
        this.valuablesValue = session.getValuablesValue();
        this.essence = session.getEssence();
        this.tap = session.getTap();
        this.twap = session.getTwap();
        this.tapCost = session.getTapCost(data);

        this.sessionColor = COLORS[KuudraProfitTrackerOptions.sessionColor];
        this.kicColor = COLORS[KuudraProfitTrackerOptions.kicColor];
        this.profitTrackerColor = COLORS[KuudraProfitTrackerOptions.profitTrackerColor];
        this.showProfit = KuudraProfitTrackerOptions.showProfit;
        this.profitColor = COLORS[KuudraProfitTrackerOptions.profitColor];
        this.showRuns = KuudraProfitTrackerOptions.showRuns;
        this.runsColor = COLORS[KuudraProfitTrackerOptions.runsColor];
        this.showChests = KuudraProfitTrackerOptions.showChests;
        this.chestsColor = COLORS[KuudraProfitTrackerOptions.chestsColor];
        this.showKeyCosts = KuudraProfitTrackerOptions.showKeyCosts;
        this.showAverageProfitPerChest = KuudraProfitTrackerOptions.showAverageProfitPerChest;
        this.averagePerChestColor = COLORS[KuudraProfitTrackerOptions.averagePerChestColor];
        this.showRerolls = KuudraProfitTrackerOptions.showRerolls;
        this.rerollsColor = COLORS[KuudraProfitTrackerOptions.rerollsColor];
        this.showRerollCosts = KuudraProfitTrackerOptions.showRerollCosts;
        this.showTime = KuudraProfitTrackerOptions.showTime;
        this.timeColor = COLORS[KuudraProfitTrackerOptions.timeColor];
        this.showAverageTimePerRun = KuudraProfitTrackerOptions.showAverageTimePerRun;
        this.averageTimePerRunColor = COLORS[KuudraProfitTrackerOptions.averageTimePerRunColor];
        this.showRate = KuudraProfitTrackerOptions.showRate;
        this.rateColor = COLORS[KuudraProfitTrackerOptions.rateColor];
        this.showValuables = KuudraProfitTrackerOptions.showValuables;
        this.valuablesColor = COLORS[KuudraProfitTrackerOptions.valuableColor];
        this.showValuablesValue = KuudraProfitTrackerOptions.showValuablesValue;
        this.showEssence = KuudraProfitTrackerOptions.showEssence;
        this.essenceColor = COLORS[KuudraProfitTrackerOptions.essenceColor];
        this.showEssenceValue = KuudraProfitTrackerOptions.showEssenceValue;
        this.showTap = KuudraProfitTrackerOptions.showTap;
        this.tapColor = COLORS[KuudraProfitTrackerOptions.tapColor];
        this.showTapCosts = KuudraProfitTrackerOptions.showTapCosts;

        this.bazaarInsta = KuudraProfitCalculatorOptions.bazaarPriceType == 0;
        this.keyInsta = KuudraProfitCalculatorOptions.keyPriceType == 0;
        this.kismetInsta = KuudraProfitCalculatorOptions.kismetPriceType == 0;
        this.wofLb = KuudraProfitCalculatorOptions.auctionPriceType == 0;

        this.faction = DataManager.getUserData().getFaction().getName();
    }
}
