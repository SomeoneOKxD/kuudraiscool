package someoneok.kic.models.kuudra;

public class AutoKickProfile {
    private final String user;
    private final String uuid;

    private final int cataLevel;
    private final int foragingLevel;
    private final int t5Comps;
    private final int magicalPower;
    private final int ragChimLevel;
    private final int termDuplexLevel;
    private final int talismanTier;

    private final String ragGemstone;
    private final String chestplate;
    private final String leggings;
    private final String boots;

    private final boolean termP7;
    private final boolean termC6;
    private final boolean termSmold5;
    private final boolean witherImpact;

    private final int legionLevel;
    private final int strongFeroMana;

    private final boolean rend;
    private final boolean rendTerm;
    private final boolean boneP7;
    private final boolean boneC6;
    private final boolean boneSmold5;
    private final boolean bladeGK7;
    private final boolean bladePros6;
    private final int habLevel;
    private final boolean biggerTeeth;

    private final long bankBalance;
    private final long goldCollection;
    private final int goldenDragonLevel;
    private final boolean hephaestusRemedies;

    private final boolean inventoryApi;
    private final boolean bankingApi;
    private final boolean collectionsApi;

    private AutoKickProfile(Builder b) {
        this.user = b.user;
        this.uuid = b.uuid;
        this.cataLevel = b.cataLevel;
        this.foragingLevel = b.foragingLevel;
        this.t5Comps = b.t5Comps;
        this.magicalPower = b.magicalPower;
        this.ragChimLevel = b.ragChimLevel;
        this.termDuplexLevel = b.termDuplexLevel;
        this.talismanTier = b.talismanTier;
        this.ragGemstone = b.ragGemstone;
        this.chestplate = b.chestplate;
        this.leggings = b.leggings;
        this.boots = b.boots;
        this.termP7 = b.termP7;
        this.termC6 = b.termC6;
        this.termSmold5 = b.termSmold5;
        this.witherImpact = b.witherImpact;
        this.legionLevel = b.legionLevel;
        this.strongFeroMana = b.strongFeroMana;
        this.rend = b.rend;
        this.rendTerm = b.rendTerm;
        this.boneP7 = b.boneP7;
        this.boneC6 = b.boneC6;
        this.boneSmold5 = b.boneSmold5;
        this.bladeGK7 = b.bladeGK7;
        this.bladePros6 = b.bladePros6;
        this.habLevel = b.habLevel;
        this.biggerTeeth = b.biggerTeeth;
        this.bankBalance = b.bankBalance;
        this.goldCollection = b.goldCollection;
        this.goldenDragonLevel = b.goldenDragonLevel;
        this.hephaestusRemedies = b.hephaestusRemedies;
        this.inventoryApi = b.inventoryApi;
        this.bankingApi = b.bankingApi;
        this.collectionsApi = b.collectionsApi;
    }

