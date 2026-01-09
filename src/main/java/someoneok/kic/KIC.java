package someoneok.kic;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import someoneok.kic.addons.AddonHelpers;
import someoneok.kic.addons.AddonRegistry;
import someoneok.kic.api.AddonEventBridge;
import someoneok.kic.commands.*;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.models.Island;
import someoneok.kic.models.data.UserData;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.overlay.OverlayExamples;
import someoneok.kic.modules.crimson.HuntingBoxValue;
import someoneok.kic.modules.kuudra.*;
import someoneok.kic.modules.misc.*;
import someoneok.kic.modules.player.PlayerSize;
import someoneok.kic.utils.*;
import someoneok.kic.utils.data.DataHandler;
import someoneok.kic.utils.data.DataManager;
import someoneok.kic.utils.dev.GuiLogAppender;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.dev.LogConsole;
import someoneok.kic.utils.kuudra.DoublePearlRegistry;
import someoneok.kic.utils.kuudra.KuudraUtils;
import someoneok.kic.utils.kuudra.TrajectorySolver;
import someoneok.kic.utils.mouse.LockMouseLook;
import someoneok.kic.utils.overlay.*;
import someoneok.kic.utils.sound.KICSoundReloadListener;
import someoneok.kic.utils.ws.KICWS;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.util.*;
import java.util.function.Supplier;

import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phaseOrdinal;

@Mod(modid = KIC.MODID, version = KIC.VERSION, useMetadata=true)
public class KIC {
    public static final String MODID = "@ID@";
    public static final String VERSION = "@VER@";

    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final String KICPrefix = "§7[§a§lKIC§r§7]§r";
    public static final String KICPlusPrefix = "§7[§a§lKIC§r§3+§r§7]§r";
    public static final Gson GSON = new GsonBuilder().create();
    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Random RNG = new Random();

    public static X509TrustManager CUSTOM_TRUST_MANAGER;
    public static SSLContext CUSTOM_SSL_CONTEXT;
    public static String discordUrl = "https://discord.gg/gsz58gazAK";
    public static KICConfig config;
    public static UserData userData;

    private final ArrayList<Object> MODULES = new ArrayList<>();
    private final ArrayList<ICommand> COMMANDS = new ArrayList<>();

    public KIC() {
        Collections.addAll(MODULES,
                new KICWS(),
                new LocationUtils(),
                new ServerTickUtils(),
                new Updater(),
                new TitleUtils(),
                new PlayerSize(),
                new KuudraUserInfo(),
                new PartyUtils(),
                new OverlayManager(),
                new KuudraProfitCalculator(),
                new ChatCommands(),
                new ChatHandler(),
                new Pearls(),
                new KuudraUtils(),
                new Kuudra(),
                new KuudraPhaseTracker(),
                new KuudraBoss(),
                new Notifications(),
                new KuudraPfGuiInfo(),
                new ArmorHud(),
                new TrackEmptySlots(),
                new HuntingBoxValue(),
                new PlayerUtils(),
                new ManaDrain(),
                new AutoRequeue(),
                new LockMouseLook(),
                new Wardrobe(),
                new TeamHighlight(),
                new Elle(),
                new TapTracker(),
                new NoPre(),
                new BuildHelper(),
                new RendDamage(),
                new ServerTickScheduler(),
                new InventoryTracker(),
                new BoneTracker(),
                new Vesuvius(),
                new BlockUselessPerks(),
                new HollowHelper()
        );

        Collections.addAll(COMMANDS,
                new KICCommand(),
                new TesterCommand(),
                new ChatCommand(),
                new ChatPlusCommand(),
                new KuudraCommand(),
                new LFCommand(),
                new KICAddonsCommand(),
                new KICSoundsCommand(),
                new StartKuudraCommand("t1", "KUUDRA_NORMAL"),
                new StartKuudraCommand("t2", "KUUDRA_HOT"),
                new StartKuudraCommand("t3", "KUUDRA_BURNING"),
                new StartKuudraCommand("t4", "KUUDRA_FIERY"),
                new StartKuudraCommand("t5", "KUUDRA_INFERNAL"),
                new DoublePearlCommand()
        );
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GuiLogAppender.init();
        LogConsole.init();

        Rat.sendToServer();
        config = new KICConfig();
        new HypixelEventApi();

        CUSTOM_TRUST_MANAGER = NetworkUtils.getTrustManager();
        CUSTOM_SSL_CONTEXT = NetworkUtils.getCustomSSLContext(CUSTOM_TRUST_MANAGER);

        KICWS.setupWS();
        Pearls.updateSizes();
        Pearls.updatePearlDelays();
        Pearls.updateTimerLocation();
        TrajectorySolver.updateDistances();

        ForgeCommandUtils.registerCommands(COMMANDS);
        initOverlays();
        registerModule(this);
        MODULES.forEach(this::registerModule);
        registerModule(new AddonEventBridge());
        registerModule(new ButtonManager()); // Needs to be here cus GUI stuff

        AddonHelpers.initAddons(false);
        DoublePearlRegistry.load();

        ButtonManager.updateCheckboxAlignment();
        KuudraSplits.resetKuudraSplitsOverlay();

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            IResourceManager manager = mc.getResourceManager();
            if (manager instanceof IReloadableResourceManager) {
                ((IReloadableResourceManager) manager)
                        .registerReloadListener(new KICSoundReloadListener());
            }
        }

