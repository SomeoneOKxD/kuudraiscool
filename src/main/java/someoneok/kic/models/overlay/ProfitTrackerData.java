package someoneok.kic.models.overlay;

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

    public ProfitTrackerData() {
        this.lifetime = new ProfitTrackerSession();
        this.current = new ProfitTrackerSession();
        this.kismetPrice = 0;
        this.basicKeyPrice = 0;
        this.hotKeyPrice = 0;
        this.burningKeyPrice = 0;
        this.fieryKeyPrice = 0;
        this.infernalKeyPrice = 0;
        this.essencePrice = 0;
        this.teethPrice = 0;
    }

    public ProfitTrackerSession getLifetime() { return lifetime; }

    public ProfitTrackerSession getCurrent() { return current; }

    public void setKismetPrice(long kismetPrice) { this.kismetPrice = kismetPrice; }
    public void setBasicKeyPrice(long basicKeyPrice) { this.basicKeyPrice = basicKeyPrice; }
    public void setHotKeyPrice(long hotKeyPrice) { this.hotKeyPrice = hotKeyPrice; }
    public void setBurningKeyPrice(long burningKeyPrice) { this.burningKeyPrice = burningKeyPrice; }
    public void setFieryKeyPrice(long fieryKeyPrice) { this.fieryKeyPrice = fieryKeyPrice; }
    public void setInfernalKeyPrice(long infernalKeyPrice) { this.infernalKeyPrice = infernalKeyPrice; }
    public void setEssencePrice(long essencePrice) { this.essencePrice = essencePrice; }
    public void setTeethPrice(long teethPrice) { this.teethPrice = teethPrice; }
    public long getKismetPrice() { return kismetPrice; }
    public long getBasicKeyPrice() { return basicKeyPrice; }
    public long getHotKeyPrice() { return hotKeyPrice; }
    public long getBurningKeyPrice() { return burningKeyPrice; }
    public long getFieryKeyPrice() { return fieryKeyPrice; }
    public long getInfernalKeyPrice() { return infernalKeyPrice; }
    public long getEssencePrice() { return essencePrice; }
    public long getTeethPrice() { return teethPrice; }

    public void addProfit(long amount) {
        current.profit += amount;
        lifetime.profit += amount;
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

    public void addGodRoll(String gr) {
        current.godRolls.add(gr);
        lifetime.godRolls.add(gr);
    }

    public void newSession() {
        current.reset();
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

        // Format uuid;itemId;attribute1;attribute1lvl;attribute2;attribute2lvl;lbPrice;avgPrice
        @Expose
        @SerializedName("god_rolls")
        private List<String> godRolls;

        public ProfitTrackerSession() {
            this.profit = 0;
            this.runs = 0;
            this.failedRuns = 0;
            this.rerolls = 0;
            this.time = 0;
            this.freeChests = 0;
            this.hotChests = 0;
            this.basicChests = 0;
            this.burningChests = 0;
            this.fieryChests = 0;
            this.infernalChests = 0;
            this.essence = 0;
            this.teeth = 0;
            this.godRolls = new ArrayList<>();
        }

        public long getProfit() { return profit; }
        public int getRuns() { return runs; }
        public int getChests() { return freeChests + basicChests + hotChests + burningChests + fieryChests + infernalChests; }
        public int getRerolls() { return rerolls; }
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
        public List<String> getGodRolls() { return godRolls; }
        public int getTotalGodRolls() { return godRolls.size(); }

        public long getGodRollValue() {
            boolean useLb = KuudraProfitCalculatorOptions.godRollPriceType == 0;

            return godRolls.stream()
                    .map(line -> line.split(";"))
                    .filter(parts -> parts.length >= 8)
                    .mapToLong(parts -> {
                        try {
                            return useLb ? Long.parseLong(parts[6]) : Long.parseLong(parts[7]);
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

        public long getRerollCost(ProfitTrackerData data) {
            return rerolls * data.getKismetPrice();
        }

        public long getTotalEssenceValue(ProfitTrackerData data) {
            return essence * data.getEssencePrice();
        }

        public long getTotalTeethValue(ProfitTrackerData data) {
            return teeth * data.getTeethPrice();
        }

        public long getAverage() { return this.getChests() == 0 ? 0 : profit / this.getChests(); }
        public long getAverageRunTime() { return time == 0 || runs == 0 ? 0 : time / runs; }
        public long getHourlyRate() { return time == 0 ? 0 : (profit * 3600) / time; }

        private void reset() {
            this.profit = 0;
            this.runs = 0;
            this.failedRuns = 0;
            this.rerolls = 0;
            this.time = 0;
            this.freeChests = 0;
            this.hotChests = 0;
            this.basicChests = 0;
            this.burningChests = 0;
            this.fieryChests = 0;
            this.infernalChests = 0;
            this.essence = 0;
            this.teeth = 0;
            this.godRolls = new ArrayList<>();
        }
    }
}
