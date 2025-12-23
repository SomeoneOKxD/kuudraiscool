package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;

public class KuudraAutoKickOptions {
    private transient static final String STATS = "Stats Options";
    private transient static final String ITEMS = "Item Options";
    private transient static final String REND = "Rend Options";
    private transient static final String PET = "Pet Options";
    private transient static final String API = "Api Off Options";
    private transient static final String MISC = "Miscellaneous Options";

    // STATS

    @Number(
            name = "Minimum Cata Level",
            description = "Minimum Catacombs level required.",
            min = 0, max = 50,
            subcategory = STATS
    )
    public static int minCataLevel = 0;

    @Number(
            name = "Minimum Foraging Level",
            description = "Minimum Foraging level required.",
            min = 0, max = 54,
            subcategory = STATS
    )
    public static int minForagingLevel = 0;

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

    // ITEMS

    @Slider(
            name = "Minimum Chimera Level On Ragnarock Axe",
            description = "Set the minimum Chimera level for Ragnarock Axe.",
            min = 0, max = 5,
            instant = true,
            subcategory = ITEMS
    )
    public static int minChimeraLevelRagAxe = 4;

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
            options = {"Infernal", "Fiery", "Burning", "Hot", "Basic", "None"},
            size = 2,
            subcategory = ITEMS
    )
    public static int minTerrorTier = 5;

    @Dropdown(
            name = "Minimum Talisman Tier",
            description = "Sets the minimum Talisman tier.",
            options = {"No Talisman", "Kuudra's Kidney", "Kuudra's Lung", "Kuudra's Heart"},
            size = 2,
            subcategory = ITEMS
    )
    public static int minTalismanTier = 0;

    @Switch(
            name = "Kick If No P7 On Duplex Term",
            description = "Kick people who don't have a Power 7 on their duplex terminator.",
            subcategory = ITEMS
    )
    public static boolean autoKickNoP7Duplex = false;

    @Switch(
            name = "Kick If No C6 On Duplex Term",
            description = "Kick people who don't have a Cusbism 6 on their duplex terminator.",
            subcategory = ITEMS
    )
    public static boolean autoKickNoC6Duplex = false;

    @Switch(
            name = "Kick If No Smold 5 On Duplex Term",
            description = "Kick people who don't have a Smoldering 5 on their duplex terminator.",
            subcategory = ITEMS
    )
    public static boolean autoKickNoSmold5Duplex = false;

    @Switch(
            name = "Kick If No Wither Impact",
            description = "Kick people who don't have a Wither Impact weapon.",
            subcategory = ITEMS
    )
    public static boolean autoKickNoWitherImpact = true;

    @Number(
            name = "Minimum Legion Level",
            description = "Set the minimum level of Legion.",
            min = 0, max = 20,
            subcategory = ITEMS
    )
    public static int minLegionLevel = 15;

    @Number(
            name = "Minimum Strong/Fero Mana Level",
            description = "Set the minimum level of Strong/Ferocious Mana.",
            min = 0, max = 20,
            subcategory = ITEMS
    )
    public static int minStrongFeroLevel = 0;

    // REND

    @Switch(
            name = "Kick If Not Rending",
            description = "Kick people who don't have a Rend Bone and a Rend Blade.",
            subcategory = REND
    )
    public static boolean autoKickNoRend = false;

    @Switch(
            name = "Kick If No Rend Term",
            description = "Kick people who don't have a Rend Terminator.",
            subcategory = REND
    )
    public static boolean autoKickNoRendTerm = false;

    @Switch(
            name = "Kick If No P7 On Rend Bone",
            description = "Kick people who don't have a Power 7 on their Rend Bone.",
            subcategory = REND
    )
    public static boolean autoKickNoP7Bone = false;

    @Switch(
            name = "Kick If No C6 On Rend Bone",
            description = "Kick people who don't have a Cusbism 6 on their Rend Bone.",
            subcategory = REND
    )
    public static boolean autoKickNoC6Bone = false;

    @Switch(
            name = "Kick If No Smold 5 On Rend Bone",
            description = "Kick people who don't have a Smoldering 5 on their Rend Bone.",
            subcategory = REND
    )
    public static boolean autoKickNoSmold5Bone = false;

    @Switch(
            name = "Kick If No GK7 On Rend Blade",
            description = "Kick people who don't have a Giant Killer 7 on their Rend Blade.",
            subcategory = REND
    )
    public static boolean autoKickNoGK7Blade = false;

    @Switch(
            name = "Kick If No PROS6 On Rend Blade",
            description = "Kick people who don't have a Prosecute 6 on their Rend Blade.",
            subcategory = REND
    )
    public static boolean autoKickNoPros6Blade = false;

    @Dropdown(
            name = "Minimum Habanero Tactics",
            description = "Sets the minimum Habanero Tactics required on Tux.",
            options = {"No Habanero Tactics", "Habanero Tactics 4", "Habanero Tactics 5"},
            size = 2,
            subcategory = REND
    )
    public static int minHabTactics = 0;

    @Switch(
            name = "Kick If No Bigger Teeth Greg",
            description = "Kick people who don't have a Golden Dragon with Bigger Teeth pet item.",
            subcategory = REND
    )
    public static boolean autoKickNoBiggerTeeth = false;

    // PET

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
            name = "Kick If No Hephaestus Remedies",
            description = "Kick people who don't have a Golden Dragon with Hephaestus Remedies pet item.",
            subcategory = PET
    )
    public static boolean autoKickNoHephaestusRemedies = false;

    @Switch(
            name = "Kick If Inventory API Is Off",
            size = 2,
            description = "Kick people who don't have their Inventory API turned on.",
            subcategory = API
    )
    public static boolean autoKickInventoryApiOff = false;

    @Switch(
            name = "Kick If Banking API Is Off",
            size = 2,
            description = "Kick people who don't have their Banking API turned on.",
            subcategory = API
    )
    public static boolean autoKickBankingApiOff = false;

    @Switch(
            name = "Kick If Collections API Is Off",
            size = 2,
            description = "Kick people who don't have their Collections API turned on.",
            subcategory = API
    )
    public static boolean autoKickCollectionsApiOff = false;

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