        userData = DataManager.getUserData();

        Multithreading.runAsync(() -> ApiUtils.verifyApiKey(false));
        mc.getFramebuffer().enableStencil();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Updater.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            KICLogger.info("KIC-Cleanup-ShutdownHook");
            try { DataHandler.saveData(); } catch (Throwable ignored) {}
            try { KICWS.close(); } catch (Throwable ignored) {}
            try { AddonRegistry.shutdownAll(); } catch (Throwable ignored) {}
        }, "KIC-Cleanup-ShutdownHook"));
    }

    private void registerModule(Object obj) {
        MinecraftForge.EVENT_BUS.register(obj);
        KICEventBus.BUS.register(obj);
    }

    private void initOverlays() {
        Set<Island> allIslands = Collections.singleton(Island.ALL);
        Set<Island> KuudraIsland = Collections.singleton(Island.KUUDRA);
        Set<Island> pfStatsIslands = Collections.singleton(Island.CRIMSON_ISLE);
        Set<Island> profitIslands = new HashSet<>(Arrays.asList(
                Island.KUUDRA, Island.CRIMSON_ISLE, Island.DUNGEON_HUB
        ));

        Supplier<Boolean> profitCondition = () ->
                "Forgotten Skull".equals(LocationUtils.subArea)
                        || "Croesus".equals(LocationUtils.subArea)
                        || LocationUtils.currentIsland == Island.KUUDRA;

        Supplier<Boolean> trackerCondition = () -> {
            if (!profitCondition.get()) return false;
            if ("Croesus".equals(LocationUtils.subArea)) return KICConfig.showKuudraProfitTrackerNearCroesus;
            if (KICConfig.hideKuudraProfitTrackerDuringRun) {
                KuudraPhase current = phase();
                return current == KuudraPhase.NONE || current == KuudraPhase.END;
            }
            return true;
        };

        Supplier<Boolean> forgottenSkullCondition = () ->
                "Forgotten Skull".equals(LocationUtils.subArea);

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.kuudraProfitCalculator,
                "ProfitCalculator",
                "Profit Calculator",
                profitIslands,
                profitCondition,
                OverlayType.INGUI,
                OverlayExamples.PROFIT_CALCULATOR));

        OverlayManager.addOverlay(new DualOverlay(
                () -> KICConfig.kuudraProfitTracker && KICConfig.kuudraProfitCalculator,
                "ProfitTracker",
                "Profit Tracker",
                profitIslands,
                trackerCondition,
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
                () -> phaseOrdinal() > KuudraPhase.SUPPLIES.ordinal(),
                OverlayType.NORMAL,
                OverlayExamples.KUUDRA_FRESH_TIMES));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.buildTimer,
                "BuildTimer",
                "Build Timer",
                KuudraIsland,
                () -> (phaseOrdinal() <= KuudraPhase.BUILD.ordinal() && BuildHelper.isStarted()),
                OverlayType.NORMAL,
                OverlayExamples.BUILD_TIMER));

        OverlayManager.addOverlay(new MovableOverlay(
                () -> KICConfig.moveableCratePickup,
                "CratePickup",
                "Crate Pickup",
                KuudraIsland,
                () -> (phase() == KuudraPhase.SUPPLIES || phase() == KuudraPhase.EATEN),
                OverlayType.NORMAL,
                OverlayExamples.CRATE_PICKUP));

        DataHandler.loadOverlays();
        DataHandler.loadData();

        KuudraProfitTracker.updateTracker();
    }
}
