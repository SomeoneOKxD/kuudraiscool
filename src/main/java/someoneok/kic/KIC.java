package someoneok.kic;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import someoneok.kic.commands.FuckOtherMods;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.Island;
import someoneok.kic.models.UserData;
import someoneok.kic.models.overlay.OverlayExamples;
import someoneok.kic.modules.crimson.HuntingBoxValue;
import someoneok.kic.modules.dev.Dev;
import someoneok.kic.modules.kuudra.*;
import someoneok.kic.modules.misc.*;
import someoneok.kic.modules.player.PlayerSize;
import someoneok.kic.utils.*;
import someoneok.kic.utils.dev.GuiLogAppender;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.*;
import someoneok.kic.utils.ws.KICWS;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.util.*;
import java.util.function.Supplier;

@Mod(modid = KIC.MODID, version = KIC.VERSION, useMetadata=true)
public class KIC {
    public static final String MODID = "@ID@";
    public static final String VERSION = "@VER@";

    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final String KICPrefix = "§7[§a§lKIC§r§7]§r";
    public static final String KICPlusPrefix = "§7[§a§lKIC§r§3+§r§7]§r";
    public static final String KICDataPrefix = "§7[§a§lKIC-DATA§r§7]§r";
    public static final Gson GSON = new GsonBuilder().create();
    public static final Random RNG = new Random();
    public static final List<String> ATTRIBUTES = Arrays.asList("arachno", "attack_speed", "blazing", "combo", "elite",
            "ender", "ignition", "life_recovery", "mana_steal", "midas_touch", "undead", "warrior", "deadeye",
            "arachno_resistance", "blazing_resistance", "breeze", "dominance", "ender_resistance", "experience",
            "fortitude", "life_regeneration", "lifeline", "magic_find", "mana_pool", "mana_regeneration", "vitality",
            "speed", "undead_resistance", "veteran", "blazing_fortune", "fishing_experience", "infection", "double_hook",
            "fisherman", "fishing_speed", "hunter", "trophy_hunter", "mending");

    public static X509TrustManager CUSTOM_TRUST_MANAGER;
    public static SSLContext CUSTOM_SSL_CONTEXT;
    public static String discordUrl = "https://discord.gg/gsz58gazAK";
    public static KICConfig config;
    public static UserData userData;

    private final ArrayList<Object> modules = new ArrayList<>();

    public KIC() {
        Collections.addAll(modules,
                new KICWS(),
                new LocationUtils(),
                new Updater(),
                new TitleUtils(),
                new PlayerSize(),
                new KuudraUserInfo(),
                new PartyUtils(),
                new OverlayManager(),
                new KuudraProfitCalculator(),
                new ChatCommands(),
                new ChatHandler(),
                new Waypoints(),
                new Kuudra(),
                new Notifications(),
                new KuudraPfGuiInfo(),
                new ArmorHud(),
                new TrackEmptySlots(),
                new HuntingBoxValue(),
                new Dev(),
                new Hologram()
        );
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GuiLogAppender.init();

        Rat.sendToServer();
        config = new KICConfig();
        new HypixelEventApi();

        CUSTOM_TRUST_MANAGER = NetworkUtils.getTrustManager();
        CUSTOM_SSL_CONTEXT = NetworkUtils.getCustomSSLContext(CUSTOM_TRUST_MANAGER);

        KICWS.setupWS();
        Waypoints.updateSizes();
        Waypoints.updatePearlDelays();
        Waypoints.updateTimerLocation();
        TrajectorySolver.updateDistances();

        FuckOtherMods.registerCommands();
        initOverlays();
        registerModule(this);
        modules.forEach(this::registerModule);
        registerModule(new ButtonManager()); // Needs to be here cus GUI stuff

        ButtonManager.updateCheckboxAlignment();
        KuudraSplits.resetKuudraSplitsOverlay();

        userData = OverlayDataManager.getUserData();

        Multithreading.runAsync(() -> ApiUtils.verifyApiKey(false));
        mc.getFramebuffer().enableStencil();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Updater.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            KICLogger.info("KIC-Cleanup-ShutdownHook");
            KICWS.close();
        }, "KIC-Cleanup-ShutdownHook"));
    }

    private void registerModule(Object obj) {
        MinecraftForge.EVENT_BUS.register(obj);
        EventManager.INSTANCE.register(obj);
    }

    private void initOverlays() {
        Set<Island> allIslands = Collections.singleton(Island.ALL);
        Set<Island> KuudraIsland = Collections.singleton(Island.KUUDRA);
        Set<Island> profitTrackerIslands = new HashSet<>(Arrays.asList(Island.KUUDRA, Island.CRIMSON_ISLE));
        Set<Island> pfStatsIslands = new HashSet<>(Collections.singletonList(Island.CRIMSON_ISLE));

        Supplier<Boolean> forgottenSkullCondition = () ->
                LocationUtils.currentIsland != Island.CRIMSON_ISLE || "Forgotten Skull".equals(LocationUtils.subArea);

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.kuudraProfitCalculator,
                "ProfitCalculator",
                "Profit Calculator",
                KuudraIsland,
                () -> true,
                OverlayType.INGUI,
                OverlayExamples.PROFIT_CALCULATOR));

        OverlayManager.addOverlay(new DualOverlay(
                () -> KICConfig.kuudraProfitTracker && KICConfig.kuudraProfitCalculator,
                "ProfitTracker",
                "Profit Tracker",
                profitTrackerIslands,
                forgottenSkullCondition,
                ""));

        OverlayManager.addOverlay(new InteractiveOverlay(
                () -> KICConfig.crimsonHuntingBoxValue,
                "HuntingBoxValue",
                "Hunting Box Value",
                allIslands,
                () -> true,
                OverlayType.INGUI,
                OverlayExamples.HUNTING_BOX_VALUE));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.partyFinderGuiStats,
                "PFStatsPlayer1",
                "PF Stats Player 1",
                pfStatsIslands,
                forgottenSkullCondition,
                OverlayType.INGUI,
                OverlayExamples.EXAMPLE_PLAYER_1));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.partyFinderGuiStats,
                "PFStatsPlayer2",
                "PF Stats Player 2",
                pfStatsIslands,
                forgottenSkullCondition,
                OverlayType.INGUI,
                OverlayExamples.EXAMPLE_PLAYER_2));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.partyFinderGuiStats,
                "PFStatsPlayer3",
                "PF Stats Player 3",
                pfStatsIslands,
                forgottenSkullCondition,
                OverlayType.INGUI,
                OverlayExamples.EXAMPLE_PLAYER_3));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.kuudraSplits,
                "KuudraSplits",
                "Kuudra Splits",
                KuudraIsland,
                () -> true,
                OverlayType.NORMAL,
                ""));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.kuudraNotificationsMoveable,
                "Notifications",
                "Notifications",
                KuudraIsland,
                () -> true,
                OverlayType.NORMAL,
                OverlayExamples.NOTIFICATIONS));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.kuudraSupplyTimes,
                "SupplyTimes",
                "Supply Times",
                KuudraIsland,
                () -> true,
                OverlayType.NORMAL,
                OverlayExamples.KUUDRA_SUPPLY_TIMES));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.kuudraFreshTimes,
                "FreshTimes",
                "Fresh Times",
                KuudraIsland,
                () -> Kuudra.currentPhase > 1,
                OverlayType.NORMAL,
                OverlayExamples.KUUDRA_FRESH_TIMES));

        OverlayDataHandler.loadOverlays();
        OverlayDataHandler.loadOverlaysData();

        KuudraProfitTracker.updateTracker();
    }
}
