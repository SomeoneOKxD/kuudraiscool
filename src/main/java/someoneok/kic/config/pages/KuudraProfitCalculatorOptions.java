package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Page;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.PageLocation;

public class KuudraProfitCalculatorOptions {
    private transient static final String PROFIT = "Profit";
    private transient static final String PET = "Kuudra Pet Crimson Perk Options";
    private transient static final String PRICE = "Item Price Options";
    private transient static final String MISC = "Miscellaneous";

    @Switch(
            name = "Ignore Essence Value",
            description = "Exclude the value of essence from profit calculations.",
            subcategory = PROFIT
    )
    public static boolean ignoreEssence = false;

    @Switch(
            name = "Ignore Teeth Value",
            description = "Exclude the value of teeth from profit calculations.",
            subcategory = PROFIT
    )
    public static boolean ignoreTeeth = false;

    @Switch(
            name = "Force Salvage Value",
            description = "Always use salvage value for Crimson, Aurora, Terror, Hollow and Fervor pieces.",
            subcategory = PROFIT
    )
    public static boolean forceSalvageValue = false;

    @Switch(
            name = "Reroll notifier",
            subcategory = PROFIT
    )
    public static boolean rerollNotifier = true;

    @Switch(
            name = "Include Extra Crimson Essence",
            description = "Include the extra Crimson Essence from the Kuudra Pet perk in profit calculations.",
            size = 2,
            subcategory = PET
    )
    public static boolean includeKuudraPetPerk = true;

    @Dropdown(
            name = "Kuudra Pet Tier",
            options = {"No Pet", "Common", "Uncommon", "Rare", "Epic", "Legendary"},
            subcategory = PET
    )
    public static int kuudraPetRarity = 5;

    @Number(
            name = "Kuudra Pet Level",
            min = 0, max = 100,
            subcategory = PET
    )
    public static int kuudraPetLevel = 100;

    @Dropdown(
            name = "Bazaar Items Price Type",
            description = "Calculate profit based on the Sell Offer price or the Instant Sell price.",
            options = {"Instant Sell Price", "Sell Offer Price"},
            subcategory = PRICE
    )
    public static int bazaarPriceType = 0;

    @Dropdown(
            name = "Auction Items Price Type",
            description = "Calculate auction items based on the Lowest Bin price or the Average Price.",
            options = {"Lowest Bin", "Average Price"},
            subcategory = PRICE
    )
    public static int auctionPriceType = 1;

    @Dropdown(
            name = "Key Price Type",
            description = "Calculate keys based on the Sell Offer price or the Instant Sell price.",
            options = {"Instant Sell Price", "Sell Offer Price"},
            subcategory = PRICE
    )
    public static int keyPriceType = 1;

    @Dropdown(
            name = "Kismet Price Type",
            description = "Calculate kismet's used for rerolling based on the Sell Offer price or the Instant Sell price.",
            options = {"Instant Sell Price", "Sell Offer Price"},
            subcategory = PRICE
    )
    public static int kismetPriceType = 0;

    @Switch(
            name = "Ignore Aurora Staffs (Value = 0)",
            subcategory = MISC
    )
    public static boolean ignoreAuroraStaff = true;

    @Switch(
            name = "Ignore Hollow Wands (Value = 0)",
            subcategory = MISC
    )
    public static boolean ignoreHollowWand = true;

    @Page(
            name = "Shard Reroll Failsafes",
            location = PageLocation.BOTTOM,
            subcategory = MISC
    )
    public KuudraRerollFailsafeOptions kuudraRerollFailsafeOptions = new KuudraRerollFailsafeOptions();
}
