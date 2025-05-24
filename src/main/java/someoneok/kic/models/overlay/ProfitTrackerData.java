package someoneok.kic.models.overlay;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ProfitTrackerData {
    private final ProfitTrackerSession lifetime;
    private final ProfitTrackerSession current;
    private final Map<Integer, ProfitTrackerSession> tierSessions;
    private final Map<Integer, ProfitTrackerSession> lifetimeTierSessions;
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
        this.tierSessions = new HashMap<>();
        this.lifetimeTierSessions = new HashMap<>();

        for (int i = 0; i <= 5; i++) {
            this.tierSessions.put(i, new ProfitTrackerSession());
            this.lifetimeTierSessions.put(i, new ProfitTrackerSession());
        }

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
    public Map<Integer, ProfitTrackerSession> getTierSessions() { return tierSessions; }
    public Map<Integer, ProfitTrackerSession> getLifetimeTierSessions() { return lifetimeTierSessions; }

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

    public void addProfit(long amount, int tier) {
        current.profit += amount;
        lifetime.profit += amount;
        tierSessions.get(tier).profit += amount;
        lifetimeTierSessions.get(tier).profit += amount;
    }

    public void addRun(int tier) {
        current.runs++;
        lifetime.runs++;
        tierSessions.get(tier).runs++;
        lifetimeTierSessions.get(tier).runs++;
    }

    public void addFailedRun(int tier) {
        current.failedRuns++;
        lifetime.failedRuns++;
        tierSessions.get(tier).failedRuns++;
        lifetimeTierSessions.get(tier).failedRuns++;
    }

    public void addFreeChest(int tier) {
        current.freeChests++;
        lifetime.freeChests++;
        tierSessions.get(tier).freeChests++;
        lifetimeTierSessions.get(tier).freeChests++;
    }

    public void addBasicChest(int tier) {
        current.basicChests++;
        lifetime.basicChests++;
        tierSessions.get(tier).basicChests++;
        lifetimeTierSessions.get(tier).basicChests++;
    }

    public void addHotChest(int tier) {
        current.hotChests++;
        lifetime.hotChests++;
        tierSessions.get(tier).hotChests++;
        lifetimeTierSessions.get(tier).hotChests++;
    }

    public void addBurningChest(int tier) {
        current.burningChests++;
        lifetime.burningChests++;
        tierSessions.get(tier).burningChests++;
        lifetimeTierSessions.get(tier).burningChests++;
    }

    public void addFieryChest(int tier) {
        current.fieryChests++;
        lifetime.fieryChests++;
        tierSessions.get(tier).fieryChests++;
        lifetimeTierSessions.get(tier).fieryChests++;
    }

    public void addInfernalChest(int tier) {
        current.infernalChests++;
        lifetime.infernalChests++;
        tierSessions.get(tier).infernalChests++;
        lifetimeTierSessions.get(tier).infernalChests++;
    }

    public void addPaidChest(int tier) {
        current.paidChests++;
        lifetime.paidChests++;
        tierSessions.get(tier).paidChests++;
        lifetimeTierSessions.get(tier).paidChests++;
    }

    public void addReroll(int tier) {
        current.rerolls++;
        lifetime.rerolls++;
        tierSessions.get(tier).rerolls++;
        lifetimeTierSessions.get(tier).rerolls++;
    }

    public void addTime(long duration, int tier) {
        current.time += duration;
        lifetime.time += duration;
        tierSessions.get(tier).time += duration;
        lifetimeTierSessions.get(tier).time += duration;
    }

    public void addEssence(long essence, int tier) {
        current.essence += essence;
        lifetime.essence += essence;
        tierSessions.get(tier).essence += essence;
        lifetimeTierSessions.get(tier).essence += essence;
    }

    public void addTeeth(int teeth, int tier) {
        current.teeth += teeth;
        lifetime.teeth += teeth;
        tierSessions.get(tier).teeth += teeth;
        lifetimeTierSessions.get(tier).teeth += teeth;
    }

    public void addGodRoll(String gr, int tier) {
        current.godRolls.add(gr);
        lifetime.godRolls.add(gr);
        tierSessions.get(tier).godRolls.add(gr);
        lifetimeTierSessions.get(tier).godRolls.add(gr);
    }

    public void newSession() {
        current.reset();
        for (ProfitTrackerSession session : tierSessions.values()) session.reset();
    }

    public void reset() {
        current.reset();
        lifetime.reset();
        for (ProfitTrackerSession session : tierSessions.values()) session.reset();
        for (ProfitTrackerSession session : lifetimeTierSessions.values()) session.reset();
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

        @Expose
        @SerializedName("paid_chests")
        private int paidChests;
        public int getPaidChests() { return paidChests; }

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

        public void reset() {
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
