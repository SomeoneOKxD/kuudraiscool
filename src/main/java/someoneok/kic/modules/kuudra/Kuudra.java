package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraSplitsOptions;
import someoneok.kic.events.PacketEvent;
import someoneok.kic.models.kuudra.*;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.PlayerUtils;
import someoneok.kic.utils.RenderUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.OverlayUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.GeneralUtils.*;
import static someoneok.kic.utils.PlayerUtils.getPlayerEyePos;
import static someoneok.kic.utils.StringUtils.*;

public class Kuudra {
    private final DecimalFormat healthFormatter = new DecimalFormat("###,###");
    private final DecimalFormat percentFormatter = new DecimalFormat("##.##");
    private final Color spotColor = new Color(155, 0, 255);
    private static final double SUPPLY_ZOMBIE_DISTANCE = 3;
    private static final Pattern RECOVERED = Pattern.compile("^(?:\\[[^]]+] )?(\\w+) recovered one of Elle's supplies! \\((\\d)/6\\)$");
    private static final Pattern FRESH = Pattern.compile("^Party > (?:\\[[^]]+] )?(\\w+): FRESH.*");

    private static final EnumMap<SupplyPickUpSpot, Map<Vec3, KuudraSupplyStatus>> supplySpotWaypoints = new EnumMap<>(SupplyPickUpSpot.class);
    private static final Map<Vec3, Map.Entry<SupplyPickUpSpot, Map<Vec3, KuudraSupplyStatus>>> vecToEntryMap = new HashMap<>();
    private final List<Vec3> supplies = new ArrayList<>();
    private final List<AxisAlignedBB> hitboxes = new ArrayList<>();
    private final Map<Vec3, Color> supplyProgressMap = new HashMap<>();

    private final TreeMap<Integer, TimedEvent> supplyTimes = new TreeMap<>();
    private final List<TimedEvent> freshTimes = new ArrayList<>();
    private final Map<String, Long> freshCooldownMap = new HashMap<>();

    public static final String NO_EQUALS = "No Equals!";
    public static final String NO_SHOP = "No Shop!";
    public static final String NO_X_CANNON = "No X Cannon!";
    public static final String NO_SLASH = "No Slash!";
    public static final String NO_TRIANGLE = "No Triangle!";
    public static final String NO_X = "No X!";
    public static final String NO_SQUARE = "No Square!";

    public static int currentPhase = 0; // 1 = supplies, 2 = build, 3 = eaten, 4 = stun, 5 = dps, 6 = skip, 7 = kill, 8 = end
    private static Vec3 noPreSpot = null;
    public static SupplyPickUpSpot noPre = null;
    private static boolean modified = false;
    private EntityMagmaCube boss = null;
    private EntityArmorStand elle = null;
    private String bossHPMessage = null;
    private float bossHPScale = 0;
    private Vec3 eyePos = null;
    private long pearlCheckTicks = 0;

    private static long tickCount = 0;
    private static long packetCount = 0;
    public static long logicalTimeMs = 0;
    private boolean buildTimerStart = false;

    static {
        initSupplySpotWaypoints();
    }

    public static void initSupplySpotWaypoints() {
        supplySpotWaypoints.clear();
        for (SupplyPickUpSpot spot : SupplyPickUpSpot.values()) {
            if (spot != SupplyPickUpSpot.NONE) {
                supplySpotWaypoints.put(spot, new HashMap<>());
            }
        }

        putSupply(SupplyPickUpSpot.TRIANGLE, KuudraSupplySpot.SUPPLY1);
        putSupply(SupplyPickUpSpot.X, KuudraSupplySpot.SUPPLY2);
        putSupply(SupplyPickUpSpot.EQUALS, KuudraSupplySpot.SUPPLY3);
        putSupply(SupplyPickUpSpot.SLASH, KuudraSupplySpot.SUPPLY4);
        putSupply(SupplyPickUpSpot.SHOP, KuudraSupplySpot.SUPPLY5);
        putSupply(SupplyPickUpSpot.X_CANNON, KuudraSupplySpot.SUPPLY6);

        rebuildVecToEntryMap();
        modified = false;
    }

    private static void putSupply(SupplyPickUpSpot pickup, KuudraSupplySpot spot) {
        supplySpotWaypoints.get(pickup).put(spot.getLocation(), KuudraSupplyStatus.NOTHING);
    }

