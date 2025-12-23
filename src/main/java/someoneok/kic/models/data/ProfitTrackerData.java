package someoneok.kic.models.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;

import java.util.ArrayList;
import java.util.List;

public class ProfitTrackerData {
    private final ProfitTrackerSession lifetime;
    private final ProfitTrackerSession current;

    @SerializedName("kismet_price")
    private long kismetPrice;
    @SerializedName("wof_price")
    private long wofPrice;
    @SerializedName("basic_key_price")
    private long basicKeyPrice;
    @SerializedName("hot_key_price")
    private long hotKeyPrice;
    @SerializedName("burning_key_price")
    private long burningKeyPrice;
    @SerializedName("fiery_key_price")
    private long fieryKeyPrice;
    @SerializedName("infernal_key_price")
    private long infernalKeyPrice;
    @SerializedName("essence_price")
    private long essencePrice;
    @SerializedName("teeth_price")
    private long teethPrice;
    @SerializedName("tap_price")
    private long tapPrice;
    @SerializedName("twap_price")
    private long twapPrice;

    public ProfitTrackerData() {
        this.lifetime = new ProfitTrackerSession();
        this.current = new ProfitTrackerSession();
        this.kismetPrice = 0;
        this.wofPrice = 0;
        this.basicKeyPrice = 0;
        this.hotKeyPrice = 0;
        this.burningKeyPrice = 0;
        this.fieryKeyPrice = 0;
        this.infernalKeyPrice = 0;
        this.essencePrice = 0;
        this.teethPrice = 0;
        this.tapPrice = 0;
        this.twapPrice = 0;
    }

    public ProfitTrackerSession getLifetime() { return lifetime; }
    public ProfitTrackerSession getCurrent() { return current; }

    public void setKismetPrice(long kismetPrice) { this.kismetPrice = kismetPrice; }
    public void setWofPrice(long wofPrice) { this.wofPrice = wofPrice; }
    public void setBasicKeyPrice(long basicKeyPrice) { this.basicKeyPrice = basicKeyPrice; }
    public void setHotKeyPrice(long hotKeyPrice) { this.hotKeyPrice = hotKeyPrice; }
    public void setBurningKeyPrice(long burningKeyPrice) { this.burningKeyPrice = burningKeyPrice; }
    public void setFieryKeyPrice(long fieryKeyPrice) { this.fieryKeyPrice = fieryKeyPrice; }
    public void setInfernalKeyPrice(long infernalKeyPrice) { this.infernalKeyPrice = infernalKeyPrice; }
    public void setEssencePrice(long essencePrice) { this.essencePrice = essencePrice; }
    public void setTeethPrice(long teethPrice) { this.teethPrice = teethPrice; }
    public void setTapPrice(long tapPrice) { this.tapPrice = tapPrice; }
    public void setTwapPrice(long twapPrice) { this.twapPrice = twapPrice; }

    public long getKismetPrice() { return kismetPrice; }
    public long getWofPrice() { return wofPrice; }
    public long getBasicKeyPrice() { return basicKeyPrice; }
    public long getHotKeyPrice() { return hotKeyPrice; }
    public long getBurningKeyPrice() { return burningKeyPrice; }
    public long getFieryKeyPrice() { return fieryKeyPrice; }
    public long getInfernalKeyPrice() { return infernalKeyPrice; }
    public long getEssencePrice() { return essencePrice; }
    public long getTeethPrice() { return teethPrice; }
    public long getTapPrice() { return tapPrice; }
    public long getTwapPrice() { return twapPrice; }

    public void newSession() { current.reset(); }

    public void addProfit(long profit) {
        current.profit += profit;
        lifetime.profit += profit;
    }

    public void addRun() {
        current.runs++;
        lifetime.runs++;
    }

    public void addFailedRun() {
        current.failedRuns++;
        lifetime.failedRuns++;
    }

    public void addFreeChest() {
        current.freeChests++;
        lifetime.freeChests++;
    }

    public void addBasicChest() {
        current.basicChests++;
        lifetime.basicChests++;
    }

    public void addHotChest() {
        current.hotChests++;
        lifetime.hotChests++;
    }

    public void addBurningChest() {
        current.burningChests++;
        lifetime.burningChests++;
    }

    public void addFieryChest() {
        current.fieryChests++;
        lifetime.fieryChests++;
    }

    public void addInfernalChest() {
        current.infernalChests++;
        lifetime.infernalChests++;
    }

    public void addReroll() {
        current.rerolls++;
        lifetime.rerolls++;
    }

    public void addShardReroll() {
        current.shardRerolls++;
        lifetime.shardRerolls++;
    }

    public void addTime(long duration) {
        current.time += duration;
        lifetime.time += duration;
    }

    public void addEssence(long essence) {
        current.essence += essence;
        lifetime.essence += essence;
    }

    public void addTeeth(int teeth) {
        current.teeth += teeth;
        lifetime.teeth += teeth;
    }

    public void addValuable(String valuable) {
        if (valuable == null || valuable.isEmpty()) return;
        current.valuables.add(valuable);
        lifetime.valuables.add(valuable);
    }

