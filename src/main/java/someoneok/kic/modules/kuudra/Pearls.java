package someoneok.kic.modules.kuudra;

import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.events.ThrowPearlEvent;
import someoneok.kic.events.TitleEvent;
import someoneok.kic.models.kuudra.*;
import someoneok.kic.models.kuudra.pearls.DoublePearl;
import someoneok.kic.models.kuudra.pearls.PearlRenderData;
import someoneok.kic.models.kuudra.pearls.PearlSolution;
import someoneok.kic.models.misc.TitleType;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.RenderUtils;
import someoneok.kic.utils.ServerTickUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.kuudra.DoublePearlRegistry;
import someoneok.kic.utils.kuudra.KuudraUtils;
import someoneok.kic.utils.overlay.OverlayManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.utils.GeneralUtils.round2;
import static someoneok.kic.utils.GeneralUtils.vecEquals;
import static someoneok.kic.utils.PlayerUtils.getPlayerEyePos;
import static someoneok.kic.utils.PlayerUtils.isHoldingSupplies;
import static someoneok.kic.utils.StringUtils.removeFormatting;
import static someoneok.kic.utils.kuudra.TrajectorySolver.solvePearl;

@SideOnly(Side.CLIENT)
public class Pearls {
    private static final int[][] PEARL_DELAY = {
            //  0    1     2     3     4     5
            {   0,  3000, 4000, 5000, 6000, 6000 }, // No tali
            {   0,  2750, 3750, 4500, 5500, 5500 }, // T1 tali
            {   0,  2500, 3250, 4000, 5000, 5000 }, // T2 tali
            {   0,  2250, 3000, 3500, 4250, 4250 }  // T3 tali
    };

    public static int cachedInitialDelay = 200;
    public static int cachedDoubleDelay = 300;
    public static float textSizeSky = 1.5f;
    public static float textSizeFlat = 1.125f;
    public static float waypointSizeSky = 0.5f;
    public static float waypointSizeFlat = 0.375f;
    public static float timerOffset = 1f;

    private final List<PearlRenderData> pearlSolutions = new ArrayList<>();
    private final List<Vec3> currentSupplies = new ArrayList<>();
    private final Pattern progressPattern = Pattern.compile("\\[\\|+]\\s*(\\d+)%");
    private final Color allColor = new Color(255, 0, 0);
    private final Color myColor = new Color(0, 255, 0);

    private long progressStartTime = -1;
    private static boolean trackingPickup = false;
    private Vec3 mySupply = null;
    private Vec3 myDouble = null;
    private String cratePickupText = "";
    private long lastTitleEvent = -1;

