package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;

public class KuudraAutoKickOptions {
    private transient static final String STATS = "Stats Options";
    private transient static final String ITEMS = "Item Options";
    private transient static final String PET = "Pet Options";
    private transient static final String API = "Api Off Options";
    private transient static final String MISC = "Miscellaneous Options";

    @Number(
            name = "Minimum Lifeline/Dominance Level",
            description = "Set the minimum level of Lifeline/Dominance.",
            min = 0, max = 70,
            step = 5,
            subcategory = STATS
    )
    public static int minLifelineLevel = 54;

    @Number(
            name = "Minimum Mana Pool Level",
            description = "Set the minimum level of Mana Pool.",
            min = 0, max = 70,
            step = 5,
            subcategory = STATS
    )
    public static int minManapoolLevel = 40;

    @Number(
            name = "Minimum Cata Level",
            description = "Set the minimum level of Catacombs.",
            min = 0, max = 50,
            step = 5,
            subcategory = STATS
    )
    public static int minCataLevel = 0;

    @Number(
            name = "Minimum T5 Completions",
            description = "Set the minimum T5 Completions.",
            min = 0, max = 25000,
            step = 50,
            subcategory = STATS
    )
    public static int minT5Completions = 900;

    @Number(
            name = "Minimum Magical Power",
            description = "Set the minimum for Magical Power.",
            min = 0, max = 2000,
            step = 50,
            subcategory = STATS
    )
    public static int minMagicalPower = 1350;

    @Slider(
            name = "Minimum Chimera Level On Ragnarock Axe",
            description = "Set the minimum Chimera level for Ragnarock Axe.",
            min = 0, max = 5,
            instant = true,
            subcategory = ITEMS
    )
    public static int minChimeraLevelRagAxe = 2;

    @Slider(
            name = "Minimum Duplex Level",
            description = "Set the minimum level of Duplex.",
            min = 0, max = 5,
            instant = true,
            subcategory = ITEMS
    )
    public static int minDuplexLevel = 0;

    @Dropdown(
            name = "Minimum Gemstone Tier In Ragnarock Axe",
            description = "Sets the minimum gemstone tier in Ragnarock Axe.",
            options = {"Perfect", "Flawless", "Fine", "Flawed", "Rough", "None"},
            size = 2,
            subcategory = ITEMS
    )
    public static int minGemstoneTierRagAxe = 5;

    @Dropdown(
            name = "Minimum Terror Tier",
            description = "Sets the minimum Terror Armor tier.",
            options = {"Infernal", "Fiery", "Burning", "Hot", "Basic"},
            size = 2,
            subcategory = ITEMS
    )
    public static int minTerrorTier = 0;

    @Switch(
            name = "Auto Kick If No P7 On Duplex Term",
            description = "Auto Kick people who don't have a Power 7 on their duplex terminator.",
            subcategory = ITEMS
    )
    public static boolean autoKickNoP7Duplex = false;

    @Switch(
            name = "Auto Kick If No C6 On Duplex Term",
            description = "Auto Kick people who don't have a Cusbism 6 on their duplex terminator.",
            subcategory = ITEMS
    )
    public static boolean autoKickNoC7Duplex = false;

    @Switch(
            name = "Auto Kick If No Wither Impact",
            size = 2,
            description = "Auto Kick people who don't have a Wither Impact weapon.",
            subcategory = ITEMS
    )
    public static boolean autoKickNoWitherImpact = true;

    @Number(
            name = "Minimum Legion Level",
            description = "Set the minimum level of Legion.",
            min = 0, max = 20,
            subcategory = ITEMS
    )
    public static int minLegionLevel = 20;

    @Number(
            name = "Minimum Strong/Fero Mana Level",
            description = "Set the minimum level of Strong/Ferocious Mana.",
            min = 0, max = 20,
            subcategory = ITEMS
    )
    public static int minStrongFeroLevel = 0;

    @Slider(
            name = "Minimum Bank Balance (Per 100M)",
            description = "Set the minimum Bank Balance. (Per 100M, 1 = 100M, 10 = 1B)",
            min = 0, max = 10,
            instant = true,
            subcategory = PET
    )
    public static int minBankBalance = 10;

    @Slider(
            name = "Minimum Gold Collection (10M)",
            description = "Set the minimum Gold Collection. (Per 10M, 1 = 10M, 10 = 100M)",
            min = 0, max = 10,
            instant = true,
            subcategory = PET
    )
    public static int minGoldCollection = 0;

    @Number(
            name = "Minimum Golden Dragon Level",
            description = "Set the minimum level for Golden Dragon.",
            min = 0, max = 200, step = 5,
            subcategory = PET
    )
    public static int minGregLevel = 200;

    @Switch(
            name = "Auto Kick If Inventory API Is Off",
            size = 2,
            description = "Auto Kick people who don't have their Inventory API turned on.",
            subcategory = API
    )
    public static boolean autoKickInventoryApiOff = false;

    @Switch(
            name = "Auto Kick If Banking API Is Off",
            size = 2,
            description = "Auto Kick people who don't have their Banking API turned on.",
            subcategory = API
    )
    public static boolean autoKickBankingApiOff = false;

    @Switch(
            name = "Auto Kick If Collections API Is Off",
            size = 2,
            description = "Auto Kick people who don't have their Collections API turned on.",
            subcategory = API
    )
    public static boolean autoKickCollectionsApiOff = false;

    @Switch(
            name = "Auto Kick Trimonu Users",
            size = 2,
            description = "Kick people who use Trimonu.",
            subcategory = MISC
    )
    public static boolean autoKickTrimonu = true;

    @Switch(
            name = "Use Whitelist",
            size = 2,
            description = "Don't kick the whitelisted players",
            subcategory = MISC
    )
    public static boolean useWhitelist = false;

    @Text(
            name = "Whitelisted IGNs and UUIDs (Separate players with ;)",
            multiline = true,
            subcategory = MISC,
            size = 2,
            description = "Example: 9593fa76cdca4d9aa6d2dbb92e33294a;Xaned;sophlie"
    )
    public static String whitelisted = "";

    @Switch(
            name = "Use blacklist",
            size = 2,
            description = "Always kick the blacklisted players",
            subcategory = MISC
    )
    public static boolean useBlacklist = false;

    @Text(
            name = "Blacklisted IGNs and UUIDs (Separate players with ;)",
            multiline = true,
            subcategory = MISC,
            size = 2,
            description = "Example: 9593fa76cdca4d9aa6d2dbb92e33294a;JustJex;a0137b1af91d4bde963cc0a0b3f0df44"
    )
    public static String blacklisted = "";
}
