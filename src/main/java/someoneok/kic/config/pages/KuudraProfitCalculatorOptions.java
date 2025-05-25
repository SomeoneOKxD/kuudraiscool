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
            description = "Calculate profit based on the Sell Order price or the Instant Sell price.",
            options = {"Instant Sell Price", "Sell Order Price"},
            subcategory = PRICE
    )
    public static int bazaarPriceType = 0;

    @Dropdown(
            name = "Attribute Items Price Type",
            description = "Calculate attribute items based on the Lowest Bin price or the Average Price.",
            options = {"Lowest Bin", "Average Price"},
            subcategory = PRICE
    )
    public static int attributePriceType = 1;

    @Dropdown(
            name = "God Roll Items Price Type",
            description = "Calculate god roll items based on the Lowest Bin price or the Average Price.",
            options = {"Lowest Bin", "Average Price"},
            subcategory = PRICE
    )
    public static int godRollPriceType = 1;

    @Dropdown(
            name = "Miscellaneous Items Price Type",
            description = "Calculate auction items based on the Lowest Bin price or the Average Price.",
            options = {"Lowest Bin", "Average Price"},
            subcategory = PRICE
    )
    public static int miscellaneousPriceType = 1;

    @Dropdown(
            name = "Key Price Type",
            description = "Calculate keys based on the Sell Order price or the Instant Sell price.",
            options = {"Instant Sell Price", "Sell Order Price"},
            subcategory = PRICE
    )
    public static int keyPriceType = 1;

    @Dropdown(
            name = "Kismet Price Type",
            description = "Calculate keys based on the Sell Order price or the Instant Sell price.",
            options = {"Instant Sell Price", "Sell Order Price"},
            subcategory = PRICE
    )
    public static int kismetPriceType = 0;

    @Switch(
            name = "Use T5 attribute prices when an item has T6+ attributes",
            subcategory = MISC
    )
    public static boolean forceT5Attribute = true;

    @Switch(
            name = "Only on Lowest Bin",
            description = "Only use T5 attribute prices when an item has T6+ attributes when on Lowest Bin type.",
            subcategory = MISC
    )
    public static boolean forceT5AttributeOnlyLB = true;

    @Switch(
            name = "Ignore Hollow Wands (Value = 0)",
            subcategory = MISC
    )
    public static boolean ignoreHollowWands = true;

    @Switch(
            name = "Ignore Aurora Staffs (Value = 0)",
            subcategory = MISC
    )
    public static boolean ignoreAuroraStaff = true;

    @Page(
            name = "Reroll Failsafes",
            location = PageLocation.BOTTOM,
            subcategory = MISC
    )
    public KuudraRerollFailsafeOptions kuudraRerollFailsafeOptions = new KuudraRerollFailsafeOptions();

    @Page(
            name = "Armor Salvage Value Attributes",
            location = PageLocation.BOTTOM,
            subcategory = MISC
    )
    public KuudraSalvageAttributeOptions kuudraSalvageAttributeOptions = new KuudraSalvageAttributeOptions();

    @Page(
            name = "Ignored Equipment Attributes",
            location = PageLocation.BOTTOM,
            subcategory = MISC
    )
    public KuudraIgnoredEquipmentAttributeOptions kuudraIgnoredEquipmentAttributeOptions = new KuudraIgnoredEquipmentAttributeOptions();

    @Page(
            name = "Ignored Shard Attributes",
            location = PageLocation.BOTTOM,
            subcategory = MISC
    )
    public KuudraIgnoredShardAttributeOptions kuudraIgnoredShardAttributeOptions = new KuudraIgnoredShardAttributeOptions();
}