    private static void rebuildVecToEntryMap() {
        vecToEntryMap.clear();
        for (Map.Entry<SupplyPickUpSpot, Map<Vec3, KuudraSupplyStatus>> entry : supplySpotWaypoints.entrySet()) {
            for (Vec3 vec : entry.getValue().keySet()) {
                vecToEntryMap.put(vec, entry);
            }
        }
    }

    public static void updateSupplyStatus(Vec3 supply, KuudraSupplyStatus status) {
        Map.Entry<SupplyPickUpSpot, Map<Vec3, KuudraSupplyStatus>> entry = vecToEntryMap.get(supply);
        if (entry != null) {
            entry.getValue().put(supply, status);
            if (noPreSpot != null && vecEquals(noPreSpot, supply) && status != KuudraSupplyStatus.NOTHING) {
                noPreSpot = null;
            }
            modified = true;
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!LocationUtils.inKuudra) return;

        String raw = removeFormatting(event.message.getUnformattedText());
        long now = System.currentTimeMillis();
        long ticks = getTotalLagTimeTicks();

        switch (raw) {
            case "[NPC] Elle: Talk with me to begin!":
                KICLogger.info("Phase 0 (Ready)");
                currentPhase = 0;
                return;

            case "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!":
                KICLogger.info("Phase 1 (Supplies)");
                currentPhase = 1;
                KuudraPhase.SUPPLIES.start(now, ticks);
                KuudraPhase.OVERALL.start(now, ticks);
                return;

            case "[NPC] Elle: OMG! Great work collecting my supplies!":
                KICLogger.info("Phase 2 (Build)");
                currentPhase = 2;
                KuudraPhase.SUPPLIES.end(now, ticks);
                KuudraPhase.BUILD.start(now, ticks);
                return;

            case "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!":
                if (currentPhase != 5) {
                    KICLogger.info("Phase 3 (Eaten)");
                    currentPhase = 3;
                    KuudraPhase.BUILD.end(now, ticks);
                    KuudraPhase.EATEN.start(now, ticks);
                }
                return;

            case "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!":
                KICLogger.info("Phase 6 (Skip)");
                currentPhase = 6;
                KuudraPhase.DPS.end(now, ticks);
                KuudraPhase.SKIP.start(now, ticks);
                return;
        }

        if (raw.contains("has been eaten by Kuudra!") && currentPhase == 3 && !raw.contains("Elle")) {
            KICLogger.info("Phase 4 (Stun)");
            currentPhase = 4;
            if (!KuudraPhase.EATEN.hasStarted()) KuudraPhase.EATEN.start(now, ticks);
            if (!KuudraPhase.EATEN.hasEnded()) KuudraPhase.EATEN.end(now, ticks);
            KuudraPhase.STUN.start(now, ticks);
            return;
        }

        if (raw.contains("destroyed one of Kuudra's pods!") && currentPhase != 5) {
            KICLogger.info("Phase 5 (Dps)");
            currentPhase = 5;
            if (!KuudraPhase.EATEN.hasEnded()) KuudraPhase.EATEN.end(now, ticks);
            if (KuudraPhase.STUN.hasStarted() && !KuudraPhase.STUN.hasEnded()) {
                KuudraPhase.STUN.end(now, ticks);
            }
            KuudraPhase.DPS.start(now, ticks);
            return;
        }

        if (raw.contains(NO_EQUALS)) {
            assignIfSpotEmpty(SupplyPickUpSpot.EQUALS);
            return;
        } else if (raw.contains(NO_SHOP)) {
            assignIfSpotEmpty(SupplyPickUpSpot.SHOP);
            return;
        } else if (raw.contains(NO_X_CANNON)) {
            assignIfSpotEmpty(SupplyPickUpSpot.X_CANNON);
            return;
        } else if (raw.contains(NO_SLASH)) {
            assignIfSpotEmpty(SupplyPickUpSpot.SLASH);
            return;
        } else if (raw.contains(NO_TRIANGLE)) {
            assignIfSpotEmpty(SupplyPickUpSpot.TRIANGLE);
            return;
        } else if (raw.contains(NO_X)) {
            assignIfSpotEmpty(SupplyPickUpSpot.X);
            return;
        }

        if (currentPhase == 1 && raw.contains("recovered")) {
            Matcher recovered = RECOVERED.matcher(raw);
            if (recovered.matches()) {
                String player = getRankColor(raw) + recovered.group(1);
                int supplyNum = Integer.parseInt(recovered.group(2));

                if (supplyTimes.containsKey(supplyNum)) return;

                if (supplyNum == 6 && !buildTimerStart) buildTimerStart = true;
                supplyTimes.put(supplyNum, new TimedEvent(player, KuudraPhase.SUPPLIES.getTime(now)));
                return;
            }
        }

        if (currentPhase == 2 && raw.contains("FRESH")) {
            Matcher fresh = FRESH.matcher(raw);
            if (fresh.matches()) {
                String player = getRankColor(raw) + fresh.group(1);

                Long lastCallTime = freshCooldownMap.get(player);
                long time = KuudraPhase.BUILD.getTime(now);
                if (lastCallTime == null || now - lastCallTime > 5000) {
                    freshTimes.add(new TimedEvent(player, time));
                    freshCooldownMap.put(player, time);
                }
                return;
            }
        }

        String trimmedUpper = raw.trim().toUpperCase().replaceAll(" ", "");
        if (trimmedUpper.startsWith("KUUDRADOWN") || trimmedUpper.startsWith("DEFEAT")) {
            currentPhase = 8;
            KuudraPhase.KILL.end(now, ticks);
            KuudraPhase.OVERALL.end(now, ticks);
            KuudraPhase.endMissedPhases();
            boolean failed = trimmedUpper.contains("DEFEAT");
            long runTime = KuudraPhase.OVERALL.getTime(System.currentTimeMillis());

            if (runTime == 0) {
                KuudraProfitTracker.onRunEnded(failed);
            } else {
                KuudraProfitTracker.onRunEnded(runTime, failed);
            }

            KICLogger.info("Phase 8 (End), failed? " + failed);

            if (KICConfig.showTotalServerLag) {
                Multithreading.schedule(() -> sendMessageToPlayer(String.format("%s §cServer lagged for §f%.2fs §7(§f%d ticks§7)", KIC.KICPrefix, getTotalLagTimeS(), getTotalLagTimeTicks())), 500, TimeUnit.MILLISECONDS);
            }

            if (KuudraSplitsOptions.showDetailedOverview) KuudraSplits.sendDetailedSplits(now, freshTimes);
        }
    }

