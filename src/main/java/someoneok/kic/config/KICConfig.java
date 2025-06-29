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
import someoneok.kic.modules.misc.TrackEmptySlots;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.EditHudScreen;

import java.util.Objects;

import static someoneok.kic.utils.ApiUtils.*;
import static someoneok.kic.utils.GeneralUtils.round2;
import static someoneok.kic.utils.StringUtils.isValidUUIDv4RegexBased;

public class KICConfig extends Config {
    private transient static String oldKey = "";
    private transient static int oldAPInitialDelay = 0;
    private transient static int oldAPDoubleDelay = 0;
    private transient static int oldAPSkyDistance = 0;
    private transient static int oldAPFlatDistance = 0;
    private transient static float oldAPTextSizeScaleSky = 0;
    private transient static float oldAPWaypointSizeScaleSky = 0;
    private transient static float oldAPTextSizeScaleFlat = 0;
    private transient static float oldAPWaypointSizeScaleFlat = 0;

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
        hideIf("autoPearls", () -> !isAdmin());
        hideIf("autoRefillPearls", () -> !isAdmin());
        hideIf("autoRefillPearlsTicks", () -> !isAdmin());
        hideIf("copyNBT", () -> !isDev() && !isTester());

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
        addDependency("kuudraAutoKick", "partyFinder");

        hideIf("showSkyPearls", () -> !pearlCalculator);
        hideIf("showFlatPearls", () -> !pearlCalculator);
        hideIf("showDoublePearls", () -> !pearlCalculator);
        hideIf("showAll", () -> !pearlCalculator);