    public String getUser() { return user; }
    public String getUuid() { return uuid; }
    public int getCataLevel() { return cataLevel; }
    public int getForagingLevel() { return foragingLevel; }
    public int getT5Comps() { return t5Comps; }
    public int getMagicalPower() { return magicalPower; }
    public int getRagChimLevel() { return ragChimLevel; }
    public int getTermDuplexLevel() { return termDuplexLevel; }
    public int getTalismanTier() { return talismanTier; }
    public String getRagGemstone() { return ragGemstone; }
    public String getChestplate() { return chestplate; }
    public String getLeggings() { return leggings; }
    public String getBoots() { return boots; }
    public boolean isTermP7() { return termP7; }
    public boolean isTermC6() { return termC6; }
    public boolean isTermSmold5() { return termSmold5; }
    public boolean hasWitherImpact() { return witherImpact; }
    public int getLegionLevel() { return legionLevel; }
    public int getStrongFeroMana() { return strongFeroMana; }
    public boolean hasRend() { return rend; }
    public boolean hasRendTerm() { return rendTerm; }
    public boolean isBoneP7() { return boneP7; }
    public boolean isBoneC6() { return boneC6; }
    public boolean isBoneSmold5() { return boneSmold5; }
    public boolean isBladeGK7() { return bladeGK7; }
    public boolean isBladePros6() { return bladePros6; }
    public int getHabLevel() { return habLevel; }
    public boolean hasBiggerTeeth() { return biggerTeeth; }
    public long getBankBalance() { return bankBalance; }
    public long getGoldCollection() { return goldCollection; }
    public int getGoldenDragonLevel() { return goldenDragonLevel; }
    public boolean hasHephaestusRemedies() { return hephaestusRemedies; }
    public boolean hasInventoryApi() { return inventoryApi; }
    public boolean hasBankingApi() { return bankingApi; }
    public boolean hasCollectionsApi() { return collectionsApi; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String user;
        private String uuid;

        private int cataLevel;
        private int foragingLevel;
        private int t5Comps;
        private int magicalPower;
        private int ragChimLevel;
        private int termDuplexLevel;
        private int talismanTier;

        private String ragGemstone;
        private String chestplate;
        private String leggings;
        private String boots;

        private boolean termP7;
        private boolean termC6;
        private boolean termSmold5;
        private boolean witherImpact;

        private int legionLevel;
        private int strongFeroMana;

        private boolean rend;
        private boolean rendTerm;
        private boolean boneP7;
        private boolean boneC6;
        private boolean boneSmold5;
        private boolean bladeGK7;
        private boolean bladePros6;
        private int habLevel;
        private boolean biggerTeeth;

        private long bankBalance;
        private long goldCollection;
        private int goldenDragonLevel;
        private boolean hephaestusRemedies;

        private boolean inventoryApi;
        private boolean bankingApi;
        private boolean collectionsApi;

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder cataLevel(int cataLevel) {
            this.cataLevel = cataLevel;
            return this;
        }

        public Builder foragingLevel(int foragingLevel) {
            this.foragingLevel = foragingLevel;
            return this;
        }

        public Builder t5Comps(int t5Comps) {
            this.t5Comps = t5Comps;
            return this;
        }

        public Builder magicalPower(int magicalPower) {
            this.magicalPower = magicalPower;
            return this;
        }

        public Builder ragChimLevel(int ragChimLevel) {
            this.ragChimLevel = ragChimLevel;
            return this;
        }

        public Builder termDuplexLevel(int termDuplexLevel) {
            this.termDuplexLevel = termDuplexLevel;
            return this;
        }

        public Builder talismanTier(int talismanTier) {
            this.talismanTier = talismanTier;
            return this;
        }

        public Builder ragGemstone(String ragGemstone) {
            this.ragGemstone = ragGemstone;
            return this;
        }

        public Builder chestplate(String chestplate) {
            this.chestplate = chestplate;
            return this;
        }

        public Builder leggings(String leggings) {
            this.leggings = leggings;
            return this;
        }

        public Builder boots(String boots) {
            this.boots = boots;
            return this;
        }

        public Builder termP7(boolean termP7) {
            this.termP7 = termP7;
            return this;
        }

        public Builder termC6(boolean termC6) {
            this.termC6 = termC6;
            return this;
        }

        public Builder termSmold5(boolean termSmold5) {
            this.termSmold5 = termSmold5;
            return this;
        }

        public Builder witherImpact(boolean witherImpact) {
            this.witherImpact = witherImpact;
            return this;
        }

        public Builder legionLevel(int legionLevel) {
            this.legionLevel = legionLevel;
            return this;
        }

        public Builder strongFeroMana(int strongFeroMana) {
            this.strongFeroMana = strongFeroMana;
            return this;
        }

        public Builder rend(boolean rend) {
            this.rend = rend;
            return this;
        }

        public Builder rendTerm(boolean rendTerm) {
            this.rendTerm = rendTerm;
            return this;
        }

        public Builder boneP7(boolean boneP7) {
            this.boneP7 = boneP7;
            return this;
        }

        public Builder boneC6(boolean boneC6) {
            this.boneC6 = boneC6;
            return this;
        }

        public Builder boneSmold5(boolean boneSmold5) {
            this.boneSmold5 = boneSmold5;
            return this;
        }

        public Builder bladeGK7(boolean bladeGK7) {
            this.bladeGK7 = bladeGK7;
            return this;
        }

        public Builder bladePros6(boolean bladePros6) {
            this.bladePros6 = bladePros6;
            return this;
        }

        public Builder habLevel(int habLevel) {
            this.habLevel = habLevel;
            return this;
        }

        public Builder biggerTeeth(boolean biggerTeeth) {
            this.biggerTeeth = biggerTeeth;
            return this;
        }

        public Builder bankBalance(long bankBalance) {
            this.bankBalance = bankBalance;
            return this;
        }

        public Builder goldCollection(long goldCollection) {
            this.goldCollection = goldCollection;
            return this;
        }

        public Builder goldenDragonLevel(int goldenDragonLevel) {
            this.goldenDragonLevel = goldenDragonLevel;
            return this;
        }

        public Builder hephaestusRemedies(boolean hephaestusRemedies) {
            this.hephaestusRemedies = hephaestusRemedies;
            return this;
        }

        public Builder inventoryApi(boolean inventoryApi) {
            this.inventoryApi = inventoryApi;
            return this;
        }

        public Builder bankingApi(boolean bankingApi) {
            this.bankingApi = bankingApi;
            return this;
        }

        public Builder collectionsApi(boolean collectionsApi) {
            this.collectionsApi = collectionsApi;
            return this;
        }

        public AutoKickProfile build() {
            return new AutoKickProfile(this);
        }
    }
}