    private void assignIfSpotEmpty(SupplyPickUpSpot spot) {
        noPre = spot;
        Map<Vec3, KuudraSupplyStatus> map = supplySpotWaypoints.get(spot);
        if (map != null) {
            for (Map.Entry<Vec3, KuudraSupplyStatus> entry : map.entrySet()) {
                if (entry.getValue() == KuudraSupplyStatus.NOTHING) {
                    noPreSpot = entry.getKey();
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (modified) initSupplySpotWaypoints();
        currentPhase = 0;
        pearlCheckTicks = 0;
        boss = null;
        elle = null;
        noPreSpot = null;
        noPre = null;
        bossHPMessage = null;
        bossHPScale = 0;
        supplies.clear();
        hitboxes.clear();
        supplyProgressMap.clear();
        eyePos = null;
        KuudraPhase.reset();
        resetFormatters();
        tickCount = 0;
        packetCount = 0;
        logicalTimeMs = 0;
        supplyTimes.clear();
        freshTimes.clear();
        freshCooldownMap.clear();
        KuudraSplits.reset();
        buildTimerStart = false;
    }

    private void resetFormatters() {
        // Reset internal buffers
        healthFormatter.applyPattern("###,###");
        percentFormatter.applyPattern("##.##");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null || !LocationUtils.inKuudra || currentPhase < 1 || currentPhase == 8) return;

        tickCount++;
        eyePos = getPlayerEyePos();
        supplies.clear();
        hitboxes.clear();
        supplyProgressMap.clear();

        long now = System.currentTimeMillis();
        long ticks = getTotalLagTimeTicks();
        if (currentPhase == 6 && mc.thePlayer.posY < 10) {
            KICLogger.info("Phase 7 (Kill)");
            currentPhase = 7;
            KuudraPhase.SKIP.end(now, ticks);
            KuudraPhase.KILL.start(now, ticks);
        }

        if (KICConfig.autoRefillPearls) {
            if (++pearlCheckTicks >= KICConfig.autoRefillPearlsTicks) {
                pearlCheckTicks = 0;
                refillPearls();
            }
        }

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand || entity instanceof EntityMagmaCube || entity instanceof EntityGiantZombie)) continue;

            if (entity instanceof EntityArmorStand) {
                String name = removeFormatting(entity.getName());
                if (isNullOrEmpty(name)) continue;

                if ("elle".equalsIgnoreCase(name)) {
                    elle = (EntityArmorStand) entity;
                } else {
                    getClosestSupply(entity.getPositionVector()).ifPresent(supply -> {
                        KuudraSupplyStatus status = getStatusFromName(name);
                        if (status != null) {
                            updateSupplyStatus(supply, status);

                            if (status == KuudraSupplyStatus.INPROGRESS) {
                                try {
                                    if (name.startsWith("PROGRESS:")) {
                                        String percentStr = name.substring("PROGRESS:".length()).replace("%", "").trim();
                                        supplyProgressMap.put(supply, getProgressColor(Float.parseFloat(percentStr)));
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    });
                }
            } else if (entity instanceof EntityMagmaCube) {
                EntityMagmaCube cube = (EntityMagmaCube) entity;
                if (cube.width > 14 && cube.getHealth() <= 100000) boss = cube;
            } else if (entity instanceof EntityGiantZombie) {
                if (entity.posY < 67) {
                    float yaw = entity.rotationYaw;
                    double x = entity.posX + 0.5 + (3.7 * Math.cos(Math.toRadians(yaw + 130)));
                    double z = entity.posZ + 0.5 + (3.7 * Math.sin(Math.toRadians(yaw + 130)));
                    Vec3 supplyVec = new Vec3(x, 75, z);
                    supplies.add(supplyVec);
                }
            }
        }

        if (KICConfig.supplyBox && (currentPhase == 1 || currentPhase == 3)) {
            for (Vec3 supply : supplies) {
                AxisAlignedBB searchArea = new AxisAlignedBB(
                        supply.xCoord - SUPPLY_ZOMBIE_DISTANCE, supply.yCoord - SUPPLY_ZOMBIE_DISTANCE, supply.zCoord - SUPPLY_ZOMBIE_DISTANCE,
                        supply.xCoord + SUPPLY_ZOMBIE_DISTANCE, supply.yCoord + SUPPLY_ZOMBIE_DISTANCE, supply.zCoord + SUPPLY_ZOMBIE_DISTANCE
                );

                List<AxisAlignedBB> zombieBoxes = mc.theWorld.getEntitiesWithinAABB(EntityZombie.class, searchArea).stream()
                        .filter(Entity::isInvisible)
                        .map(EntityZombie::getEntityBoundingBox)
                        .filter(box -> box != null &&
                                box.maxX - box.minX < 10 &&
                                box.maxY - box.minY < 10 &&
                                box.maxZ - box.minZ < 10 &&
                                !Double.isNaN(box.minX) && !Double.isNaN(box.maxX) &&
                                !Double.isInfinite(box.minX) && !Double.isInfinite(box.maxX))
                        .filter(box -> {
                            double cx = (box.minX + box.maxX) / 2.0;
                            double cy = (box.minY + box.maxY) / 2.0;
                            double cz = (box.minZ + box.maxZ) / 2.0;
                            return new Vec3(cx, cy, cz).distanceTo(supply) <= SUPPLY_ZOMBIE_DISTANCE;
                        })
                        .collect(Collectors.toList());

                if (!zombieBoxes.isEmpty()) {
                    AxisAlignedBB hitbox = getEnclosingBox(zombieBoxes);
                    if (hitbox != null) hitboxes.add(hitbox);
                }
            }
        }

        if (KICConfig.showKuudraBossBar && boss != null) {
            updateBossBarValues();
        } else {
            bossHPMessage = null;
            bossHPScale = 0;
        }

        if (KICConfig.kuudraSplits) {
            KuudraSplits.updateKuudraSplits(now);
        }

        if (KICConfig.kuudraSupplyTimes && currentPhase == 1) {
            KuudraSplits.updateSupplyTimes(supplyTimes);
        }

        if (KICConfig.kuudraFreshTimes && currentPhase == 2) {
            KuudraSplits.updateFreshTimes(freshTimes);
        }
    }

    private AxisAlignedBB getEnclosingBox(List<AxisAlignedBB> boxes) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (AxisAlignedBB box : boxes) {
            if (box == null ||
                    box.maxX - box.minX > 10 ||
                    box.maxY - box.minY > 10 ||
                    box.maxZ - box.minZ > 10 ||
                    Double.isNaN(box.minX) || Double.isNaN(box.maxX) ||
                    Double.isInfinite(box.minX) || Double.isInfinite(box.maxX)) {
                continue;
            }

            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }

        if (minX == Double.POSITIVE_INFINITY) return null;

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void renderBossHealthTag() {
        float health = boss.getHealth();
        String tag = "§c" + healthFormatter.format(health) + "/100,000";
        if (LocationUtils.kuudraTier == 5) {
            tag = currentPhase >= 7
                    ? "§e" + parseToShorthandNumber(health * 9600) + "/240M"
                    : "§c" + healthFormatter.format((health - 25000) / 3 * 4) + "/100,000";
        }
        RenderUtils.renderNameTag(tag, boss.posX, boss.posY + boss.height / 2, boss.posZ, 4.0f, true);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !LocationUtils.inKuudra || currentPhase == 8) return;

        if (boss != null) {
            if (KICConfig.showKuudraBossBar) {
                updateBossBar();
            }

            if (KICConfig.showKuudraHealth) {
                renderBossHealthTag();
            }

            if (KICConfig.showKuudraOutline) {
                RenderUtils.drawEntityBox(boss, Color.GREEN, 3, event.partialTicks);
            }
        }

        if (KICConfig.elleESP && elle != null && currentPhase < 3) {
            RenderUtils.drawEntityBox(elle, Color.MAGENTA, 5, event.partialTicks);
        }

        if (KICConfig.supplyWaypoints && (currentPhase == 1 || currentPhase == 3)) {
            supplies.forEach(supply -> RenderUtils.drawBeaconBeam(
                    supply,
                    KICConfig.supplyColor.toJavaColor(),
                    150,
                    true,
                    event.partialTicks
            ));
        }

        if (KICConfig.supplyBox && (currentPhase == 1 || currentPhase == 3)) {
            hitboxes.forEach(hitbox -> RenderUtils.drawBox(
                    hitbox,
                    KICConfig.supplyColor.toJavaColor(),
                    false,
                    event.partialTicks
            ));
        }

        if (KICConfig.showSpots && currentPhase == 1 && eyePos != null) {
            Arrays.stream(SupplyPickUpSpot.values())
                    .filter(entry -> entry != SupplyPickUpSpot.NONE)
                    .forEach(supplyPickUpSpot -> {
                        Vec3 location = supplyPickUpSpot.getLocation();
                        if (KICConfig.hideSpotIfClose && eyePos.distanceTo(location) < 10) return;
                        if (KICConfig.showSpotNames) {
                            RenderUtils.renderNameTag(
                                    EnumChatFormatting.DARK_PURPLE + supplyPickUpSpot.getDisplayText(),
                                    location.xCoord,
                                    location.yCoord + 2,
                                    location.zCoord,
                                    1,
                                    false
                            );
                        }
                        RenderUtils.drawPixelBox(
                                location,
                                spotColor,
                                1,
                                true,
                                event.partialTicks
                        );
                    });
        }

        if (KICConfig.supplyWaypointsProgress && currentPhase == 2) {
            for (Map.Entry<Vec3, Color> entry : supplyProgressMap.entrySet()) {
                RenderUtils.drawBeaconBeam(
                        entry.getKey(),
                        entry.getValue(),
                        150,
                        false,
                        event.partialTicks
                );
            }
        }
    }

    private void updateBossBarValues() {
        float health = boss.getHealth();
        String hpColor = "§c";
        float scale;

        if (LocationUtils.kuudraTier == 5) {
            if (currentPhase >= 7) {
                scale = Math.max(0f, health * 9600f) / 240_000_000f;
                hpColor = "§e";
            } else {
                scale = Math.max(0f, health - 25000f) / 75_000f;
            }
        } else {
            scale = Math.max(0f, health) / 100_000f;
        }

        bossHPScale = scale;
        bossHPMessage = hpColor + percentFormatter.format(bossHPScale * 100f) + "%";
    }

    private void updateBossBar() {
        BossStatus.healthScale = bossHPScale;
        BossStatus.statusBarTime = 100;
        BossStatus.bossName = "§e§l« §c§lKuudra §e§l»";
        BossStatus.hasColorModifier = true;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if (!KICConfig.showKuudraBossBar || !LocationUtils.inKuudra ||
                event.type != RenderGameOverlayEvent.ElementType.TEXT || currentPhase <= 0 || currentPhase == 8 || isNullOrEmpty(bossHPMessage)) return;

        ScaledResolution res = new ScaledResolution(mc);
        int screenWidth = res.getScaledWidth();
        int gameWidth = mc.displayWidth / res.getScaleFactor();
        int offsetX = (screenWidth - gameWidth) / 2;
        int x = offsetX + gameWidth / 2;
        int y = 10;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 400);
        OverlayUtils.drawString(0, 0, bossHPMessage, OverlayUtils.TextStyle.Shadow, OverlayUtils.Alignment.Center);
        GlStateManager.popMatrix();
    }

    private void refillPearls() {
        ItemStack pearlStack = PlayerUtils.getHotbarItemStack(Items.ender_pearl);
        if (pearlStack == null || pearlStack.stackSize >= 16) return;

        int pearlsNeeded = 16 - pearlStack.stackSize;
        sendCommand("/gfs ender_pearl " + pearlsNeeded);
    }

    public static KuudraSupplyStatus getStatusFromName(String name) {
        if (name.contains("BRING SUPPLY CHEST HERE")) return KuudraSupplyStatus.NOTHING;
        if (name.contains("SUPPLIES RECEIVED")) return KuudraSupplyStatus.RECEIVED;
        if (name.contains("PROGRESS: ")) {
            return name.contains("COMPLETE") ? KuudraSupplyStatus.COMPLETED : KuudraSupplyStatus.INPROGRESS;
        }
        return null;
    }

    private Color getProgressColor(float percent) {
        percent = Math.max(0, Math.min(100, percent));
        float ratio = (float) Math.pow(percent / 100f, 1.5);

        int red = (int) ((1 - ratio) * 255);
        int green = (int) (ratio * 255);

        return new Color(red, green, 0);
    }

    public static List<Vec3> getAllUncompletedSpots() {
        List<Vec3> uncompleted = new ArrayList<>();
        for (Map<Vec3, KuudraSupplyStatus> map : supplySpotWaypoints.values()) {
            for (Map.Entry<Vec3, KuudraSupplyStatus> entry : map.entrySet()) {
                if (entry.getValue() == KuudraSupplyStatus.NOTHING) {
                    uncompleted.add(entry.getKey());
                }
            }
        }
        return uncompleted;
    }

    public static Optional<Vec3> getClosestSupply(Vec3 vector, KuudraSupplyStatus status) {
        double minDistSq = Double.MAX_VALUE;
        Vec3 closest = null;

        for (Map<Vec3, KuudraSupplyStatus> map : supplySpotWaypoints.values()) {
            for (Map.Entry<Vec3, KuudraSupplyStatus> entry : map.entrySet()) {
                if (entry.getValue() != status) continue;

                double distSq = vector.squareDistanceTo(entry.getKey());
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    closest = entry.getKey();
                }
            }
        }

        return Optional.ofNullable(closest);
    }

