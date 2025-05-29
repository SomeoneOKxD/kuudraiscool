package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.TitleEvent;
import someoneok.kic.models.kuudra.DoublePearlSpot;
import someoneok.kic.models.kuudra.PearlRenderData;
import someoneok.kic.models.kuudra.PearlSolution;
import someoneok.kic.models.kuudra.SupplyPickUpSpot;
import someoneok.kic.models.misc.TitleType;
import someoneok.kic.utils.*;
import someoneok.kic.utils.dev.KICLogger;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.TrajectorySolver.solvePearl;
import static someoneok.kic.utils.ApiUtils.isAdmin;
import static someoneok.kic.utils.GeneralUtils.round2;
import static someoneok.kic.utils.GeneralUtils.vecEquals;
import static someoneok.kic.utils.PlayerUtils.getPlayerEyePos;
import static someoneok.kic.utils.StringUtils.removeFormatting;

@SideOnly(Side.CLIENT)
public class Waypoints {
    public static final Map<Integer, Integer> KUUDRA_TIER_DELAY;
    public static int cachedInitialDelay = 200;
    public static int cachedDoubleDelay = 300;
    public static float textSizeSky = 1.5f;
    public static float textSizeFlat = 1.125f;
    public static float waypointSizeSky = 0.5f;
    public static float waypointSizeFlat = 0.375f;
    public static float timerOffset = 1f;

