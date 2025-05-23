package someoneok.kic.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import someoneok.kic.config.pages.KuudraAutoKickOptions;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.config.pages.KuudraProfitTrackerOptions;
import someoneok.kic.config.pages.KuudraSplitsOptions;
import someoneok.kic.modules.kuudra.TrajectorySolver;
import someoneok.kic.modules.kuudra.Waypoints;
import someoneok.kic.modules.misc.ButtonManager;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.EditHudScreen;

import java.util.Objects;

import static someoneok.kic.utils.ApiUtils.*;
import static someoneok.kic.utils.GeneralUtils.round2;
import static someoneok.kic.utils.StringUtils.isValidUUIDv4RegexBased;

public class KICConfig extends Config {
    private static String oldKey = "";
    private static int oldAPInitialDelay = 0;
    private static int oldAPDoubleDelay = 0;
    private static int oldAPSkyDistance = 0;
    private static int oldAPFlatDistance = 0;
    private static float oldAPTextSizeScaleSky = 0;
    private static float oldAPWaypointSizeScaleSky = 0;
    private static float oldAPTextSizeScaleFlat = 0;
    private static float oldAPWaypointSizeScaleFlat = 0;

    public KICConfig() {
        super(new Mod("Kuudraiscool", ModType.SKYBLOCK), "kic.json");
        initialize();
        oldKey = apiKey;
        // Player Size
        addDependency("playerSize", "playerSizeToggle");
        addDependency("playerSizeIgnoreNPC", "playerSizeToggle");

        // Websocket
        addDependency("kicChat", "verifiedApiKeyChecker", ApiUtils::isVerified);
        addDependency("notifyAhUpdate", "verifiedApiKeyChecker", ApiUtils::isVerified);
        addDependency("dataUpdateNotificationMethod", "notifyAhUpdate");

        // Key info
        hideIf("keyErrorError", () -> !ApiUtils.isApiKeyError());
        hideIf("keyActiveSuccess", () -> ApiUtils.isApiKeyError() && !ApiUtils.isVerified());
        hideIf("keyVerifiedWarning", ApiUtils::isVerified);
        addListener("apiKey", () -> {
            if (Objects.equals(apiKey, oldKey)) return;
            if (!isValidUUIDv4RegexBased(apiKey)) return;
            KICLogger.info("Api key verification triggered from listener");
            oldKey = apiKey;
            Multithreading.runAsync(() -> verifyApiKey(true));
        });

        // Tester & Admin
        hideIf("testerMode", () -> !isTester() && !isDev() && !isBeta());
        hideIf("testerModeLogInChat", () -> !isTester() && !isDev() && !isBeta());
        hideIf("KICAdminGuiStyle", () -> !isAdmin());
        hideIf("KICAdminGuiColor", () -> !isAdmin());
        hideIf("autoPearls", () -> !isAdmin());
        hideIf("devKey", () -> !isDev());

        // Premium
        hideIf("partyFinderGuiStats", () -> !hasPremium());
        hideIf("kicPlusChat", () -> !hasPremium());

        // Kuudra
        oldAPInitialDelay = APInitialDelay;
        oldAPDoubleDelay = APDoubleDelay;
        oldAPSkyDistance = APSkyDistance;
        oldAPFlatDistance = APFlatDistance;
        oldAPTextSizeScaleSky = APTextSizeScaleSky;
        oldAPWaypointSizeScaleSky = APWaypointSizeScaleSky;
        oldAPTextSizeScaleFlat = APTextSizeScaleFlat;
        oldAPWaypointSizeScaleFlat = APWaypointSizeScaleFlat;

        addDependency("kuudraProfitTracker", "kuudraProfitCalculator");
        addDependency("showGodRollHolo", "kuudraProfitCalculator");
        addDependency("showGodRollHoloBothSides", "kuudraProfitCalculator");
        addDependency("showGodRollHoloBothSides", "showGodRollHolo");
        addDependency("kuudraAutoKick", "partyFinder");

        addDependency("showDoublePearls", "pearlCalculator");
        addDependency("showDoublePearls", "pearlCalculator");

        hideIf("advancedPearlSettings", () -> !KICConfig.pearlCalculator);
        hideIf("advancedPearlSettingsInfo", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APInitialDelay", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APDoubleDelay", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APSkyDistance", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APFlatDistance", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APTextSizeScaleSky", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APWaypointSizeScaleSky", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APTextSizeScaleFlat", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APWaypointSizeScaleFlat", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APTimerPos", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);

        hideIf("APUseCustomShape", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings);
        hideIf("APCustom", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings || !KICConfig.APUseCustomShape);
        hideIf("APCustomShapeQuality", () -> !KICConfig.pearlCalculator || !KICConfig.advancedPearlSettings || !KICConfig.APUseCustomShape);

        addDependency("advancedPearlSettingsInfo", "advancedPearlSettings");
        addDependency("APInitialDelay", "advancedPearlSettings");
        addDependency("APDoubleDelay", "advancedPearlSettings");
        addDependency("APSkyDistance", "advancedPearlSettings");
        addDependency("APFlatDistance", "advancedPearlSettings");
        addDependency("APTextSizeScaleSky", "advancedPearlSettings");
        addDependency("APWaypointSizeScaleSky", "advancedPearlSettings");
        addDependency("APTextSizeScaleFlat", "advancedPearlSettings");
        addDependency("APWaypointSizeScaleFlat", "advancedPearlSettings");
        addDependency("APTimerPos", "advancedPearlSettings");

        addDependency("APUseCustomShape", "advancedPearlSettings");
        addDependency("APCustomShapeQuality", "advancedPearlSettings");
        addDependency("APCustom", "advancedPearlSettings");
        addDependency("APCustomShapeQuality", "advancedPearlSettings");

        addDependency("APCustomShapeQuality", "APUseCustomShape");
        addDependency("APCustom", "APUseCustomShape");
        addDependency("APCustomShapeQuality", "APUseCustomShape");

        addListener("advancedPearlSettings", () -> {
            Waypoints.updateSizes();
            Waypoints.updatePearlDelays();
        });
        addListener("APInitialDelay", () -> {
            if (Objects.equals(APInitialDelay, oldAPInitialDelay)) return;
            oldAPInitialDelay = APInitialDelay;
            Waypoints.updatePearlDelays();
        });
        addListener("APDoubleDelay", () -> {
            if (Objects.equals(APDoubleDelay, oldAPDoubleDelay)) return;
            oldAPDoubleDelay = APDoubleDelay;
            Waypoints.updatePearlDelays();
        });
        addListener("APSkyDistance", () -> {
            if (Objects.equals(APSkyDistance, oldAPSkyDistance)) return;
            oldAPSkyDistance = APSkyDistance;
            TrajectorySolver.updateDistances();
        });
        addListener("APFlatDistance", () -> {
            if (Objects.equals(APFlatDistance, oldAPFlatDistance)) return;
            oldAPFlatDistance = APFlatDistance;
            TrajectorySolver.updateDistances();
        });
        addListener("APTextSizeScaleSky", () -> {
            if (Objects.equals(round2(APTextSizeScaleSky), oldAPTextSizeScaleSky)) return;
            oldAPTextSizeScaleSky = round2(APTextSizeScaleSky);
            Waypoints.updateSizes();
        });
        addListener("APWaypointSizeScaleSky", () -> {
            if (Objects.equals(round2(APWaypointSizeScaleSky), oldAPWaypointSizeScaleSky)) return;
            oldAPWaypointSizeScaleSky = round2(APWaypointSizeScaleSky);
            Waypoints.updateSizes();
        });
        addListener("APTextSizeScaleFlat", () -> {
            if (Objects.equals(round2(APTextSizeScaleFlat), oldAPTextSizeScaleFlat)) return;
            oldAPTextSizeScaleFlat = round2(APTextSizeScaleFlat);
            Waypoints.updateSizes();
        });
        addListener("APWaypointSizeScaleFlat", () -> {
            if (Objects.equals(round2(APWaypointSizeScaleFlat), oldAPWaypointSizeScaleFlat)) return;
            oldAPWaypointSizeScaleFlat = round2(APWaypointSizeScaleFlat);
            Waypoints.updateSizes();
        });

        addListener("APTimerPos", Waypoints::updateTimerLocation);

        addListener("invButtonLoc", ButtonManager::updateCheckboxAlignment);

        addDependency("showSpotNames", "showSpots");
        addDependency("hideSpotIfClose", "showSpots");

        addDependency("ACAutoBuy", "kuudraProfitCalculator");
        addDependency("ACAlwaysAutoBuy", "kuudraProfitCalculator");
        addDependency("ACAlwaysAutoBuy", "ACAutoBuy");
        addDependency("ACAutoBuyMinProfit", "kuudraProfitCalculator");
        addDependency("ACAutoBuyMinProfit", "ACAutoBuy");
        addDependency("ACAutoCloseGui", "kuudraProfitCalculator");
        addDependency("ACAutoCloseGui", "ACAutoBuy");
        addDependency("ACAutoReroll", "kuudraProfitCalculator");
        addDependency("ACOnlyRerollInT5", "kuudraProfitCalculator");
        addDependency("ACOnlyRerollInT5", "ACAutoReroll");
        addDependency("ACShouldRerollType", "kuudraProfitCalculator");
        addDependency("ACShouldRerollType", "ACAutoReroll");
        addDependency("ACAutoRerollMinValue", "kuudraProfitCalculator");
        addDependency("ACAutoRerollMinValue", "ACAutoReroll");

        addDependency("kuudraProfitTrackerAddRunTimeDelay", "kuudraProfitTracker");
        addDependency("kuudraProfitTrackerTotalTimeToAdd", "kuudraProfitTracker");
        addDependency("kuudraProfitTrackerTotalTimeToAdd", "kuudraProfitTrackerAddRunTimeDelay");
        hideIf("kuudraProfitTrackerAddRunTimeDelay", () -> !KICConfig.kuudraProfitTracker);
        hideIf("kuudraProfitTrackerTotalTimeToAdd", () -> !KICConfig.kuudraProfitTracker || !KICConfig.kuudraProfitTrackerAddRunTimeDelay);

        addDependency("kuudraNotificationsMoveable", "kuudraNotifications");
        addDependency("kuudraNotiNoEquals", "kuudraNotifications");
        addDependency("kuudraNotiNoShop", "kuudraNotifications");
        addDependency("kuudraNotiNoXCannon", "kuudraNotifications");
        addDependency("kuudraNotiNoSlash", "kuudraNotifications");
        addDependency("kuudraNotiNoSquare", "kuudraNotifications");
        addDependency("kuudraNotiNoTriangle", "kuudraNotifications");
        addDependency("kuudraNotiNoX", "kuudraNotifications");
        addDependency("kuudraNotiDropped", "kuudraNotifications");
        addDependency("kuudraNotiCooldown", "kuudraNotifications");
        addDependency("kuudraNotiGrabbing", "kuudraNotifications");
        addDependency("kuudraNotiGrabbed", "kuudraNotifications");
        addDependency("kuudraNotiPlaced", "kuudraNotifications");

        addDependency("kuudraNotiNoSupplyTime", "kuudraNotifications");
        addDependency("kuudraNotiDroppedTime", "kuudraNotifications");
        addDependency("kuudraNotiCooldownTime", "kuudraNotifications");
        addDependency("kuudraNotiGrabbingTime", "kuudraNotifications");
        addDependency("kuudraNotiGrabbedTime", "kuudraNotifications");
        addDependency("kuudraNotiPlacedTime", "kuudraNotifications");

        // Crimson
        addDependency("kaDefaultAttributeLvl", "kaUseDefaultAttributeLvl");

        // Chat
        addDependency("partyCommandRuns", "partyCommands");
        addDependency("partyCommandStats", "partyCommands");
        addDependency("partyCommandAp", "partyCommands");
        addDependency("partyCommandKick", "partyCommands");
        addDependency("partyCommandCata", "partyCommands");
        addDependency("partyCommandRtca", "partyCommands");
        addDependency("dmCommandRuns", "dmCommands");
        addDependency("dmCommandStats", "dmCommands");
        addDependency("dmCommandAp", "dmCommands");
        addDependency("dmCommandCata", "dmCommands");
        addDependency("dmCommandRtca", "dmCommands");

        // World
        addDependency("serverAlertTime", "serverAlert");
    }

    // Categories
    private transient static final String KUUDRA = "Kuudra";
    private transient static final String CRIMSON = "Crimson";
    private transient static final String PLAYER = "Player";
    private transient static final String CHAT = "Chat";
    private transient static final String WS = "Websocket";
    private transient static final String MISC = "Miscellaneous";
    private transient static final String HUD = "Huds";

    // Modules

    // GENERAL
    private transient static final String PRESETS = "Presets";
    private transient static final String TESTER = "Tester";
    private transient static final String ADMIN = "Admin";

    // KUUDRA
    private transient static final String KUUDRA_SUB = "Kuudra";
    private transient static final String KUUDRA_BOSS = "Kuudra Boss";
    private transient static final String KUUDRA_WAYPOINTS = "Kuudra Waypoints";
    private transient static final String KUUDRA_PRE = "Kuudra Pre";
    private transient static final String KUUDRA_MISC = "Kuudra Miscellaneous";
    private transient static final String KUUDRA_PROFIT_CALC = "Kuudra Profit Calculator";
    private transient static final String KUUDRA_AUTO_CHEST = "Kuudra Auto Chest";
    private transient static final String KUUDRA_PROFIT_TRACKER = "Kuudra Profit Tracker";
    private transient static final String KUUDRA_AUTO_KICK = "Auto Kick";
    private transient static final String KUUDRA_SPLITS = "Kuudra Splits";
    private transient static final String KUUDRA_NOTIFICATIONS = "Kuudra Notifications";

    // CRIMSON
    private transient static final String CRIMSON_GENERAL = "General";
    private transient static final String CRIMSON_AH_HELPER = "Auction Helper";
    private transient static final String CRIMSON_CONTAINER = "Container";
    private transient static final String CRIMSON_TOOLTIP = "Tooltip";
    private transient static final String CRIMSON_AH = "KIC Auction";

    // PLAYER
    private transient static final String PLAYER_SIZING = "Player Sizing";

    // CHAT
    private transient static final String PARTY_COMMANDS = "Party Commands";
    private transient static final String DM_COMMANDS = "DM Commands";

    // WEBSOCKET
    private transient static final String KIC_CHAT = "KIC Chat";
    private transient static final String DATA_UPDATES = "Data Updates";

    // Config options

    // GENERAL

    @Info(
            text = "Your API key needs to be verified to be able to use this!",
            type = InfoType.ERROR,
            size = 2
    )
    public static boolean keyErrorError;

    @Info(
            text = "Your API key has been verified and is active!",
            type = InfoType.SUCCESS,
            size = 2
    )
    public static boolean keyActiveSuccess;

    @Text(
            name = "KIC API Key",
            description = "Enter your kuudraiscool API key here.",
            secure = true
    )
    public static String apiKey = "";

    @Button(
            name = "Verify kuudraiscool API key",
            text = "Click me!",
            size = 2
    )
    Runnable verifyKey = () -> {
        if (ApiUtils.isVerified() && Objects.equals(apiKey, oldKey)) return;
        if (!isValidUUIDv4RegexBased(apiKey)) return;
        KICLogger.info("Api key verification triggered from button");
        oldKey = apiKey;
        Multithreading.runAsync(() -> verifyApiKey(true));
    };

    @Info(
            text = "Clicking the preset button will immediately reset your entire config to the selected preset. There is no confirmation!",
            type = InfoType.WARNING,
            subcategory = PRESETS,
            size = 2
    )
    public static boolean presetWarning;

    @Button(
            name = "Default preset",
            text = "Click me!",
            subcategory = PRESETS
    )
    Runnable useDefault = ConfigPresets::defaultPreset;

    @Switch(
            name = "Tester Mode",
            description = "Toggle tester mode",
            subcategory = TESTER
    )
    public static boolean testerMode = false;

    @Switch(
            name = "Tester Mode - Log In Chat",
            description = "Toggle logging in chat",
            subcategory = TESTER
    )
    public static boolean testerModeLogInChat = false;

    @Dropdown(
            name = "KIC Admin GUI Style",
            options = {
                    "Default", "Prism", "PackHQ"
            },
            subcategory = ADMIN
    )
    public static int KICAdminGuiStyle = 1;

    @Dropdown(
            name = "KIC Admin GUI Color",
            subcategory = ADMIN,
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow"
            }
    )
    public static int KICAdminGuiColor = 12;