    public static Optional<Vec3> getClosestSupply(Vec3 vector) {
        double minDistSq = Double.MAX_VALUE;
        Vec3 closest = null;

        for (Map<Vec3, KuudraSupplyStatus> map : supplySpotWaypoints.values()) {
            for (Vec3 vec : map.keySet()) {
                double distSq = vector.squareDistanceTo(vec);
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    closest = vec;
                }
            }
        }

        return Optional.ofNullable(closest);
    }

    public static Vec3 getMySupplySpot(Vec3 vector) {
        SupplyPickUpSpot closestSpot = SupplyPickUpSpot.getClosestSpot(vector).orElse(SupplyPickUpSpot.NONE);

        if (closestSpot == SupplyPickUpSpot.SQUARE) {
            if (noPreSpot != null) return noPreSpot;
        }

        if (closestSpot != SupplyPickUpSpot.NONE) {
            Map<Vec3, KuudraSupplyStatus> waypoints = supplySpotWaypoints.get(closestSpot);
            if (waypoints != null && waypoints.size() == 1) {
                Map.Entry<Vec3, KuudraSupplyStatus> entry = waypoints.entrySet().iterator().next();
                if (entry.getValue() == KuudraSupplyStatus.NOTHING) {
                    return entry.getKey();
                }
            }
        }

        return getClosestSupply(vector, KuudraSupplyStatus.NOTHING).orElse(noPreSpot);
    }

    public static long getTotalLagTimeMs() {
        return Math.max(tickCount - packetCount, 0) * 50;
    }

    public static double getTotalLagTimeS() {
        return getTotalLagTimeMs() / 1000.0;
    }

    public static long getTotalLagTimeTicks() {
        return Math.max(tickCount - packetCount, 0);
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Received event) {
        if (mc.thePlayer == null || mc.theWorld == null || !LocationUtils.inKuudra || currentPhase < 1 || currentPhase == 8) return;

        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            packetCount++;
            logicalTimeMs += 50;
        }
    }
}