    static {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 3500);
        map.put(2, 4500);
        map.put(3, 5500);
        map.put(4, 6500);
        map.put(5, 6500);
        KUUDRA_TIER_DELAY = Collections.unmodifiableMap(map);
    }

    private final List<PearlRenderData> pearlSolutions = new ArrayList<>();
    private final List<Vec3> currentSupplies = new ArrayList<>();
    private final Pattern progressPattern = Pattern.compile("\\[\\|+]\\s*(\\d+)%");
    private final Color allColor = new Color(255, 0, 0);
    private final Color myColor = new Color(0, 255, 0);

    private long progressStartTime = -1;
    private boolean tracking = false;
    private Vec3 mySupply = null;
    private PearlRenderData mySupplyData = null;
    private long autoPearlStart = -1;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null
                || mc.theWorld == null
                || !LocationUtils.inKuudra
                || Kuudra.currentPhase != 1
                || !ApiUtils.isVerified()) return;

        if (KICConfig.showNothingSupplyWaypointsBeacon) {
            for (Vec3 targetSupply : currentSupplies) {
                RenderUtils.drawBeaconBeam(
                        targetSupply,
                        KICConfig.supplySpotColor.toJavaColor(),
                        150,
                        true,
                        event.partialTicks
                );
            }
        }

        if (KICConfig.pearlCalculator && !pearlSolutions.isEmpty()) {
            for (PearlRenderData data : pearlSolutions) {
                Color renderColor = data.isDouble
                        ? myColor
                        : (mySupply != null && vecEquals(mySupply, data.target) ? myColor : allColor);
                Vec3 pos = data.solution;
                float size = data.isSky ? waypointSizeSky : waypointSizeFlat;

                if (KICConfig.advancedPearlSettings && KICConfig.APUseCustomShape) {
                    drawPearlWaypointShape(pos, size, renderColor, event.partialTicks);
                } else {
                    RenderUtils.drawPixelBox(
                            pos,
                            renderColor,
                            size,
                            true,
                            event.partialTicks
                    );
                }

                float scale = data.isSky ? textSizeSky : textSizeFlat;
                RenderUtils.renderNameTag(
                        data.cachedDisplay,
                        pos.xCoord,
                        pos.yCoord + (timerOffset * scale),
                        pos.zCoord,
                        scale,
                        false
                );
            }
        }

        if (KICConfig.showNothingSupplyWaypoints) {
            for (Vec3 targetSupply : currentSupplies) {
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
                        KICConfig.supplySpotColor.toJavaColor(),
                        1,
                        true,
                        event.partialTicks
                );
            }
        }
    }

    public static void drawPearlWaypointShape(Vec3 pos, float size, Color color, float partialTicks) {
        if (KICConfig.APCustom == 0) {
            RenderUtils.drawFacingCircle(
                    pos,
                    size,
                    KICConfig.APCustomShapeQuality,
                    color,
                    partialTicks
            );
        } else {
            RenderUtils.drawFacingHeart(
                    pos,
                    size,
                    KICConfig.APCustomShapeQuality,
                    color,
                    partialTicks
            );
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        currentSupplies.clear();
        pearlSolutions.clear();
        mySupply = null;
        mySupplyData = null;
        progressStartTime = -1;
        tracking = false;
        autoPearlStart = -1;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || mc.thePlayer == null
                || mc.theWorld == null
                || !LocationUtils.inKuudra
                || Kuudra.currentPhase != 1
                || !ApiUtils.isVerified()) return;

        currentSupplies.clear();
        currentSupplies.addAll(Kuudra.getAllUncompletedSpots());

        if (KICConfig.pearlCalculator) {
            Vec3 eyePos = getPlayerEyePos();
            if (eyePos != null) {
                mySupply = Kuudra.getMySupplySpot(eyePos);
                pearlSolutions.clear();
                boolean hasSupplies = hasSupplies();

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

                if (KICConfig.showDoublePearls) {
                    for (DoublePearlSpot pearlSpot : DoublePearlSpot.getSpot(SupplyPickUpSpot.getClosestSpot(eyePos).orElse(SupplyPickUpSpot.NONE))) {
                        Vec3 targetSpot = pearlSpot.getLocation();
                        PearlSolution doubleSol = solvePearl(true, eyePos, targetSpot);
                        tryAddPearl(doubleSol, targetSpot, true, true, hasSupplies);
                    }
                }
            }
        }
    }

    private void tryAddPearl(PearlSolution sol, Vec3 target, boolean isDouble, boolean isSky, boolean hasSupplies) {
        if (sol != null && sol.solution != null) {
            PearlRenderData data = new PearlRenderData(sol.solution, sol.yaw, sol.pitch, sol.flightTimeMs, target, isDouble, isSky);
            data.updateDisplay(hasSupplies, progressStartTime, tracking);
            pearlSolutions.add(data);

            if (mySupply != null && isSky && !isDouble && vecEquals(target, mySupply)) {
                mySupplyData = data;
            }
        }
    }

    @SubscribeEvent
    public void onTitle(TitleEvent.Incoming event) {
        if (!KICConfig.pearlCalculator
                || !LocationUtils.inKuudra
                || Kuudra.currentPhase != 1
                || event.getType() != TitleType.TITLE
                || !ApiUtils.isVerified()) return;

        String raw = removeFormatting(event.getComponent().getUnformattedText());
        if (!raw.contains("[||||||||||||||||||||]")) return;

        Matcher matcher = progressPattern.matcher(raw);
        if (!matcher.find()) return;

        int percent = Integer.parseInt(matcher.group(1));

        if (percent == 0) {
            progressStartTime = Kuudra.logicalTimeMs;
            tracking = true;
            triggerAutoPearl();
        } else if (tracking && percent == 100) {
            resetTracking();
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!KICConfig.pearlCalculator
                || !LocationUtils.inKuudra
                || Kuudra.currentPhase != 1
                || !ApiUtils.isVerified()) return;

        String message = removeFormatting(event.message.getUnformattedText());
        if ("You moved and the Chest slipped out of your hands!".equals(message)) {
            resetTracking();
            autoPearlStart = -1;
        }
    }

    private void resetTracking() {
        tracking = false;
        progressStartTime = -1;
    }

    private boolean hasSupplies() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return (heldItem != null && heldItem.getDisplayName().contains("Elle's Supplies"));
    }

    // Auto Pearl very shit, needs to be modified before releasing
    private void triggerAutoPearl() {
        if (!KICConfig.autoPearls || mySupplyData == null || !isAdmin()) return;
        long start = Kuudra.logicalTimeMs;
        autoPearlStart = start;
        long time = mySupplyData.getTimeUntilThrow(progressStartTime, tracking);
        long trigger = Math.max(300, time - 300);
        KICLogger.info("Triggering auto pearl in: " + trigger);
        Multithreading.schedule(
                () -> {
                    if (autoPearlStart != start) return;
                    autoPearl(mySupplyData.yaw, mySupplyData.pitch, 200, start, null);
                },
                trigger,
                TimeUnit.MILLISECONDS
        );
    }

    private void autoPearl(float yaw, float pitch, int time, long start, Runnable runAfterThrow) {
        if (!KICConfig.autoPearls || !isAdmin()) return;
        RotationUtils.smoothRotateTo(yaw, pitch, time, () -> {
            if (autoPearlStart != start) return;
            int pearlSlot = PlayerUtils.getHotbarSlotIndex(Items.ender_pearl);
            if (pearlSlot < 0) return;
            PlayerUtils.swapToIndex(pearlSlot);
            Multithreading.schedule(() -> {
                if (autoPearlStart != start) return;
                MovingObjectPosition hit = mc.thePlayer.rayTrace(10.0, 1.0F);

                if (hit == null || hit.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                    PlayerUtils.rightClick();
                    if (runAfterThrow != null) runAfterThrow.run();
                } else {
                    KICLogger.info("[AutoPearl] Obstructed by " + hit.typeOfHit + ", canceling throw.");
                }
                autoPearlStart = -1;
            }, 100, TimeUnit.MILLISECONDS);
        });
    }

    public static void updatePearlDelays() {
        cachedInitialDelay = KICConfig.advancedPearlSettings ? KICConfig.APInitialDelay : 200;
        cachedDoubleDelay = KICConfig.advancedPearlSettings ? KICConfig.APDoubleDelay : 300;
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

    public static void updateTimerLocation() {
        timerOffset = KICConfig.APTimerPos == 0 ? 1f : -1f;
    }
}