    @KeyBind(
            name = "Dev Key",
            size = 2,
            subcategory = ADMIN
    )
    public static OneKeyBind devKey = new OneKeyBind(UKeyboard.KEY_EQUALS);

    // KUUDRA

    @Switch(
            name = "Party Finder",
            category = KUUDRA,
            subcategory = KUUDRA_SUB,
            description = "Toggle Party Finder stats."
    )
    public static boolean partyFinder = true;

    @Switch(
            name = "Party Finder Gui Stats (KIC+)",
            category = KUUDRA,
            subcategory = KUUDRA_SUB
    )
    public static boolean partyFinderGuiStats = false;

    @Dropdown(
            name = "Kuudra Player Stats Style",
            options = {
                    "KIC", "Attribute Mod"
            },
            category = KUUDRA,
            subcategory = KUUDRA_SUB,
            size = 2
    )
    public static int kuudraStatsStyle = 0;

    @Switch(
            name = "Total Time Server Lagged For",
            category = KUUDRA,
            subcategory = KUUDRA_SUB,
            description = "At the end of the run show how long the server lagged for."
    )
    public static boolean showTotalServerLag = true;

    @Switch(
            name = "Dynamic Pearl Calculator",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            description = "Shows you where to throw your pearl for from any position to land on the supply spot."
    )
    public static boolean pearlCalculator = true;