    private boolean hasPostedPrimarySkyThrow = false;
    private boolean hasPostedPrimaryFlatThrow = false;
    private boolean hasPostedDoubleThrow = false;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        currentSupplies.clear();
        pearlSolutions.clear();
        mySupply = null;
        myDouble = null;
        resetTracking();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!LocationUtils.inKuudra() || phase() != KuudraPhase.SUPPLIES || !ApiUtils.isVerified()) return;

        if (KICConfig.pearlCalculator && !pearlSolutions.isEmpty()) {
            for (PearlRenderData data : pearlSolutions) {
                Color renderColor = data.isDouble ? myColor : (mySupply != null && vecEquals(mySupply, data.target) ? myColor : allColor);
                Vec3 pos = data.solution;
                float waypointSize = data.isSky ? waypointSizeSky : waypointSizeFlat;
                float textScale = data.isSky ? textSizeSky : textSizeFlat;

                RenderUtils.drawPixelBox(
                        pos,
                        renderColor,
                        waypointSize,
                        true,
                        event.partialTicks
                );
                if (KICConfig.showTimer) {
                    RenderUtils.renderNameTag(
                            data.cachedDisplay,
                            pos.xCoord,
                            pos.yCoord + (timerOffset * textScale),
                            pos.zCoord,
                            textScale,
                            false
                    );
                }
            }
        }

        if (KICConfig.showNothingSupplyWaypointsBeacon || KICConfig.showNothingSupplyWaypoints) {
            Color supplyColor = KICConfig.supplySpotColor.toJavaColor();

            for (Vec3 targetSupply : currentSupplies) {
                if (KICConfig.showNothingSupplyWaypointsBeacon) {
                    RenderUtils.drawBeaconBeam(
                            targetSupply,
                            supplyColor,
                            150,
                            true,
                            event.partialTicks
                    );
                }

                if (KICConfig.showNothingSupplyWaypoints) {
                    RenderUtils.renderNameTag(
                            "§aSupply",
                            targetSupply.xCoord,
                            targetSupply.yCoord + 2,
                            targetSupply.zCoord,
                            1,
                            false
                    );
                    RenderUtils.drawPixelBox(
                            targetSupply,
                            supplyColor,
                            1,
                            true,
                            event.partialTicks
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || mc.thePlayer == null
                || mc.theWorld == null
                || !LocationUtils.inKuudra()
                || phase() != KuudraPhase.SUPPLIES
                || !ApiUtils.isVerified()) return;
        if (lastTitleEvent != -1 && ServerTickUtils.getServerTime() - lastTitleEvent > 750) {
            KICLogger.info("Force resetting tracking as last title event was over 750ms ago.");
            resetTracking();
        }

        currentSupplies.clear();
        currentSupplies.addAll(KuudraUtils.getAllUncompletedSupplies());

        if (!KICConfig.pearlCalculator) return;

        Vec3 eyePos = getPlayerEyePos();
        if (eyePos == null) return;

        PickupSpot closestSpot = PickupSpot.getClosestSpot(eyePos);
        mySupply = getMyDropSpot(eyePos, closestSpot);
        pearlSolutions.clear();
        boolean hasSupplies = isHoldingSupplies();

        if (KICConfig.showAll) {
            for (Vec3 targetSupply : currentSupplies) {
                if (KICConfig.showSkyPearls) {
                    PearlSolution sky = solvePearl(true, eyePos, targetSupply);
                    tryAddPearl(sky, targetSupply, false, true, hasSupplies);
                }

                if (KICConfig.showFlatPearls) {
                    PearlSolution flat = solvePearl(false, eyePos, targetSupply);
                    tryAddPearl(flat, targetSupply, false, false, hasSupplies);
                }
            }
        } else {
            if (mySupply != null) {
                if (KICConfig.showSkyPearls) {
                    PearlSolution sky = solvePearl(true, eyePos, mySupply);
                    tryAddPearl(sky, mySupply, false, true, hasSupplies);
                }

                if (KICConfig.showFlatPearls) {
                    PearlSolution flat = solvePearl(false, eyePos, mySupply);
                    tryAddPearl(flat, mySupply, false, false, hasSupplies);
                }
            }
        }

        myDouble = null;
        if (KICConfig.showDoublePearls && closestSpot != PickupSpot.NONE) {
            for (DoublePearl pearlSpot : DoublePearlRegistry.getRoutesFrom(closestSpot)) {
                Vec3 targetSpot = pearlSpot.getLocation();
                if (pearlHitMagmaCube(eyePos, targetSpot)) return;

                if (myDouble == null) myDouble = targetSpot;
                PearlSolution doubleSol = solvePearl(true, eyePos, targetSpot);
                tryAddPearl(doubleSol, targetSpot, true, true, hasSupplies);
            }
        }
    }

    private void tryAddPearl(PearlSolution sol, Vec3 target, boolean isDouble, boolean isSky, boolean hasSupplies) {
        if (sol == null || sol.solution == null) return;

        PearlRenderData data = new PearlRenderData(sol, target, isDouble, isSky, hasSupplies, progressStartTime, trackingPickup);
        pearlSolutions.add(data);

        if (mySupply != null && !isDouble && vecEquals(target, mySupply)) {
            if (data.time <= 50) {
                if (isSky) {
                    if (!hasPostedPrimarySkyThrow) {
                        hasPostedPrimarySkyThrow = true;
                        KICEventBus.post(new ThrowPearlEvent(data));
                    }
                } else {
                    if (!hasPostedPrimaryFlatThrow) {
                        hasPostedPrimaryFlatThrow = true;
                        KICEventBus.post(new ThrowPearlEvent(data));
                    }
                }
            }
        } else if (myDouble != null && isSky && isDouble && vecEquals(target, myDouble)) {
            if (data.time <= 50 && !hasPostedDoubleThrow) {
                hasPostedDoubleThrow = true;
                KICEventBus.post(new ThrowPearlEvent(data));
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onTitle(TitleEvent.Incoming event) {
        if (!KICConfig.pearlCalculator
                || !LocationUtils.inKuudra()
                || phase() != KuudraPhase.SUPPLIES
                || event.getType() != TitleType.TITLE
                || !ApiUtils.isVerified()) return;

        String raw = removeFormatting(event.getComponent().getUnformattedText());
        if (!raw.contains("[||||||||||||||||||||]")) return;

        Matcher matcher = progressPattern.matcher(raw);
        if (!matcher.find()) return;

        if (KICConfig.moveableCratePickup) {
            event.setCanceled(true);
            cratePickupText = event.getComponent().getFormattedText();
            OverlayManager.getOverlay("CratePickup").updateText(cratePickupText);
        }

        int percent = Integer.parseInt(matcher.group(1));

        long now = ServerTickUtils.getServerTime();
        lastTitleEvent = now;
        if (percent == 0) {
            progressStartTime = now;
            trackingPickup = true;
        } else if (trackingPickup && percent == 100) {
            resetTracking();
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!KICConfig.pearlCalculator
                || !LocationUtils.inKuudra()
                || phase() != KuudraPhase.SUPPLIES
                || !ApiUtils.isVerified()) return;

        String message = removeFormatting(event.message.getUnformattedText());
        if ("You moved and the Chest slipped out of your hands!".equals(message)
                || " ☠ You were killed by Kuudra Follower and became a ghost.".equals(message)
                || "You retrieved some of Elle's supplies from the Lava!".equals(message)) {
            resetTracking();
        }
    }

    private boolean pearlHitMagmaCube(Vec3 from, Vec3 to) {
        final double x0 = from.xCoord, y0 = from.yCoord, z0 = from.zCoord;
        final double x1 = to.xCoord,   y1 = to.yCoord,   z1 = to.zCoord;

        final double minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        final double minY = Math.min(y0, y1), maxY = Math.max(y0, y1);
        final double minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);

        for (EntityMagmaCube cube : KuudraUtils.getMagmaCubes()) {
            if (cube == null || cube.isDead || !cube.isEntityAlive()) continue;
            AxisAlignedBB bb = cube.getEntityBoundingBox();
            if (bb.maxX < minX || bb.minX > maxX ||
                    bb.maxY < minY || bb.minY > maxY ||
                    bb.maxZ < minZ || bb.minZ > maxZ) continue;
            if (segmentIntersectsAABB(x0, y0, z0, x1, y1, z1, bb)) return true;
        }
        return false;
    }

    private static boolean segmentIntersectsAABB(double x0, double y0, double z0,
                                                 double x1, double y1, double z1,
                                                 AxisAlignedBB b) {
        double tmin = 0.0, tmax = 1.0;

        double dx = x1 - x0;
        if (Math.abs(dx) < 1e-12) {
            if (x0 < b.minX || x0 > b.maxX) return false;
        } else {
            double inv = 1.0 / dx;
            double t0 = (b.minX - x0) * inv;
            double t1 = (b.maxX - x0) * inv;
            if (t0 > t1) { double t = t0; t0 = t1; t1 = t; }
            tmin = Math.max(tmin, t0);
            tmax = Math.min(tmax, t1);
            if (tmin > tmax) return false;
        }

        double dy = y1 - y0;
        if (Math.abs(dy) < 1e-12) {
            if (y0 < b.minY || y0 > b.maxY) return false;
        } else {
            double inv = 1.0 / dy;
            double t0 = (b.minY - y0) * inv;
            double t1 = (b.maxY - y0) * inv;
            if (t0 > t1) { double t = t0; t0 = t1; t1 = t; }
            tmin = Math.max(tmin, t0);
            tmax = Math.min(tmax, t1);
            if (tmin > tmax) return false;
        }

        double dz = z1 - z0;
        if (Math.abs(dz) < 1e-12) {
            return !(z0 < b.minZ) && !(z0 > b.maxZ);
        } else {
            double inv = 1.0 / dz;
            double t0 = (b.minZ - z0) * inv;
            double t1 = (b.maxZ - z0) * inv;
            if (t0 > t1) { double t = t0; t0 = t1; t1 = t; }
            tmin = Math.max(tmin, t0);
            tmax = Math.min(tmax, t1);
            return !(tmin > tmax);
        }
    }

    private void resetTracking() {
        trackingPickup = false;
        progressStartTime = -1;
        lastTitleEvent = -1;
        hasPostedPrimarySkyThrow = false;
        hasPostedPrimaryFlatThrow = false;
        hasPostedDoubleThrow = false;
        if (!cratePickupText.isEmpty()) {
            cratePickupText = "";
            OverlayManager.getOverlay("CratePickup").updateText(cratePickupText);
        }
    }

    private Vec3 getMyDropSpot(Vec3 vector, PickupSpot pickUpSpot) {
        Supply[] supplies = KuudraUtils.getSupplies();
        SupplySpot noPre = NoPre.getNoPreSpot();

        boolean noPreIsNothing = false;
        if (noPre != null) {
            Supply noPreSupply = getSupply(supplies, noPre.ordinal());
            noPreIsNothing = (noPreSupply != null && noPreSupply.getStatus() == SupplyStatus.NOTHING);
        }

        Vec3 fallback = noPreIsNothing ? noPre.getLocation() : vector;

        if (pickUpSpot == PickupSpot.SQUARE && noPreIsNothing) return noPre.getLocation();

        Supply supply = null;
        switch (pickUpSpot) {
            case SHOP:     supply = getSupply(supplies, 0); break;
            case X:        supply = getSupply(supplies, 1); break;
            case X_CANNON: supply = getSupply(supplies, 2); break;
            case EQUALS:   supply = getSupply(supplies, 3); break;
            case SLASH:    supply = getSupply(supplies, 4); break;
            case TRIANGLE: supply = getSupply(supplies, 5); break;
            default:       break;
        }

        if (supply != null && supply.getStatus() == SupplyStatus.NOTHING) return supply.getSpot().getLocation();
        return getClosestSupply(vector).orElse(fallback);
    }

    private Supply getSupply(Supply[] supplies, int index) {
        if (supplies == null || index < 0 || index >= supplies.length) return null;
        return supplies[index];
    }

    private Optional<Vec3> getClosestSupply(Vec3 vector) {
        double minDistSq = Double.MAX_VALUE;
        Vec3 closest = null;

        for (Supply supply : KuudraUtils.getSupplies()) {
            if (supply.getStatus() != SupplyStatus.NOTHING) continue;
            Vec3 loc = supply.getSpot().getLocation();
            double distSq = vector.squareDistanceTo(loc);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = loc;
            }
        }

        return Optional.ofNullable(closest);
    }

    public static void updatePearlDelays() {
        cachedInitialDelay = KICConfig.advancedPearlSettings ? KICConfig.APInitialDelay : 100;
        cachedDoubleDelay = KICConfig.advancedPearlSettings ? KICConfig.APDoubleDelay : 200;
    }

    public static void updateSizes() {
        textSizeSky = 1.5f;
        textSizeFlat = 1.125f;
        waypointSizeSky = 0.5f;
        waypointSizeFlat = 0.375f;

        if (KICConfig.advancedPearlSettings) {
            textSizeSky = round2(textSizeSky * KICConfig.APTextSizeScaleSky);
            textSizeFlat = round2(textSizeFlat * KICConfig.APTextSizeScaleFlat);
            waypointSizeSky = round2(waypointSizeSky * KICConfig.APWaypointSizeScaleSky);
            waypointSizeFlat = round2(waypointSizeFlat * KICConfig.APWaypointSizeScaleFlat);
        } else {
            textSizeSky = round2(textSizeSky);
            textSizeFlat = round2(textSizeFlat);
            waypointSizeSky = round2(waypointSizeSky);
            waypointSizeFlat = round2(waypointSizeFlat);
        }
    }

    public static void updateTimerLocation() { timerOffset = KICConfig.APTimerPos == 0 ? 1f : -1f; }
    public static boolean isPickingUpSupply() { return trackingPickup; }
    public static int getPearlDelay(int talismanTier, int kuudraTier) {
        if (talismanTier < 0) talismanTier = 0;
        if (talismanTier > 3) talismanTier = 3;
        if (kuudraTier < 1) kuudraTier = 1;
        if (kuudraTier > 5) kuudraTier = 5;

        return PEARL_DELAY[talismanTier][kuudraTier];
    }
}