    public void addTap(int tap) {
        current.tap += tap;
        lifetime.tap += tap;
    }

    public void addTwap(int twap) {
        current.twap += twap;
        lifetime.twap += twap;
    }

    public void reset() {
        current.reset();
        lifetime.reset();
    }

    public static class ProfitTrackerSession {
        @Expose
        @SerializedName("profit")
        private long profit;

        @Expose
        @SerializedName("runs")
        private int runs;

        @Expose
        @SerializedName("failed_runs")
        private int failedRuns;

        @Expose
        @SerializedName("rerolls")
        private int rerolls;

        @Expose
        @SerializedName("shardRerolls")
        private int shardRerolls;

        @Expose
        @SerializedName("time")
        private long time;

        @Expose
        @SerializedName("free_chests")
        private int freeChests;

        @Expose
        @SerializedName("basic_chests")
        private int basicChests;

        @Expose
        @SerializedName("hot_chests")
        private int hotChests;

        @Expose
        @SerializedName("burning_chests")
        private int burningChests;

        @Expose
        @SerializedName("fiery_chests")
        private int fieryChests;

        @Expose
        @SerializedName("infernal_chests")
        private int infernalChests;

        @Expose
        @SerializedName("essence")
        private long essence;

        @Expose
        @SerializedName("teeth")
        private int teeth;

        @Expose
        @SerializedName("tap")
        private int tap;

        @Expose
        @SerializedName("twap")
        private int twap;

        // Format type;itemId;lbPrice/buyPrice;avgPrice/sellPrice
        @Expose
        @SerializedName("valuables")
        private List<String> valuables;

        public ProfitTrackerSession() { reset(); }

        public long getProfit(ProfitTrackerData data) { return profit - getRerollCost(data) - getTapCost(data); }
        public int getRuns() { return runs; }
        public int getChests() { return freeChests + basicChests + hotChests + burningChests + fieryChests + infernalChests; }
        public int getRerolls() { return rerolls; }
        public int getShardRerolls() { return shardRerolls; }
        public long getTime() { return time; }
        public int getFailedRuns() { return failedRuns; }
        public int getFreeChests() { return freeChests; }
        public int getBasicChests() { return basicChests; }
        public int getHotChests() { return hotChests; }
        public int getBurningChests() { return burningChests; }
        public int getFieryChests() { return fieryChests; }
        public int getInfernalChests() { return infernalChests; }
        public long getEssence() { return essence; }
        public int getTeeth() { return teeth; }
        public int getTap() { return tap; }
        public int getTwap() { return twap; }
        public List<String> getValuables() { return valuables; }
        public int getTotalValuables() { return valuables.size(); }
        public long getRerollCost(ProfitTrackerData data) { return (rerolls * data.getKismetPrice()) + (shardRerolls * data.getWofPrice()); }
        public long getTotalEssenceValue(ProfitTrackerData data) { return essence * data.getEssencePrice(); }
        public long getTotalTeethValue(ProfitTrackerData data) { return teeth * data.getTeethPrice(); }
        public long getTapCost(ProfitTrackerData data) { return (tap * data.getTapPrice()) + (twap * data.getTwapPrice()); }
        public long getAverage() { return this.getChests() == 0 ? 0 : profit / this.getChests(); }
        public long getAverageRunTime() { return time == 0 || runs == 0 ? 0 : time / runs; }
        public long getHourlyRate() { return time == 0 ? 0 : (profit * 3600) / time; }

        public long getValuablesValue() {
            return valuables.stream()
                    .map(line -> line.split(";"))
                    .filter(parts -> parts.length >= 4)
                    .mapToLong(parts -> {
                        try {
                            boolean usePrice1 = false;
                            if (parts[0].equalsIgnoreCase("BAZAAR")) {
                                usePrice1 = KuudraProfitCalculatorOptions.bazaarPriceType == 1;
                            } else if (parts[0].equalsIgnoreCase("AUCTION")) {
                                usePrice1 = KuudraProfitCalculatorOptions.auctionPriceType == 0;
                            }

                            return usePrice1 ? Long.parseLong(parts[2]) : Long.parseLong(parts[3]);
                        } catch (NumberFormatException e) {
                            return 0L;
                        }
                    })
                    .sum();
        }

        public long getChestsCost(ProfitTrackerData data) {
            return (basicChests * data.getBasicKeyPrice()) +
                    (hotChests * data.getHotKeyPrice()) +
                    (burningChests * data.getBurningKeyPrice()) +
                    (fieryChests * data.getFieryKeyPrice()) +
                    (infernalChests * data.getInfernalKeyPrice());
        }

        private void reset() {
            this.profit = 0;
            this.runs = 0;
            this.failedRuns = 0;
            this.rerolls = 0;
            this.shardRerolls = 0;
            this.time = 0;
            this.freeChests = 0;
            this.hotChests = 0;
            this.basicChests = 0;
            this.burningChests = 0;
            this.fieryChests = 0;
            this.infernalChests = 0;
            this.essence = 0;
            this.teeth = 0;
            this.tap = 0;
            this.twap = 0;
            this.valuables = new ArrayList<>();
        }
    }
}