        hideIf("advancedPearlSettings", () -> !pearlCalculator);
        hideIf("advancedPearlSettingsInfo", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APInitialDelay", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APDoubleDelay", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APSkyDistance", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APFlatDistance", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APTextSizeScaleSky", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APWaypointSizeScaleSky", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APTextSizeScaleFlat", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APWaypointSizeScaleFlat", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APTimerPos", () -> !pearlCalculator || !advancedPearlSettings);

        hideIf("APUseCustomShape", () -> !pearlCalculator || !advancedPearlSettings);
        hideIf("APCustom", () -> !pearlCalculator || !advancedPearlSettings || !APUseCustomShape);
        hideIf("APCustomShapeQuality", () -> !pearlCalculator || !advancedPearlSettings || !APUseCustomShape);

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

        hideIf("showSpotNames", () -> !showSpots);
        hideIf("hideSpotIfClose", () -> !showSpots);

        hideIf("supplySpotColor", () -> !showNothingSupplyWaypoints && !showNothingSupplyWaypointsBeacon);
        hideIf("supplyColor", () -> !supplyWaypoints && !supplyBox);
        hideIf("autoRefillPearlsTicks", () -> !autoRefillPearls);

        addDependency("ACAutoBuy", "kuudraProfitCalculator");
        hideIf("ACAlwaysAutoBuy", () -> !kuudraProfitCalculator || !ACAutoBuy);
        hideIf("ACAutoBuyMinProfit", () -> !kuudraProfitCalculator || !ACAutoBuy);
        hideIf("ACAutoCloseGui", () -> !(ACAutoBuy || ACInstaBuy));
        hideIf("ACAutoCloseDelay", () -> !(ACAutoBuy || ACInstaBuy) || !ACAutoCloseGui);
        hideIf("ACInitialInstaBuyDelay", () -> !ACInstaBuy);
        addDependency("ACAutoReroll", "kuudraProfitCalculator");
        hideIf("ACOnlyRerollInT5", () -> !kuudraProfitCalculator || !ACAutoReroll);
        hideIf("ACShouldRerollType", () -> !kuudraProfitCalculator || !ACAutoReroll);
        hideIf("ACAutoRerollMinValue", () -> !kuudraProfitCalculator || !ACAutoReroll);

        hideIf("kuudraProfitTrackerAddRunTimeDelay", () -> !kuudraProfitTracker);
        hideIf("kuudraProfitTrackerTotalTimeToAdd", () -> !kuudraProfitTracker || !kuudraProfitTrackerAddRunTimeDelay);

        // Notifications
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

        addDependency("kuudraNotiTotalProftTime", "kuudraNotiTotalProfit");

        // Crimson
        addDependency("kaDefaultAttributeLvl", "kaUseDefaultAttributeLvl");

        // Chat
        hideIf("partyCommandRuns", () -> !partyCommands);
        hideIf("partyCommandStats", () -> !partyCommands);
        hideIf("partyCommandAp", () -> !partyCommands);
        hideIf("partyCommandKick", () -> !partyCommands);
        hideIf("partyCommandCata", () -> !partyCommands);
        hideIf("partyCommandRtca", () -> !partyCommands);
        hideIf("dmCommandRuns", () -> !dmCommands);
        hideIf("dmCommandStats", () -> !dmCommands);
        hideIf("dmCommandAp", () -> !dmCommands);
        hideIf("dmCommandCata", () -> !dmCommands);
        hideIf("dmCommandRtca", () -> !dmCommands);

        // Misc
        registerKeyBind(openEmptyECBP, TrackEmptySlots::openEmptyEcOrBp);
        hideIf("trackEnderChestPages", () -> !trackEmptyECBP);
        hideIf("trackBackpacks", () -> !trackEmptyECBP);
        hideIf("openEmptyECBP", () -> !trackEmptyECBP);

        for (int i = 1; i <= 9; i++) {
            hideIf("ec" + i, () -> !trackEmptyECBP || !trackEnderChestPages);
        }

        for (int i = 1; i <= 18; i++) {
            hideIf("bp" + i, () -> !trackEmptyECBP || !trackBackpacks);
        }
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
    private transient static final String DEV = "Dev";

    // KUUDRA
    private transient static final String KUUDRA_PARTY_FINDER = "Kuudra Party Finder";
    private transient static final String KUUDRA_SPLITS = "Kuudra Splits";
    private transient static final String KUUDRA_BOSS = "Kuudra Boss";
    private transient static final String KUUDRA_WAYPOINTS = "Kuudra Waypoints";
    private transient static final String KUUDRA_P1 = "Kuudra Phase 1";
    private transient static final String KUUDRA_P2 = "Kuudra Phase 2";
    private transient static final String KUUDRA_PROFIT_CALC = "Kuudra Profit Calculator";
    private transient static final String KUUDRA_AUTO_CHEST = "Kuudra Auto Chest";
    private transient static final String KUUDRA_PROFIT_TRACKER = "Kuudra Profit Tracker";
    private transient static final String KUUDRA_NOTIFICATIONS = "Kuudra Notifications";

    // CRIMSON
    private transient static final String CRIMSON_GENERAL = "General";
    private transient static final String CRIMSON_CONTAINER = "Container";

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

    @KeyBind(
            name = "Copy NBT to clipboard",
            size = 2,
            subcategory = DEV
    )
    public static OneKeyBind copyNBT = new OneKeyBind(UKeyboard.KEY_NONE);

    // KUUDRA

    // KUUDRA_PARTY_FINDER
    @Switch(
            name = "Party Finder",
            category = KUUDRA,
            subcategory = KUUDRA_PARTY_FINDER,
            description = "Toggle Party Finder stats."
    )
    public static boolean partyFinder = true;

    @Switch(
            name = "Party Finder Gui Stats (KIC+)",
            category = KUUDRA,
            subcategory = KUUDRA_PARTY_FINDER
    )
    public static boolean partyFinderGuiStats = false;

    @Dropdown(
            name = "Kuudra Player Stats Style",
            options = {
                    "KIC", "Attribute Mod"
            },
            category = KUUDRA,
            subcategory = KUUDRA_PARTY_FINDER
    )
    public static int kuudraStatsStyle = 0;

    @Info(
            text = "Auto Kick is a \"USE AT YOUR OWN RISK!\" feature!",
            type = InfoType.WARNING,
            category = KUUDRA,
            subcategory = KUUDRA_PARTY_FINDER,
            size = 2
    )
    public static boolean UseAtYourOwnRisk0;

    @Switch(
            name = "Auto Kick",
            category = KUUDRA,
            subcategory = KUUDRA_PARTY_FINDER,
            size = 2,
            description = "Automatically kicks people who do not meet the set requirements.\nUSE AT YOUR OWN RISK!"
    )
    public static boolean kuudraAutoKick = false;

    @Page(
            name = "Auto Kick Options",
            category = KUUDRA,
            subcategory = KUUDRA_PARTY_FINDER,
            location = PageLocation.BOTTOM
    )
    public KuudraAutoKickOptions kuudraAutoKickOptions = new KuudraAutoKickOptions();

    // KUUDRA_SPLITS
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

    // KUUDRA_BOSS
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

    // KUUDRA_WAYPOINTS
    @Switch(
            name = "Supply Drop Spot Waypoint",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showNothingSupplyWaypoints = false;

    @Switch(
            name = "Supply Drop Spot Beacon",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showNothingSupplyWaypointsBeacon = false;

    @Color(
            name = "Supply Drop Spot Color",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static OneColor supplySpotColor = new OneColor(255,255,255);

    @Switch(
            name = "Dynamic Pearl Calculator",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            description = "Shows you where to throw your pearl for from any position to land on the supply spot.",
            size = 2
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
            name = "Show All Supply Waypoints",
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS,
            description = "Show all waypoints that don’t have supplies placed yet, instead of only those relevant to the current location.\nWARNING: Significant performance impact!"
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
    public static float APWaypointSizeScaleSky = 0.75f;

    @Slider(
            name = "Pearl Timer Size Scale (Flat)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APTextSizeScaleFlat = 0.75f;

    @Slider(
            name = "Pearl Waypoint Size Scale (Flat)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APWaypointSizeScaleFlat = 0.75f;

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

    // KUUDRA_P1
    @Switch(
            name = "Show Pre Spots",
            category = KUUDRA,
            subcategory = KUUDRA_P1,
            size = 2
    )
    public static boolean showSpots = false;

    @Switch(
            name = "Show Pre Spot Names",
            category = KUUDRA,
            subcategory = KUUDRA_P1
    )
    public static boolean showSpotNames = false;

    @Switch(
            name = "Hide Pre Spot If close",
            category = KUUDRA,
            subcategory = KUUDRA_P1
    )
    public static boolean hideSpotIfClose = true;

    @Switch(
            name = "Show Kuudra Supply Waypoints",
            category = KUUDRA,
            subcategory = KUUDRA_P1
    )
    public static boolean supplyWaypoints = true;

    @Switch(
            name = "Show Supply Hitbox",
            category = KUUDRA,
            subcategory = KUUDRA_P1
    )
    public static boolean supplyBox = false;

    @Color(
            name = "Supply Color",
            category = KUUDRA,
            subcategory = KUUDRA_P1,
            size = 2
    )
    public static OneColor supplyColor = new OneColor(0, 255, 180);

    @Switch(
            name = "Auto Refill Pearls In Hotbar",
            category = KUUDRA,
            subcategory = KUUDRA_P1
    )
    public static boolean autoRefillPearls = false;

    @Number(
            name = "Pearl Refill Timer In Ticks",
            min = 10, max = 600,
            step = 10,
            category = KUUDRA,
            subcategory = KUUDRA_P1
    )
    public static int autoRefillPearlsTicks = 100;

    // KUUDRA_P2
    @Switch(
            name = "Elle ESP",
            category = KUUDRA,
            subcategory = KUUDRA_P2
    )
    public static boolean elleESP = true;

    @Switch(
            name = "Supply Pile Build Progress Beacon",
            category = KUUDRA,
            subcategory = KUUDRA_P2
    )
    public static boolean supplyWaypointsProgress = true;

    // KUUDRA_PROFIT_CALC
    @Switch(
            name = "Profit Calculator",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC,
            size = 2
    )
    public static boolean kuudraProfitCalculator = true;

    @Switch(
            name = "Show Valuable Hologram",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC
    )
    public static boolean showValuableHolo = true;

    @Switch(
            name = "Show Valuable Hologram From Both Sides",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC,
            size = 2,
            description = "Only turn this off if the Valuable hologram is displayed twice otherwise leave it on."
    )
    public static boolean showValuableHoloBothSides = true;

    @Page(
            name = "Profit Calculator Options",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_CALC,
            location = PageLocation.BOTTOM
    )
    public KuudraProfitCalculatorOptions kuudraProfitCalculatorOptions = new KuudraProfitCalculatorOptions();

    // KUUDRA_AUTO_CHEST
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
            subcategory = KUUDRA_AUTO_CHEST,
            description = "Automatically open the chest after profit has been calculated, waits profit calculator.",
            size = 2
    )
    public static boolean ACAutoBuy = false;

    @Number(
            name = "Minimum Total Profit For Auto Buy",
            description = "Set the minimum total profit required to auto buy the chest. (Set to 0 to turn off)",
            min = 0, max = 100_000_000,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static int ACAutoBuyMinProfit = 0;

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
            description = "Instantly open the chest when the GUI opens, ignoring profit calculator. Does not reroll even if auto-reroll is enabled."
    )
    public static boolean ACInstaBuy = false;

    @Number(
            name = "Delay Before Insta Buy After Opening GUI",
            min = 100, max = 1000,
            step = 50,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static int  ACInitialInstaBuyDelay = 300;

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
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static int ACAutoCloseDelay = 250;

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
            name = "Reroll If First Two Slots Are Under",
            description = "Specifies the minimum combined value of the first 2 slots required to avoid a reroll. If their total value is below this threshold, the chest will be rerolled.",
            min = 1, max = 10_000_000,
            category = KUUDRA,
            subcategory = KUUDRA_AUTO_CHEST
    )
    public static int ACAutoRerollMinValue = 2_000_000;

    // KUUDRA_PROFIT_TRACKER
    @Switch(
            name = "Profit Tracker",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_TRACKER,
            size = 2
    )
    public static boolean kuudraProfitTracker = true;

    @Switch(
            name = "Include Delay In Run Time",
            category = KUUDRA,
            subcategory = KUUDRA_PROFIT_TRACKER
    )
    public static boolean kuudraProfitTrackerAddRunTimeDelay = true;

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
            name = "Hunting Box Value",
            category = CRIMSON,
            subcategory = CRIMSON_CONTAINER
    )
    public static boolean crimsonHuntingBoxValue = true;

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
            category = MISC,
            size = 2
    )
    public static boolean showArmorInHud = false;

    @Dropdown(
            name = "Inventory Button Location",
            category = MISC,
            options = {"Left", "Right"},
            size = 2
    )
    public static int invButtonLoc = 0;

    @Switch(
            name = "Track Empty Slots In Ender Chest Pages And Backpacks",
            category = MISC,
            size = 2
    )
    public static boolean trackEmptyECBP = false;

    @Switch(
            name = "Track Ender Chest Pages",
            category = MISC,
            size = 2
    )
    public static boolean trackEnderChestPages = false;

    @Switch(
            name = "Track Backpacks",
            category = MISC,
            size = 2
    )
    public static boolean trackBackpacks = true;

    @KeyBind(
            name = "Open EC or BP With Empty Slots Keybind",
            description = "Key to press when you want to open an ender chest page or backpack with empty slots.",
            size = 2,
            category = MISC
    )
    public static OneKeyBind openEmptyECBP = new OneKeyBind(UKeyboard.KEY_NONE);

    @Switch(
            name = "Ender Chest Page 1",
            category = MISC
    )
    public static boolean ec1 = false;

    @Switch(
            name = "Ender Chest Page 2",
            category = MISC
    )
    public static boolean ec2 = false;

    @Switch(
            name = "Ender Chest Page 3",
            category = MISC
    )
    public static boolean ec3 = false;

    @Switch(
            name = "Ender Chest Page 4",
            category = MISC
    )
    public static boolean ec4 = false;

    @Switch(
            name = "Ender Chest Page 5",
            category = MISC
    )
    public static boolean ec5 = false;

    @Switch(
            name = "Ender Chest Page 6",
            category = MISC
    )
    public static boolean ec6 = false;

    @Switch(
            name = "Ender Chest Page 7",
            category = MISC
    )
    public static boolean ec7 = false;

    @Switch(
            name = "Ender Chest Page 8",
            category = MISC
    )
    public static boolean ec8 = false;

    @Switch(
            name = "Ender Chest Page 9",
            category = MISC
    )
    public static boolean ec9 = false;

    @Switch(
            name = "Backpack 1",
            category = MISC
    )
    public static boolean bp1 = false;

    @Switch(
            name = "Backpack 2",
            category = MISC
    )
    public static boolean bp2 = false;

    @Switch(
            name = "Backpack 3",
            category = MISC
    )
    public static boolean bp3 = false;

    @Switch(
            name = "Backpack 4",
            category = MISC
    )
    public static boolean bp4 = false;

    @Switch(
            name = "Backpack 5",
            category = MISC
    )
    public static boolean bp5 = false;

    @Switch(
            name = "Backpack 6",
            category = MISC
    )
    public static boolean bp6 = false;

    @Switch(
            name = "Backpack 7",
            category = MISC
    )
    public static boolean bp7 = false;

    @Switch(
            name = "Backpack 8",
            category = MISC
    )
    public static boolean bp8 = false;

    @Switch(
            name = "Backpack 9",
            category = MISC
    )
    public static boolean bp9 = false;

    @Switch(
            name = "Backpack 10",
            category = MISC
    )
    public static boolean bp10 = false;

    @Switch(
            name = "Backpack 11",
            category = MISC
    )
    public static boolean bp11 = false;

    @Switch(
            name = "Backpack 12",
            category = MISC
    )
    public static boolean bp12 = false;

    @Switch(
            name = "Backpack 13",
            category = MISC
    )
    public static boolean bp13 = false;

    @Switch(
            name = "Backpack 14",
            category = MISC
    )
    public static boolean bp14 = false;

    @Switch(
            name = "Backpack 15",
            category = MISC
    )
    public static boolean bp15 = false;

    @Switch(
            name = "Backpack 16",
            category = MISC
    )
    public static boolean bp16 = false;

    @Switch(
            name = "Backpack 17",
            category = MISC
    )
    public static boolean bp17 = false;

    @Switch(
            name = "Backpack 18",
            category = MISC
    )
    public static boolean bp18 = false;

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