    @Switch(
            name = "Show Sky Pearl",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showSkyPearls = true;

    @Switch(
            name = "Show Flat Pearl",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showFlatPearls = true;

    @Switch(
            name = "Show Double Pearl",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showDoublePearls = true;

    @Switch(
            name = "Show Supply Spots Without Any Supplies",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showNothingSupplyWaypoints = false;

    @Switch(
            name = "Show Beacon At Supply Spots Without Any Supplies",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showNothingSupplyWaypointsBeacon = false;

    @Color(
            name = "Supply Spot Color",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static OneColor supplySpotColor = new OneColor(0,0,100);

    @Switch(
            name = "Show All Supply Waypoints",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showAll = false;

    @Switch(
            name = "Auto Pearls",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean autoPearls = false;

    @Info(
            text = "Enabling advanced pearl settings may negatively impact performance. Use with caution!",
            type = InfoType.WARNING,
            size = 2,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean advancedPearlSettingsWarning;

    @Switch(
            name = "Use Advanced Settings",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean advancedPearlSettings = false;

    @Info(
            text = "Advanced Pearl Settings",
            type = InfoType.INFO,
            size = 2,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean advancedPearlSettingsInfo;

    @Number(
            name = "Initial Pearl Delay (In ms) (Sky, Flat, Double)",
            min = 0, max = 1000,
            step = 50,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APInitialDelay = 0;

    @Number(
            name = "Added Double Pearl Delay (In ms)",
            min = 0, max = 1000,
            step = 50,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APDoubleDelay = 500;

    @Number(
            name = "Waypoint Offset Distance (In blocks) (Sky, Double)",
            min = 1, max = 100,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APSkyDistance = 20;

    @Number(
            name = "Waypoint Offset Distance (In blocks) (Flat)",
            min = 1, max = 100,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APFlatDistance = 15;

    @Slider(
            name = "Pearl Timer Size Scale (Sky, Double)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APTextSizeScaleSky = 0.75f;

    @Slider(
            name = "Pearl Waypoint Size Scale (Sky, Double)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APWaypointSizeScaleSky = 0.50f;

    @Slider(
            name = "Pearl Timer Size Scale (Flat)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APTextSizeScaleFlat = 0.50f;

    @Slider(
            name = "Pearl Waypoint Size Scale (Flat)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APWaypointSizeScaleFlat = 0.50f;

    @Dropdown(
            name = "Timer Position",
            options = {"Above", "Under"},
            size = 2,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static int APTimerPos = 1;

    @Switch(
            name = "Use Custom Shape",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean APUseCustomShape = false;

    @Dropdown(
            name = "Custom Shape",
            options = {"Circle", "Heart"},
            size = 2,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static int APCustom = 0;

    @Slider(
            name = "Custom Shape Quality (Higher = more lag)",
            min = 3, max = 64,
            instant = true,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static int APCustomShapeQuality = 16;

    @Switch(
            name = "Show Pre Spots",
            category = KUUDRA,
            subcategory = KUUDRA_PRE
    )
    public static boolean showSpots = false;

    @Switch(
            name = "Show Pre Spot Names",
            category = KUUDRA,
            subcategory = KUUDRA_PRE
    )
    public static boolean showSpotNames = false;

    @Switch(
            name = "Hide Pre Spot If close",
            category = KUUDRA,
            subcategory = KUUDRA_PRE
    )
    public static boolean hideSpotIfClose = true;

    @Switch(
            name = "Kuudra Boss Bar",
            category = KUUDRA,
            subcategory = KUUDRA_BOSS
    )
    public static boolean showKuudraBossBar = true;

    @Switch(
            name = "Show Kuudra Outline",
            category = KUUDRA,
            subcategory = KUUDRA_BOSS
    )
    public static boolean showKuudraOutline = true;

    @Switch(
            name = "Show Kuudra Health",
            category = KUUDRA,
            subcategory = KUUDRA_BOSS
    )
    public static boolean showKuudraHealth = true;

    @Switch(
            name = "Elle ESP",
            category = KUUDRA,
            subcategory = KUUDRA_MISC
    )
    public static boolean elleESP = true;

    @Switch(
            name = "Show Kuudra Supply Waypoints",
            category = KUUDRA,
            subcategory = KUUDRA_MISC
    )
    public static boolean supplyWaypoints = true;

    @Switch(
            name = "Show Kuudra Supply Box",
            category = KUUDRA,
            subcategory = KUUDRA_MISC
    )
    public static boolean supplyBox = false;

    @Color(
            name = "Supply Color",
            category = KUUDRA,
            subcategory = KUUDRA_MISC
    )
    public static OneColor supplyColor = new OneColor(0, 255, 180);

    @Switch(
            name = "Supply Pile Build Progress Beacon",
            category = KUUDRA,
            subcategory = KUUDRA_MISC
    )
    public static boolean supplyWaypointsProgress = true;

    @Switch(
            name = "Auto Refill Pearls In Hotbar",
            category = KUUDRA,
            subcategory = KUUDRA_MISC
    )
    public static boolean autoRefillPearls = false;

    @Number(
            name = "Pearl Refill Timer In Ticks",
            min = 10, max = 600,
            step = 10,
            category = KUUDRA,
            subcategory = KUUDRA_MISC,
            size = 2
    )
    public static int autoRefillPearlsTicks = 200;

    @Switch(
            name = "Profit Calculator",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC,
            size = 2
    )
    public static boolean kuudraProfitCalculator = true;

    @Switch(
            name = "Show God Roll Hologram",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC
    )
    public static boolean showGodRollHolo = true;

    @Switch(
            name = "Show God Roll Hologram From Both Sides",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC,
            size = 2,
            description = "Only turn this off if the god roll hologram is displayed twice otherwise leave it on."
    )
    public static boolean showGodRollHoloBothSides = true;

    @Page(
            name = "Profit Calculator Options",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC,
            location = PageLocation.BOTTOM
    )
    public KuudraProfitCalculatorOptions kuudraProfitCalculatorOptions = new KuudraProfitCalculatorOptions();

    @Info(
            text = "Auto Buy and Auto Reroll are \"USE AT YOUR OWN RISK!\" settings!",
            type = InfoType.WARNING,
            size = 2,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static boolean UseAtYourOwnRisk1;

    @Info(
            text = "Auto Buy Options",
            type = InfoType.INFO,
            size = 2,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static boolean isIgnored1;

    @Switch(
            name = "Auto Buy",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static boolean ACAutoBuy = false;

    @Switch(
            name = "Always Buy",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST,
            description = "Always buy the chest regardless of profit, waits on profit calculator."
    )
    public static boolean ACAlwaysAutoBuy = true;

    @Switch(
            name = "Insta Buy",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST,
            description = "Instantly open the chest when the GUI opens, ignoring profit calculator.",
            size = 2
    )
    public static boolean ACInstaBuy = false;

    @Number(
            name = "Minimun Total Profit For Auto Buy",
            description = "Set the minimum total profit required to auto buy the chest. (Set to 0 to turn off)",
            min = 0, max = 100_000_000,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static int ACAutoBuyMinProfit = 0;

    @Switch(
            name = "Auto Close Chest After Buy",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static boolean ACAutoCloseGui = true;

    @Number(
            name = "Auto Close Chest Delay",
            min = 100, max = 1000,
            step = 50,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST,
            size = 2
    )
    public static int ACAutoCloseDelay = 250;

    @Number(
            name = "Delay Before Insta Buy After Opening GUI",
            min = 100, max = 1000,
            step = 50,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST,
            size = 2
    )
    public static int  ACInitialInstaBuyDelay = 300;

    @Info(
            text = "Auto Reroll Options",
            type = InfoType.INFO,
            size = 2,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static boolean isIgnored2;

    @Switch(
            name = "Auto Reroll",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static boolean ACAutoReroll = false;

    @Switch(
            name = "Only Reroll In T5",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static boolean ACOnlyRerollInT5 = true;

    @Dropdown(
            name = "Should Reroll Type Check",
            description = "Choose what type of check to use for auto reroll",
            options = {"Kismet Price > Value Of First 2 Slots", "Custom Amount"},
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static int ACShouldRerollType = 1;

    @Number(
            name = "Minimun Total Value Of First 2 Slots",
            description = "Specifies the minimum combined value of the first 2 slots required to avoid a reroll. If their total value is below this threshold, the chest will be rerolled.",
            min = 1, max = 10_000_000,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static int ACAutoRerollMinValue = 2_000_000;

    @Switch(
            name = "Profit Tracker (All Tiers)",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_TRACKER,
            size = 2
    )
    public static boolean kuudraProfitTracker = true;

    @Switch(
            name = "Include Delay In Run Time",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_TRACKER,
            size = 2
    )
    public static boolean kuudraProfitTrackerAddRunTimeDelay = true;
    @Switch(
            name = "Profit Tracker (Infernal Only)",
            description = "Only shows the profit tracker in T5 Infernal Kuudra",
            category = "Kuudra"
    )
    public static boolean kuudraProfitTrackerInfernal = false;

    @Number(
            name = "Delay To Add (s)",
            min = 0, max = 60,
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_TRACKER
    )
    public static int kuudraProfitTrackerTotalTimeToAdd = 15;

    @Page(
            name = "Profit Tracker Options",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_TRACKER,
            location = PageLocation.BOTTOM
    )
    public KuudraProfitTrackerOptions kuudraProfitTrackerOptions = new KuudraProfitTrackerOptions();

    @Info(
            text = "Settings under " + KUUDRA_AUTO_KICK + " are \"USE AT YOUR OWN RISK!\" settings!",
            type = InfoType.WARNING,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_KICK,
            size = 2
    )
    public static boolean UseAtYourOwnRisk0;

    @Switch(
            name = "Auto Kick",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_KICK,
            size = 2,
            description = "Automatically kicks people who do not meet the set requirements.\nUSE AT YOUR OWN RISK!"
    )
    public static boolean kuudraAutoKick = false;

    @Page(
            name = "Auto Kick Options",
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_KICK,
            location = PageLocation.BOTTOM
    )
    public KuudraAutoKickOptions kuudraAutoKickOptions = new KuudraAutoKickOptions();

    @Switch(
            name = "Kuudra Splits",
            category = KUUDRA,
            subcategory = KUUDRA_SPLITS
    )
    public static boolean kuudraSplits = true;

    @Switch(
            name = "Kuudra Supply Times",
            category = KUUDRA,
            subcategory = KUUDRA_SPLITS
    )
    public static boolean kuudraSupplyTimes = true;

    @Switch(
            name = "Kuudra Fresh Times",
            category = KUUDRA,
            subcategory = KUUDRA_SPLITS
    )
    public static boolean kuudraFreshTimes = true;

    @Page(
            name = "Kuudra Splits Options",
            category = KUUDRA,
            subcategory = KUUDRA_SPLITS,
            location = PageLocation.BOTTOM
    )
    public KuudraSplitsOptions kuudraSplitsOptions = new KuudraSplitsOptions();

    @Switch(
            name = "Notifications",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotifications = true;

    @Switch(
            name = "Moveable Notifications",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotificationsMoveable = false;

    @Info(
            text = "Kuudra Run Notifications",
            type = InfoType.INFO,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS,
            size = 2
    )
    public static boolean kuudraNotificationsRun;

    @Switch(
            name = "No Equals",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoEquals = true;

    @Switch(
            name = "No Shop",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoShop = true;

    @Switch(
            name = "No X Cannon",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoXCannon = true;

    @Switch(
            name = "No Slash",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoSlash = true;

    @Switch(
            name = "No Square",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoSquare = true;

    @Switch(
            name = "No Triangle",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoTriangle = true;

    @Switch(
            name = "No X",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoX = true;

    @Switch(
            name = "Dropped Chest Warning",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiDropped = true;

    @Switch(
            name = "Cooldown (Sending to server)",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiCooldown = true;

    @Switch(
            name = "Already Grabbing Supplies",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiGrabbing = true;

    @Switch(
            name = "Someone Else Grabbing Supplies",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiGrabbed = true;

    @Switch(
            name = "Placed Supply (You)",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiPlaced = true;

    @Number(
            name = "No Supply Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiNoSupplyTime = 100;

    @Number(
            name = "Dropped Chest Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiDroppedTime = 40;

    @Number(
            name = "Cooldown Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiCooldownTime = 50;

    @Number(
            name = "Already Grabbing Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiGrabbingTime = 40;

    @Number(
            name = "Someone Grabbing Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiGrabbedTime = 40;

    @Number(
            name = "Placed Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiPlacedTime = 40;

    @Info(
            text = "Kuudra Profit Notifications",
            type = InfoType.INFO,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS,
            size = 2
    )
    public static boolean kuudraNotificationsProfit;

    @Switch(
            name = "Total Profit",
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiTotalProfit = false;

    @Number(
            name = "Total Profit Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiTotalProftTime = 100;

    // CRIMSON

    @Switch(
            name = "Use shortened attribute names",
            category = CRIMSON,
            subcategory = CRIMSON_GENERAL
    )
    public static boolean useShortenedAttribute = false;

    @Switch(
            name = "Auction Helper",
            category = CRIMSON,
            subcategory = CRIMSON_AH_HELPER
    )
    public static boolean crimsonAuctionHelper = true;

    @Number(
            name = "Undercut Amount",
            min = 0, max = Integer.MAX_VALUE,
            category = CRIMSON,
            subcategory = CRIMSON_AH_HELPER
    )
    public static int ahHelperUnderCut = 1;

    @Switch(
            name = "Container Helper",
            category = CRIMSON,
            subcategory = CRIMSON_CONTAINER
    )
    public static boolean crimsonContainerHelper = true;

    @Switch(
            name = "Block Non-Highlighted Slot Clicks",
            category = CRIMSON,
            subcategory = CRIMSON_CONTAINER
    )
    public static boolean crimsonContainerBlockClicks = true;

    @Switch(
            name = "Show Prices In Tooltip",
            category = CRIMSON,
            subcategory = CRIMSON_TOOLTIP
    )
    public static boolean crimsonTooltipPrices = true;

    @Switch(
            name = "Show Prices Per Attribute",
            category = CRIMSON,
            subcategory = CRIMSON_TOOLTIP
    )
    public static boolean crimsonTooltipPerAttribute = true;

    @Dropdown(
            name = "KIC Auction Color",
            category = CRIMSON,
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow"
            },
            subcategory = CRIMSON_AH
    )
    public static int KICAuctionColor = 12;

    @Dropdown(
            name = "KIC Auction Style",
            category = CRIMSON,
            options = {
                    "Default", "Prism", "PackHQ"
            },
            subcategory = CRIMSON_AH
    )
    public static int KICAuctionStyle = 1;

    @Switch(
            name = "Auto re-open GUI after buying an item",
            category = CRIMSON,
            subcategory = CRIMSON_AH
    )
    public static boolean openKAGUIAgain = true;

    @Switch(
            name = "Use a default attribute level",
            category = CRIMSON,
            subcategory = CRIMSON_AH
    )
    public static boolean kaUseDefaultAttributeLvl = false;

    @Slider(
            name = "Default Attribute Level",
            category = CRIMSON,
            min = 1, max = 10,
            subcategory = CRIMSON_AH
    )
    public static int kaDefaultAttributeLvl = 5;

    @Slider(
            name = "Kic Auction - (Price/lvl X) - Armor/Equipment/Fishing",
            category = CRIMSON,
            min = 1, max = 10,
            subcategory = CRIMSON_AH
    )
    public static int kaPricePerXLvl = 5;

    @Slider(
            name = "Kic Auction - (Price/lvl X) - Shards",
            category = CRIMSON,
            min = 1, max = 10,
            subcategory = CRIMSON_AH
    )
    public static int kaPricePerXLvlShards = 4;

    // PLAYER

    @Switch(
            name = "Enable player sizing",
            category = PLAYER,
            subcategory = PLAYER_SIZING,
            size = 2,
            description = "Main toggle for smoll people."
    )
    public static boolean playerSizeToggle = false;

    @Slider(
            name = "Player size",
            category = PLAYER,
            subcategory = PLAYER_SIZING,
            min = 0.1f, max = 2.0f
    )
    public static float playerSize = 0.6f;

    @Switch(
            name = "Ignore NPC's",
            category = PLAYER,
            subcategory = PLAYER_SIZING
    )
    public static boolean playerSizeIgnoreNPC = true;

    @Dropdown(
            name = "Player Sizing Method",
            category = PLAYER,
            subcategory = PLAYER_SIZING,
            options = {"Method 1", "Method 2"},
            size = 2
    )
    public static int playerSizingMethod = 0;

    // CHAT

    @Switch(
            name = "MVP++ Emojis",
            category = CHAT,
            description = "Allows sending MVP++ emojis without having MVP++.",
            size = 2
    )
    public static boolean emojis = false;

    @Switch(
            name = "Party Commands",
            category = CHAT,
            subcategory = PARTY_COMMANDS,
            size = 2
    )
    public static boolean partyCommands = true;

    @Switch(
            name = ".runs",
            category = CHAT,
            subcategory = PARTY_COMMANDS,
            description = "T5 Runs\n\nParty > [MVP+] Xaned: .runs rainbode\nParty > [MVP+] Xaned: 10673 runs"
    )
    public static boolean partyCommandRuns = true;

    @Switch(
            name = ".stats",
            category = CHAT,
            subcategory = PARTY_COMMANDS,
            description = "Kuudra Stats\n\nParty > [MVP+] Xaned: .stats rainbode\nParty > [MVP+] Xaned: Lifeline: 80 | Mana pool: 80 | Magical power: 1682"
    )
    public static boolean partyCommandStats = true;

    @Switch(
            name = ".ap",
            category = CHAT,
            subcategory = PARTY_COMMANDS,
            description = "Attribute Price\n\nParty > [MVP+] Xaned: .ap mf 5\nParty > [MVP+] Xaned: Magic Find 5 > Helmet: 5.40M - Cp: 5.22M - Legs: 5.00M - Boots: 4.85M - Neck: 6.00M - Cloak: 20.00M - Belt: 20.00M - Brace: 19.00M"
    )
    public static boolean partyCommandAp = true;

    @Switch(
            name = ".kick",
            category = CHAT,
            subcategory = PARTY_COMMANDS,
            description = "Kick a player\n\nParty > [MVP+] Xaned: .kick SuuerSindre\n-----------------------------------------------------\n[MVP+] SuuerSindre has been removed from the party.\n-----------------------------------------------------"
    )
    public static boolean partyCommandKick = false;

    @Switch(
            name = ".cata",
            category = CHAT,
            subcategory = PARTY_COMMANDS,
            description = "Cata Info\n\nParty > [MVP+] Xaned: .cata Xaned\nParty > &b[MVP+] Xaned: Xaned's Cata: 48.77 - PB: 05:37:20 - MP: 1404 - Secrets: 28.51K"
    )
    public static boolean partyCommandCata = true;

    @Switch(
            name = ".rtca",
            category = CHAT,
            subcategory = PARTY_COMMANDS,
            description = "Road To Class Average\n\nParty > [MVP+] Xaned: .rtca rainbode\nParty > [MVP+] Xaned: rainbode H: 802 - M: 72 - B: 512 - A: 702 - T: 181 (265h) (7min/run)"
    )
    public static boolean partyCommandRtca = true;

    @Switch(
            name = "DM Commands",
            category = CHAT,
            subcategory = DM_COMMANDS,
            size = 2
    )
    public static boolean dmCommands = true;

    @Switch(
            name = ".runs",
            category = CHAT,
            subcategory = DM_COMMANDS,
            description = "T5 Runs\n\nFrom [MVP+] Xaned: .runs rainbode\nTo [MVP+] Xaned: 10673 runs"
    )
    public static boolean dmCommandRuns = true;

    @Switch(
            name = ".stats",
            category = CHAT,
            subcategory = DM_COMMANDS,
            description = "Kuudra Stats\n\nFrom [MVP+] Xaned: .stats rainbode\nTo [MVP+] Xaned: Lifeline: 70 | Mana pool: 70 | Magical power: 1682"
    )
    public static boolean dmCommandStats = true;

    @Switch(
            name = ".ap",
            category = CHAT,
            subcategory = DM_COMMANDS,
            description = "Attribute Price\n\nFrom [MVP+] Xaned: .ap mf 5\nTo [MVP+] Xaned: Magic Find 5 > Helmet: 5.40M - Cp: 5.22M - Legs: 5.00M - Boots: 4.85M - Neck: 6.00M - Cloak: 20.00M - Belt: 20.00M - Brace: 19.00M"
    )
    public static boolean dmCommandAp = true;

    @Switch(
            name = ".cata",
            category = CHAT,
            subcategory = DM_COMMANDS,
            description = "Cata Info\n\nFrom [MVP+] Xaned: .cata Xaned\nTo [MVP+] Xaned: Xaned's Cata: 48.77 - PB: 05:37:20 - MP: 1404 - Secrets: 28.51K"
    )
    public static boolean dmCommandCata = true;

    @Switch(
            name = ".rtca",
            category = CHAT,
            subcategory = DM_COMMANDS,
            description = "Road To Class Average\n\nFrom [MVP+] Xaned: .rtca rainbode\nTo [MVP+] Xaned: rainbode H: 802 - M: 72 - B: 512 - A: 702 - T: 181 (265h) (7min/run)"
    )
    public static boolean dmCommandRtca = true;

    // WS

    @Info(
            text = "Your API key needs to be verified to be able to use this!",
            type = InfoType.WARNING,
            category = WS,
            size = 2
    )
    public static boolean keyVerifiedWarning;

    @Switch(
            name = "KIC Chat",
            category = WS,
            subcategory = KIC_CHAT
    )
    public static boolean kicChat = true;

    @Switch(
            name = "KIC+ Chat (KIC+)",
            category = WS,
            subcategory = KIC_CHAT
    )
    public static boolean kicPlusChat = false;

    @Dropdown(
            name = "KIC Chat Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            category = WS,
            subcategory = KIC_CHAT
    )
    public static int kicChatColor = 15;

    @Dropdown(
            name = "KIC+ Chat Color (KIC+)",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            category = WS,
            subcategory = KIC_CHAT
    )
    public static int kicPlusChatColor = 15;

    @Switch(
            name = "Notify me when Auction Data updates",
            category = WS,
            subcategory = DATA_UPDATES
    )
    public static boolean notifyAhUpdate = false;

    @Dropdown(
            name = "Notification method",
            category = WS,
            subcategory = DATA_UPDATES,
            options = {"Chat", "OneConfig Notification", "Chat + OneConfig Notification"},
            size = 2
    )
    public static int dataUpdateNotificationMethod = 0;

    // MISC

    @Switch(
            name = "Show Current Armor Next To Hotbar",
            category = MISC
    )
    public static boolean showArmorInHud = false;

    @Dropdown(
            name = "Inventory Button Location",
            category = MISC,
            options = {"Left", "Right"},
            size = 2
    )
    public static int invButtonLoc = 0;

    // HUDS

    @KeyBind(
            name = "Edit Huds Keybind",
            description = "Keybind to hold when you want to edit huds currently on your screen.",
            size = 2,
            category = HUD
    )
    public static OneKeyBind hudMoveKeybind = new OneKeyBind(UKeyboard.KEY_LCONTROL);

    @Button(
            name = "Edit All Enabled Huds",
            text = "Click me!",
            description = "Click to edit all enabled huds.",
            size = 2,
            category = HUD
    )
    Runnable editHuds = () -> GuiUtils.displayScreen(new EditHudScreen());

    @Switch(
            name = "Draw Background On Huds",
            description = "Draw a semi transparent background for huds",
            category = HUD
    )
    public static boolean drawHudBackground = false;

    @Switch(
            name = "Use Text Shadows In Huds",
            description = "Use text shadows in hud for text",
            category = HUD
    )
    public static boolean useTextShadowInHuds = true;
}
