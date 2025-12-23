package someoneok.kic.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigPageButton;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import someoneok.kic.KIC;
import someoneok.kic.addons.AddonConfigIO;
import someoneok.kic.addons.AddonHandle;
import someoneok.kic.addons.AddonHelpers;
import someoneok.kic.addons.AddonRegistry;
import someoneok.kic.api.AddonConfigWiring;
import someoneok.kic.config.pages.KuudraAutoKickOptions;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.config.pages.KuudraProfitTrackerOptions;
import someoneok.kic.config.pages.KuudraSplitsOptions;
import someoneok.kic.config.sharing.DoNotShare;
import someoneok.kic.modules.kuudra.Pearls;
import someoneok.kic.modules.misc.ButtonManager;
import someoneok.kic.modules.misc.TrackEmptySlots;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.FileUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.kuudra.TrajectorySolver;
import someoneok.kic.utils.overlay.EditHudScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static someoneok.kic.addons.AddonHelpers.ADDON_DIR;
import static someoneok.kic.utils.ApiUtils.*;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
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
    private transient final List<BasicOption> addonIndexButtons = new ArrayList<>();

    public void rebuildAddonsIndex() {
        OptionSubcategory sub = ConfigUtils.getSubCategory(mod.defaultPage, ADDONS, ADDON_INSTALLED);

        if (!addonIndexButtons.isEmpty()) {
            sub.options.removeAll(addonIndexButtons);
            addonIndexButtons.clear();
        }

        for (AddonHandle h : AddonRegistry.handles()) {
            Class<?> pageKlass = h.addon.getConfigPageClass();
            if (pageKlass == null) continue;

            try {
                Object pageInstance = pageKlass.newInstance();
                EphemeralConfig eph = new EphemeralConfig(this.mod);
                OptionPage page = eph.buildPage(pageInstance, h.addon.getName() + " Settings");
                if (h.addon instanceof AddonConfigWiring) ((AddonConfigWiring) h.addon).wire(eph);

                ConfigPageButton btn = new ConfigPageButton(
                        null, this,
                        h.addon.getName(),
                        "Open " + h.addon.getName() + " settings",
                        ADDONS, "Installed",
                        page
                );

                sub.options.add(btn);
                addonIndexButtons.add(btn);
            } catch (Throwable ignored) {}
        }
    }

    public KICConfig() {
        super(new Mod("Kuudraiscool", ModType.SKYBLOCK), "kic.json");
        initialize();
        oldKey = apiKey;

        addDependency("playerSize", "playerSizeToggle");
        addDependency("playerSizeIgnoreNPC", "playerSizeToggle");
        addDependency("kicChat", "verifiedApiKeyChecker", ApiUtils::isVerified);

        // Key info
        hideIf("keyErrorError", () -> !ApiUtils.isApiKeyError());
        hideIf("keyActiveSuccess", () -> ApiUtils.isApiKeyError() && !ApiUtils.isVerified());
        hideIf("keyVerifiedWarning", ApiUtils::isVerified);

        // Admin, Tester & Beta
        hideIf("boneTrackerDebug", () -> !isTester() && !isDev() && !isBeta());
        hideIf("apiToUse", () -> !isTester() && !isDev() && !isBeta());
        hideIf("copyNBT", () -> !isTester() && !isDev() && !isBeta());
        hideIf("showDebugInfo", () -> !isTester() && !isDev() && !isBeta());
        hideIf("forceKuudraLocation", () -> !isDev() && !isTester());
        hideIf("forceKuudraTier", () -> !isDev() && !isTester());
        hideIf("forceKuudraPhase", () -> !isDev() && !isTester());

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
        addDependency("showValuableHolo", "kuudraProfitCalculator");
        addDependency("kuudraAutoKick", "partyFinder");

        hideIf("kuudraTalisman", () -> !pearlCalculator);
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

        addListener("advancedPearlSettings", () -> {
            Pearls.updateSizes();
            Pearls.updatePearlDelays();
        });
        addListener("APInitialDelay", () -> {
            if (Objects.equals(APInitialDelay, oldAPInitialDelay)) return;
            oldAPInitialDelay = APInitialDelay;
            Pearls.updatePearlDelays();
        });
        addListener("APDoubleDelay", () -> {
            if (Objects.equals(APDoubleDelay, oldAPDoubleDelay)) return;
            oldAPDoubleDelay = APDoubleDelay;
            Pearls.updatePearlDelays();
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
            Pearls.updateSizes();
        });
        addListener("APWaypointSizeScaleSky", () -> {
            if (Objects.equals(round2(APWaypointSizeScaleSky), oldAPWaypointSizeScaleSky)) return;
            oldAPWaypointSizeScaleSky = round2(APWaypointSizeScaleSky);
            Pearls.updateSizes();
        });
        addListener("APTextSizeScaleFlat", () -> {
            if (Objects.equals(round2(APTextSizeScaleFlat), oldAPTextSizeScaleFlat)) return;
            oldAPTextSizeScaleFlat = round2(APTextSizeScaleFlat);
            Pearls.updateSizes();
        });
        addListener("APWaypointSizeScaleFlat", () -> {
            if (Objects.equals(round2(APWaypointSizeScaleFlat), oldAPWaypointSizeScaleFlat)) return;
            oldAPWaypointSizeScaleFlat = round2(APWaypointSizeScaleFlat);
            Pearls.updateSizes();
        });

        addListener("APTimerPos", Pearls::updateTimerLocation);
        addListener("invButtonLoc", ButtonManager::updateCheckboxAlignment);

        hideIf("elleMessageStyle", () -> elleShutTheFuckUp);
        hideIf("teamHighlightColor", () -> !teamHighlight);

        hideIf("supplySpotColor", () -> !showNothingSupplyWaypoints && !showNothingSupplyWaypointsBeacon);
        hideIf("supplyColor", () -> !supplyWaypoints && !supplyBox);

        hideIf("showKuudraProfitTrackerNearCroesus", () -> !kuudraProfitTracker);
        hideIf("kuudraProfitTrackerAddRunTimeDelay", () -> !kuudraProfitTracker);
        hideIf("kuudraProfitTrackerTotalTimeToAdd", () -> !kuudraProfitTracker || !kuudraProfitTrackerAddRunTimeDelay);

        hideIf("kuudraMobSize", () -> !tinyKuudraMobs);

        hideIf("ichorCircleQuality", () -> !ichorPool);

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

        hideIf("backboneOnIslandFront", () -> !backboneOnIsland);

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

        hideIf("wardrobePlaySound", () -> !wardrobeKeybinds);
        hideIf("wardrobeSound", () -> !wardrobeKeybinds || !wardrobePlaySound);
        for (int i = 1; i <= 9; i++) {
            hideIf("wardrobeSlot" + i, () -> !wardrobeKeybinds);
        }

        hideIf("noAddonsInfo", () -> !AddonRegistry.noAddons());
    }

    @Override
    public void save() {
        super.save();
        AddonConfigIO.saveAllAddons();
    }

    // Categories
    private transient static final String KUUDRA_MISC = "Kuudra Misc";
    private transient static final String KUUDRA_RUN = "Kuudra Run";
    private transient static final String KUUDRA_END = "Kuudra End";
    private transient static final String CHAT = "Chat";
    private transient static final String MISC = "Miscellaneous";
    private transient static final String ADDONS = "Addons";

    // Modules

    // GENERAL
    private transient static final String KEY = "Key";
    private transient static final String HUD = "Huds";
    private transient static final String PRESETS = "Presets";
    private transient static final String DEV = "Dev";

    // KUUDRA_MISC
    private transient static final String KUUDRA_PARTY_FINDER = "Kuudra Party Finder";
    private transient static final String KUUDRA_SPLITS = "Kuudra Splits";
    private transient static final String KUUDRA_VESUVIUS = "Vesuvius & Croesus";
    private transient static final String KUUDRA_NOTIFICATIONS = "Kuudra Notifications";

    // KUUDRA_RUN
    private transient static final String KUUDRA_P1 = "Kuudra Phase 1";
    private transient static final String KUUDRA_WAYPOINTS = "Kuudra Waypoints";
    private transient static final String KUUDRA_P2 = "Kuudra Phase 2";
    private transient static final String KUUDRA_P4 = "Kuudra Phase 4";
    private transient static final String KUUDRA_BOSS = "Kuudra Boss";
    private transient static final String KUUDRA_RUN_MISC = "Miscellaneous";

    // KUUDRA_END
    private transient static final String KUUDRA_REQUEUE = "Kuudra Requeue";
    private transient static final String KUUDRA_PROFIT_CALC = "Kuudra Profit Calculator";
    private transient static final String KUUDRA_PROFIT_TRACKER = "Kuudra Profit Tracker";

    // CHAT
    private transient static final String KIC_CHAT = "KIC Chat";
    private transient static final String PARTY_COMMANDS = "Party Commands";
    private transient static final String DM_COMMANDS = "DM Commands";

    // MISC
    private transient static final String BONE = "Bonemerang";
    private transient static final String WARDROBE = "Wardrobe";
    private transient static final String BPEC = "Backpack & Enderchest";
    private transient static final String PLAYER_SIZING = "Player Sizing";

    // ADDONS
    private transient static final String ADDON_HELP = "General";
    private transient static final String ADDON_INSTALLED = "Installed";

    // Config options

    // GENERAL

    @Info(
            text = "Your API key needs to be verified to be able to use this!",
            type = InfoType.ERROR,
            size = 2,
            subcategory = KEY
    )
    private transient static boolean keyErrorError;

    @Info(
            text = "Your API key has been verified and is active!",
            type = InfoType.SUCCESS,
            size = 2,
            subcategory = KEY
    )
    private transient static boolean keyActiveSuccess;

    @DoNotShare
    @Text(
            name = "KIC API Key",
            description = "Enter your kuudraiscool API key here.",
            secure = true,
            subcategory = KEY
    )
    public static String apiKey = "";

    @Button(
            name = "Verify kuudraiscool API key",
            text = "Click me!",
            size = 2,
            subcategory = KEY
    )
    Runnable verifyKey = () -> {
        if (ApiUtils.isVerified() && Objects.equals(apiKey, oldKey)) return;
        if (!isValidUUIDv4RegexBased(apiKey)) return;
        KICLogger.forceInfo("Api key verification triggered from button");
        oldKey = apiKey;
        Multithreading.runAsync(() -> verifyApiKey(true));
    };

    @KeyBind(
            name = "Edit Huds Keybind",
            description = "Keybind to hold when you want to edit huds currently on your screen.",
            size = 2,
            subcategory = HUD
    )
    public static OneKeyBind hudMoveKeybind = new OneKeyBind(UKeyboard.KEY_LCONTROL);

    @Button(
            name = "Edit All Enabled Huds",
            text = "Click me!",
            description = "Click to edit all enabled huds.",
            size = 2,
            subcategory = HUD
    )
    Runnable editHuds = () -> GuiUtils.displayScreen(new EditHudScreen());

    @Switch(
            name = "Draw Background On Huds",
            description = "Draw a semi transparent background for huds",
            subcategory = HUD
    )
    public static boolean drawHudBackground = false;

    @Switch(
            name = "Use Text Shadows In Huds",
            description = "Use text shadows in hud for text",
            subcategory = HUD
    )
    public static boolean useTextShadowInHuds = true;

    @Info(
            text = "Clicking the preset button will immediately reset your entire config to the selected preset. There is no confirmation!",
            type = InfoType.WARNING,
            subcategory = PRESETS,
            size = 2
    )
    private transient static boolean presetWarning;

    @Button(
            name = "Default preset",
            text = "Click me!",
            subcategory = PRESETS
    )
    Runnable useDefault = ConfigPresets::defaultPreset;

    @DoNotShare
    @Switch(
            name = "Log websocket messages",
            subcategory = DEV
    )
    public static boolean logWebsocketMessages = false;

    @DoNotShare
    @Dropdown(
            name = "API To Use",
            options = {
                    "Main", "Test"
            },
            size = 2,
            subcategory = DEV
    )
    public static int apiToUse = 0;

    @DoNotShare
    @Switch(
            name = "Force Location To Kuudra",
            size = 2,
            subcategory = DEV
    )
    public static boolean forceKuudraLocation = false;

    @DoNotShare
    @Dropdown(
            name = "Force Kuudra Tier",
            options = {
                    "Off", "1", "2", "3", "4", "5"
            },
            size = 2,
            subcategory = DEV
    )
    public static int forceKuudraTier = 0;

    @DoNotShare
    @Dropdown(
            name = "Force Kuudra Phase",
            options = {
                    "Off", "1", "2", "3", "4", "5", "6", "7", "8"
            },
            size = 2,
            subcategory = DEV
    )
    public static int forceKuudraPhase = 0;

    // KUUDRA_MISC
    @Switch(
            name = "Party Finder",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_PARTY_FINDER,
            description = "Toggle Party Finder stats."
    )
    public static boolean partyFinder = true;

    @Switch(
            name = "Party Finder Gui Stats (KIC+)",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_PARTY_FINDER
    )
    public static boolean partyFinderGuiStats = false;

    @Info(
            text = "Auto Kick is a \"USE AT YOUR OWN RISK!\" feature!",
            type = InfoType.WARNING,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_PARTY_FINDER,
            size = 2
    )
    private transient static boolean UseAtYourOwnRisk0;

    @Switch(
            name = "Auto Kick",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_PARTY_FINDER,
            size = 2,
            description = "Automatically kicks people who do not meet the set requirements.\nUSE AT YOUR OWN RISK!"
    )
    public static boolean kuudraAutoKick = false;

    @Page(
            name = "Auto Kick Options",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_PARTY_FINDER,
            location = PageLocation.BOTTOM
    )
    public static KuudraAutoKickOptions kuudraAutoKickOptions = new KuudraAutoKickOptions();

    @Switch(
            name = "Kuudra Splits",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_SPLITS
    )
    public static boolean kuudraSplits = true;

    @Switch(
            name = "Kuudra Supply Times",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_SPLITS
    )
    public static boolean kuudraSupplyTimes = true;

    @Switch(
            name = "Kuudra Fresh Times",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_SPLITS
    )
    public static boolean kuudraFreshTimes = true;

    @Page(
            name = "Kuudra Splits Options",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_SPLITS,
            location = PageLocation.BOTTOM
    )
    public static KuudraSplitsOptions kuudraSplitsOptions = new KuudraSplitsOptions();

    @Switch(
            name = "Highlight unopened chests",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_VESUVIUS
    )
    public static boolean highlightUnopenedChests = false;

    @Switch(
            name = "Notifications",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotifications = true;

    @Switch(
            name = "Moveable Notifications",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotificationsMoveable = false;

    @Info(
            text = "Kuudra Run Notifications",
            type = InfoType.INFO,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS,
            size = 2
    )
    private transient static boolean kuudraNotificationsRun;

    @Switch(
            name = "No Equals",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoEquals = true;

    @Switch(
            name = "No Shop",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoShop = true;

    @Switch(
            name = "No X Cannon",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoXCannon = true;

    @Switch(
            name = "No Slash",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoSlash = true;

    @Switch(
            name = "No Square",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoSquare = true;

    @Switch(
            name = "No Triangle",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoTriangle = true;

    @Switch(
            name = "No X",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiNoX = true;

    @Switch(
            name = "Dropped Chest Warning",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiDropped = true;

    @Switch(
            name = "Cooldown (Sending to server)",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiCooldown = true;

    @Switch(
            name = "Already Grabbing Supplies",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiGrabbing = true;

    @Switch(
            name = "Someone Else Grabbing Supplies",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiGrabbed = true;

    @Switch(
            name = "Placed Supply (You)",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiPlaced = true;

    @Switch(
            name = "Fresh",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiFresh = true;

    @Number(
            name = "No Supply Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiNoSupplyTime = 100;

    @Number(
            name = "Dropped Chest Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiDroppedTime = 40;

    @Number(
            name = "Cooldown Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiCooldownTime = 50;

    @Number(
            name = "Already Grabbing Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiGrabbingTime = 40;

    @Number(
            name = "Someone Grabbing Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiGrabbedTime = 40;

    @Number(
            name = "Placed Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiPlacedTime = 40;

    @Number(
            name = "Fresh Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiFreshTime = 40;

    @Info(
            text = "Kuudra Profit Notifications",
            type = InfoType.INFO,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS,
            size = 2
    )
    private transient static boolean kuudraNotificationsProfit;

    @Switch(
            name = "Total Profit",
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static boolean kuudraNotiTotalProfit = false;

    @Number(
            name = "Total Profit Time (In Ticks)",
            min = 1, max = 1000,
            category = KUUDRA_MISC,
            subcategory = KUUDRA_NOTIFICATIONS
    )
    public static int kuudraNotiTotalProftTime = 100;

    // KUUDRA_RUN
    @Switch(
            name = "No Pre",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P1
    )
    public static boolean noPre = true;

    @Switch(
            name = "Show Kuudra Supply Waypoints",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P1
    )
    public static boolean supplyWaypoints = true;

    @Switch(
            name = "Show Supply Hitbox",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P1
    )
    public static boolean supplyBox = false;

    @Color(
            name = "Supply Color",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P1,
            size = 2
    )
    public static OneColor supplyColor = new OneColor(0, 255, 180);

    @Switch(
            name = "Moveable Crate Pickup Overlay",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P1
    )
    public static boolean moveableCratePickup = false;

    @Switch(
            name = "Custom Supply Drop Message",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P1
    )
    public static boolean customSupplyDropMsg = false;

    @Switch(
            name = "Supply Drop Spot Waypoint",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showNothingSupplyWaypoints = false;

    @Switch(
            name = "Supply Drop Spot Beacon",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showNothingSupplyWaypointsBeacon = false;

    @Color(
            name = "Supply Drop Spot Color",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static OneColor supplySpotColor = new OneColor(255, 255, 255);

    @Switch(
            name = "Dynamic Pearl Calculator",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS,
            description = "Shows you where to throw your pearl for from any position to land on the supply spot.",
            size = 2
    )
    public static boolean pearlCalculator = true;

    @Dropdown(
            name = "Kuudra Talisman",
            options = {"No Talisman", "Kuudra's Kidney", "Kuudra's Lung", "Kuudra's Heart"},
            size = 2,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static int kuudraTalisman = 0;

    @Switch(
            name = "Show Sky Pearl",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showSkyPearls = true;

    @Switch(
            name = "Show Flat Pearl",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showFlatPearls = true;

    @Switch(
            name = "Show Double Pearl",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showDoublePearls = true;

    @Switch(
            name = "Show All Supply Waypoints",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS,
            description = "Show all waypoints that don’t have supplies placed yet, instead of only those relevant to the current location.\nWARNING: Significant performance impact!"
    )
    public static boolean showAll = false;

    @Switch(
            name = "Show Throw Timer",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean showTimer = true;

    @Info(
            text = "Enabling advanced pearl settings may negatively impact performance. Use with caution!",
            type = InfoType.WARNING,
            size = 2,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    private transient static boolean advancedPearlSettingsWarning;

    @Switch(
            name = "Use Advanced Settings",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static boolean advancedPearlSettings = false;

    @Info(
            text = "Advanced Pearl Settings",
            type = InfoType.INFO,
            size = 2,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    private transient static boolean advancedPearlSettingsInfo;

    @Number(
            name = "Initial Pearl Delay (In ms) (Sky, Flat, Double)",
            min = -1000, max = 1000,
            step = 50,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APInitialDelay = 0;

    @Number(
            name = "Added Double Pearl Delay (In ms)",
            min = -1000, max = 1000,
            step = 50,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APDoubleDelay = 0;

    @Number(
            name = "Waypoint Offset Distance (In blocks) (Sky, Double)",
            min = 1, max = 100,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APSkyDistance = 20;

    @Number(
            name = "Waypoint Offset Distance (In blocks) (Flat)",
            min = 1, max = 100,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS,
            size = 2
    )
    public static int APFlatDistance = 15;

    @Slider(
            name = "Pearl Timer Size Scale (Sky, Double)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APTextSizeScaleSky = 0.75f;

    @Slider(
            name = "Pearl Waypoint Size Scale (Sky, Double)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APWaypointSizeScaleSky = 0.75f;

    @Slider(
            name = "Pearl Timer Size Scale (Flat)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APTextSizeScaleFlat = 0.75f;

    @Slider(
            name = "Pearl Waypoint Size Scale (Flat)",
            min = 0.1f, max = 2f,
            instant = true,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static float APWaypointSizeScaleFlat = 0.75f;

    @Dropdown(
            name = "Timer Position",
            options = {"Above", "Under"},
            size = 2,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_WAYPOINTS
    )
    public static int APTimerPos = 1;

    @Switch(
            name = "Elle ESP",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P2
    )
    public static boolean elleESP = true;

    @Switch(
            name = "Supply Pile Build Progress Beacon",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P2
    )
    public static boolean supplyWaypointsProgress = true;

    @Switch(
            name = "Build Timer",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P2,
            description = "Show a countdown timer until you can start building after last supply gets dropped off."
    )
    public static boolean buildTimer = false;

    @Switch(
            name = "Announce Fresh",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P2
    )
    public static boolean announceFresh = false;

    @Switch(
            name = "Backbone Hit",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P4
    )
    public static boolean backboneHit = false;

    @Switch(
            name = "Rend Now",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P4
    )
    public static boolean rendNow = false;

    @Switch(
            name = "Show Ichor Pool",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P4
    )
    public static boolean ichorPool = false;

    @Number(
            name = "Ichor Pool Render Quality",
            min = 8, max = 256,
            step = 8,
            category = KUUDRA_RUN,
            subcategory = KUUDRA_P4
    )
    public static int ichorCircleQuality = 32;

    @Switch(
            name = "Kuudra Boss Bar",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_BOSS
    )
    public static boolean showKuudraBossBar = true;

    @Switch(
            name = "Show Kuudra Outline",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_BOSS
    )
    public static boolean showKuudraOutline = true;

    @Switch(
            name = "Show Kuudra Health",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_BOSS
    )
    public static boolean showKuudraHealth = true;

    @Switch(
            name = "Rend Damage",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_BOSS
    )
    public static boolean rendDamage = true;

    @Switch(
            name = "Kuudra Spawn Direction",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_BOSS
    )
    public static boolean kuudraDirection = false;

    @Switch(
            name = "Elle Shut The Fuck Up",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static boolean elleShutTheFuckUp = false;

    @Dropdown(
            name = "Elle Message Style",
            description = "Choose the style of Elle's messages",
            options = {"Off", "Roadman", "Pirate", "Zoomer", "Emo", "Shakespeare"},
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static int elleMessageStyle = 0;

    @Switch(
            name = "Hide Mob Nametags",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static boolean hideMobNametags = false;

    @Switch(
            name = "Hide Useless Armor Stands",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static boolean hideUselessArmorStands = false;

    @Switch(
            name = "Tiny Kuudra Mobs",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static boolean tinyKuudraMobs = false;

    @Slider(
            name = "Kuudra Mob Size",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC,
            min = 0.1f, max = 1.0f
    )
    public static float kuudraMobSize = 0.6f;

    @Switch(
            name = "Team Highlight",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static boolean teamHighlight = false;

    @Color(
            name = "Team Highlight Color",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static OneColor teamHighlightColor = new OneColor(0, 128, 0);

    @Switch(
            name = "Block Useless Perks",
            description = "Block clicking on perks that aren't useful, can be bypassed by holding ctrl.",
            category = KUUDRA_RUN,
            subcategory = KUUDRA_RUN_MISC
    )
    public static boolean blockUselessPerks = false;

    // KUUDRA_END
    @Switch(
            name = "Auto Requeue",
            description = "Instantly requeue when the run ends.",
            category = KUUDRA_END,
            subcategory = KUUDRA_REQUEUE,
            size = 2
    )
    public static boolean autoRequeue = false;

    @Switch(
            name = "Profit Calculator",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_CALC,
            size = 2
    )
    public static boolean kuudraProfitCalculator = true;

    @Switch(
            name = "Show Valuable Hologram",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_CALC
    )
    public static boolean showValuableHolo = true;

    @Page(
            name = "Profit Calculator Options",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_CALC,
            location = PageLocation.BOTTOM
    )
    public static KuudraProfitCalculatorOptions kuudraProfitCalculatorOptions = new KuudraProfitCalculatorOptions();

    @Switch(
            name = "Profit Tracker",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_TRACKER,
            size = 2
    )
    public static boolean kuudraProfitTracker = true;

    @Switch(
            name = "Show Tracker Near Croesus",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_TRACKER,
            size = 2
    )
    public static boolean showKuudraProfitTrackerNearCroesus = true;

    @Switch(
            name = "Hide Tracker During Run",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_TRACKER,
            size = 2
    )
    public static boolean hideKuudraProfitTrackerDuringRun = false;

    @Switch(
            name = "Include Delay In Run Time",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_TRACKER
    )
    public static boolean kuudraProfitTrackerAddRunTimeDelay = true;

    @Number(
            name = "Delay To Add (s)",
            min = 0, max = 60,
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_TRACKER
    )
    public static int kuudraProfitTrackerTotalTimeToAdd = 15;

    @Page(
            name = "Profit Tracker Options",
            category = KUUDRA_END,
            subcategory = KUUDRA_PROFIT_TRACKER,
            location = PageLocation.BOTTOM
    )
    public static KuudraProfitTrackerOptions kuudraProfitTrackerOptions = new KuudraProfitTrackerOptions();

    // CHAT

    @Switch(
            name = "MVP++ Emojis",
            category = CHAT,
            description = "Allows sending MVP++ emojis without having MVP++.",
            size = 2
    )
    public static boolean emojis = false;

    @Switch(
            name = "KIC Chat",
            category = CHAT,
            subcategory = KIC_CHAT
    )
    public static boolean kicChat = true;

    @Switch(
            name = "KIC+ Chat (KIC+)",
            category = CHAT,
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
            category = CHAT,
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
            category = CHAT,
            subcategory = KIC_CHAT
    )
    public static int kicPlusChatColor = 15;

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

    // MISC

    @Switch(
            name = "Hunting Box Value",
            category = MISC,
            size = 2
    )
    public static boolean crimsonHuntingBoxValue = true;

    @Switch(
            name = "Announce Mana Drain",
            category = MISC,
            size = 2
    )
    public static boolean announceManaDrain = false;

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

    @Dropdown(
            name = "KIC Sharing GUI Style",
            options = {
                    "Default", "Prism", "PackHQ"
            },
            category = MISC,
            size = 2
    )
    public static int KICSharingGuiStyle = 1;

    @Switch(
            name = "Backbone On Private Island",
            category = MISC,
            subcategory = BONE
    )
    public static boolean backboneOnIsland = true;

    @Switch(
            name = "Show Front Bone Hit",
            category = MISC,
            subcategory = BONE
    )
    public static boolean backboneOnIslandFront = false;

    @Switch(
            name = "Wardrobe Keybinds",
            category = MISC,
            subcategory = WARDROBE,
            size = 2
    )
    public static boolean wardrobeKeybinds = false;

    @Switch(
            name = "Play Sound On Swap",
            category = MISC,
            subcategory = WARDROBE
    )
    public static boolean wardrobePlaySound = false;

    @Text(
            name = "Swap Sound",
            description = "Sound to play after a successful swap. You can search for sounds with \"/kicsounds\".",
            category = MISC,
            subcategory = WARDROBE
    )
    public static String wardrobeSound = "note.pling";

    @KeyBind(
            name = "Wardrobe Slot 1 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot1 = new OneKeyBind(UKeyboard.KEY_1);

    @KeyBind(
            name = "Wardrobe Slot 2 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot2 = new OneKeyBind(UKeyboard.KEY_2);

    @KeyBind(
            name = "Wardrobe Slot 3 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot3 = new OneKeyBind(UKeyboard.KEY_3);

    @KeyBind(
            name = "Wardrobe Slot 4 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot4 = new OneKeyBind(UKeyboard.KEY_4);

    @KeyBind(
            name = "Wardrobe Slot 5 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot5 = new OneKeyBind(UKeyboard.KEY_5);

    @KeyBind(
            name = "Wardrobe Slot 6 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot6 = new OneKeyBind(UKeyboard.KEY_6);

    @KeyBind(
            name = "Wardrobe Slot 7 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot7 = new OneKeyBind(UKeyboard.KEY_7);

    @KeyBind(
            name = "Wardrobe Slot 8 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot8 = new OneKeyBind(UKeyboard.KEY_8);

    @KeyBind(
            name = "Wardrobe Slot 9 Keybind",
            category = MISC,
            subcategory = WARDROBE
    )
    public static OneKeyBind wardrobeSlot9 = new OneKeyBind(UKeyboard.KEY_9);

    @Switch(
            name = "Track Empty Slots In Ender Chest Pages And Backpacks",
            category = MISC,
            subcategory = BPEC,
            size = 2
    )
    public static boolean trackEmptyECBP = false;

    @Switch(
            name = "Track Ender Chest Pages",
            category = MISC,
            subcategory = BPEC,
            size = 2
    )
    public static boolean trackEnderChestPages = false;

    @Switch(
            name = "Track Backpacks",
            category = MISC,
            subcategory = BPEC,
            size = 2
    )
    public static boolean trackBackpacks = true;

    @KeyBind(
            name = "Open EC or BP With Empty Slots Keybind",
            description = "Key to press when you want to open an ender chest page or backpack with empty slots.",
            size = 2,
            category = MISC,
            subcategory = BPEC
    )
    public static OneKeyBind openEmptyECBP = new OneKeyBind(UKeyboard.KEY_NONE);

    @Switch(
            name = "Ender Chest Page 1",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec1 = false;

    @Switch(
            name = "Ender Chest Page 2",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec2 = false;

    @Switch(
            name = "Ender Chest Page 3",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec3 = false;

    @Switch(
            name = "Ender Chest Page 4",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec4 = false;

    @Switch(
            name = "Ender Chest Page 5",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec5 = false;

    @Switch(
            name = "Ender Chest Page 6",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec6 = false;

    @Switch(
            name = "Ender Chest Page 7",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec7 = false;

    @Switch(
            name = "Ender Chest Page 8",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec8 = false;

    @Switch(
            name = "Ender Chest Page 9",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean ec9 = false;

    @Switch(
            name = "Backpack 1",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp1 = false;

    @Switch(
            name = "Backpack 2",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp2 = false;

    @Switch(
            name = "Backpack 3",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp3 = false;

    @Switch(
            name = "Backpack 4",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp4 = false;

    @Switch(
            name = "Backpack 5",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp5 = false;

    @Switch(
            name = "Backpack 6",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp6 = false;

    @Switch(
            name = "Backpack 7",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp7 = false;

    @Switch(
            name = "Backpack 8",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp8 = false;

    @Switch(
            name = "Backpack 9",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp9 = false;

    @Switch(
            name = "Backpack 10",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp10 = false;

    @Switch(
            name = "Backpack 11",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp11 = false;

    @Switch(
            name = "Backpack 12",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp12 = false;

    @Switch(
            name = "Backpack 13",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp13 = false;

    @Switch(
            name = "Backpack 14",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp14 = false;

    @Switch(
            name = "Backpack 15",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp15 = false;

    @Switch(
            name = "Backpack 16",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp16 = false;

    @Switch(
            name = "Backpack 17",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp17 = false;

    @Switch(
            name = "Backpack 18",
            category = MISC,
            subcategory = BPEC
    )
    public static boolean bp18 = false;

    @Switch(
            name = "Enable player sizing",
            category = MISC,
            subcategory = PLAYER_SIZING,
            description = "Main toggle for smoll people."
    )
    public static boolean playerSizeToggle = false;

    @Switch(
            name = "Ignore NPC's",
            category = MISC,
            subcategory = PLAYER_SIZING
    )
    public static boolean playerSizeIgnoreNPC = true;

    @Slider(
            name = "Player size",
            category = MISC,
            subcategory = PLAYER_SIZING,
            min = 0.1f, max = 2.0f
    )
    public static float playerSize = 0.6f;

    @Dropdown(
            name = "Player Sizing Method",
            category = MISC,
            subcategory = PLAYER_SIZING,
            options = {"Method 1", "Method 2"},
            size = 2
    )
    public static int playerSizingMethod = 0;

    // ADDONS

    @Info(
            text = "No addons detected. Place addon JARs in `.minecraft/config/kuudraiscool/addons` and restart your game.",
            type = InfoType.WARNING,
            size = 2,
            category = ADDONS,
            subcategory = ADDON_HELP
    )
    private transient static boolean noAddonsInfo;

    @Button(
            name = "Open Addons Directory",
            text = "Click me!",
            size = 2,
            category = ADDONS,
            subcategory = ADDON_HELP
    )
    Runnable openAddonsDirectory = () -> {
        try {
            if (!ADDON_DIR.exists()) ADDON_DIR.mkdirs();
            FileUtils.openFolder(ADDON_DIR);
            sendMessageToPlayer(KIC.KICPrefix + " §aOpened addons folder.");
        } catch (Exception e) {
            sendMessageToPlayer(KIC.KICPrefix + " §cFailed to open addons folder! Please open it manually.");
            KICLogger.error("[KIC] Failed to open add-ons directory: " + e.getMessage());
        }
    };

    @Button(
            name = "Reload Addons",
            text = "Click me!",
            size = 2,
            category = ADDONS,
            subcategory = ADDON_HELP
    )
    Runnable reloadAddons = AddonHelpers::reloadAddons;
}
