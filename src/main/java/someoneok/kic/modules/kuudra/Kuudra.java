package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.BoneHitEvent;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.TimedEvent;
import someoneok.kic.utils.*;
import someoneok.kic.utils.kuudra.KuudraUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phaseOrdinal;
import static someoneok.kic.utils.ChatUtils.sendCommand;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.ItemUtils.getItemId;
import static someoneok.kic.utils.LocationUtils.getScoreboardLines;
import static someoneok.kic.utils.StringUtils.*;

public class Kuudra {
    private static final double SUPPLY_ZOMBIE_DISTANCE_SQ = 3.0 * 3.0;
    private static final Pattern RECOVERED = Pattern.compile("^(?:\\[[^]]+] )?(\\w+) recovered one of Elle's supplies! \\((\\d)/6\\)$");
    private static final Pattern FRESH = Pattern.compile("^Party > (?:\\[[^]]+] )?(\\w+): FRESH.*");

    private final List<AxisAlignedBB> hitboxes = new ArrayList<>();
    private static final List<TimedEvent> freshTimes = new ArrayList<>();
    private final TreeMap<Integer, TimedEvent> supplyTimes = new TreeMap<>();
    private final Map<String, Long> freshCooldownMap = new HashMap<>();

    private static long tickCount = 0;
    private static long serverTickBaseline = -1;

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!LocationUtils.inKuudra() || !ApiUtils.isVerified()) return;

        String raw = removeFormatting(event.message.getUnformattedText());

        if (raw.equals("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!")) {
            if (KICConfig.kuudraNotiFresh) Notifications.showMessage("§aFRESH", KICConfig.kuudraNotiFreshTime);
            if (KICConfig.announceFresh && PartyUtils.inParty()) sendCommand("/pc FRESH");
            return;
        }

        KuudraPhase currentPhase = phase();
        long now = System.currentTimeMillis();
        if (currentPhase == KuudraPhase.SUPPLIES && raw.contains("recovered")) {
            Matcher recovered = RECOVERED.matcher(raw);
            if (recovered.matches()) {
                String player = getRankColor(raw) + recovered.group(1);
                int supplyNum = Integer.parseInt(recovered.group(2));

                if (supplyTimes.containsKey(supplyNum)) return;
                if (supplyNum == 6 && !BuildHelper.isStarted()) BuildHelper.start();

                long time = KuudraPhase.SUPPLIES.getTime(now);
                supplyTimes.put(supplyNum, new TimedEvent(player, time));

                if (KICConfig.customSupplyDropMsg) {
                    event.setCanceled(true);

                    String formattedMessage = String.format(
                            "%s %s §arecovered a supply in %s%s §r§8(%d/6)",
                            KICPrefix,
                            player,
                            KuudraSplits.getRecoveredColor(time),
                            formatElapsedTimeMs(time),
                            supplyNum
                    );

                    sendMessageToPlayer(formattedMessage);
                }
                return;
            }
        }

        if (currentPhase == KuudraPhase.BUILD && raw.contains("FRESH")) {
            Matcher fresh = FRESH.matcher(raw);
            if (fresh.matches()) {
                String player = getRankColor(raw) + fresh.group(1);

                Long lastCallTime = freshCooldownMap.get(player);
                long time = KuudraPhase.BUILD.getTime(now);
                if (lastCallTime == null || now - lastCallTime > 5000) {
                    freshTimes.add(new TimedEvent(player, time));
                    freshCooldownMap.put(player, time);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        hitboxes.clear();
        KuudraPhase.reset();
        tickCount = 0;
        serverTickBaseline = -1;
        supplyTimes.clear();
        freshTimes.clear();
        freshCooldownMap.clear();
        KuudraSplits.reset();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || mc.thePlayer == null
                || mc.theWorld == null
                || !LocationUtils.inKuudra()
                || !ApiUtils.isVerified()) return;

        KuudraPhase currentPhase = phase();
        if (currentPhase == KuudraPhase.NONE || currentPhase == KuudraPhase.END) return;

        if (serverTickBaseline < 0) serverTickBaseline = ServerTickUtils.getServerTick();
        tickCount++;
        hitboxes.clear();

        if (mc.thePlayer.posY < 10 && phase() != KuudraPhase.KILL) KuudraPhaseTracker.updatePhaseToKill();

        if (KICConfig.buildTimer && phaseOrdinal() <= KuudraPhase.BUILD.ordinal()) {
            for (String line : getScoreboardLines()) {
                String raw = removeUnicode(line);
                if (!raw.startsWith("Protect Elle (") || !raw.endsWith("%)")) continue;

                int start = raw.indexOf('(') + 1;
                int end = raw.indexOf('%');

                if (start <= 0 || end <= start) continue;

                try {
                    String percentStr = raw.substring(start, end).trim();
                    BuildHelper.setProgress(Integer.parseInt(percentStr));
                    break;
                } catch (NumberFormatException ignored) {}
            }
        }

        if (KICConfig.supplyBox && currentPhase == KuudraPhase.SUPPLIES) {
            for (Vec3 crate : KuudraUtils.getCrates()) {
                double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
                double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

                for (EntityZombie zombie : KuudraUtils.getZombies()) {
                    AxisAlignedBB box = zombie.getEntityBoundingBox();
                    if (box == null) continue;

                    double width = box.maxX - box.minX;
                    double height = box.maxY - box.minY;
                    double depth = box.maxZ - box.minZ;

                    if (width > 10 || height > 10 || depth > 10 ||
                            Double.isNaN(box.minX) || Double.isNaN(box.maxX) ||
                            Double.isInfinite(box.minX) || Double.isInfinite(box.maxX)) {
                        continue;
                    }

                    double cx = (box.minX + box.maxX) * 0.5;
                    double cy = (box.minY + box.maxY) * 0.5;
                    double cz = (box.minZ + box.maxZ) * 0.5;

                    double dx = cx - crate.xCoord;
                    double dy = cy - crate.yCoord;
                    double dz = cz - crate.zCoord;

                    if (dx * dx + dy * dy + dz * dz <= SUPPLY_ZOMBIE_DISTANCE_SQ) {
                        minX = Math.min(minX, box.minX);
                        minY = Math.min(minY, box.minY);
                        minZ = Math.min(minZ, box.minZ);
                        maxX = Math.max(maxX, box.maxX);
                        maxY = Math.max(maxY, box.maxY);
                        maxZ = Math.max(maxZ, box.maxZ);
                    }
                }

                if (minX != Double.POSITIVE_INFINITY) {
                    hitboxes.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
                }
            }
        }

        if (KICConfig.kuudraSplits) KuudraSplits.updateKuudraSplits();
        if (KICConfig.kuudraSupplyTimes && currentPhase == KuudraPhase.SUPPLIES) KuudraSplits.updateSupplyTimes(supplyTimes);
        if (KICConfig.kuudraFreshTimes && currentPhase == KuudraPhase.BUILD) KuudraSplits.updateFreshTimes(freshTimes);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!LocationUtils.inKuudra() || !ApiUtils.isVerified()) return;

        KuudraPhase currentPhase = phase();
        if (currentPhase == KuudraPhase.END) return;

        if (currentPhase == KuudraPhase.SUPPLIES || currentPhase == KuudraPhase.EATEN) {
            Color supplyColor = KICConfig.supplyColor.toJavaColor();

            if (KICConfig.supplyWaypoints) {
                for (Vec3 crate : KuudraUtils.getCrates()) {
                    RenderUtils.drawBeaconBeam(crate, supplyColor, 150, true, event.partialTicks);
                }
            }

            if (KICConfig.supplyBox) {
                for (AxisAlignedBB hitbox : hitboxes) {
                    RenderUtils.drawBox(
                            hitbox,
                            supplyColor,
                            false,
                            false,
                            event.partialTicks
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public void onBoneHit(BoneHitEvent e) {
        if (!KICConfig.backboneHit || !LocationUtils.inKuudra() || phase() != KuudraPhase.KILL) return;
        if (e.getTarget().getEntityId() != KuudraUtils.getKuudra().getEntityId()) return;

        final EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        final ItemStack held = player.getHeldItem();
        if (held == null) return;

        final String itemId = getItemId(held);
        if (itemId == null || itemId.contains("BREATH") || itemId.contains("JUJU") || itemId.contains("TERMINATOR")) return;

        final String holding = held.getDisplayName();
        String wearing = "(none)";
        final ItemStack helm = player.getCurrentArmor(3);
        if (helm != null) wearing = helm.getDisplayName();

        if (e.getType() == BoneHitEvent.Hit.BACK) {
            sendMessageToPlayer(
                    KICPrefix +
                            " §7Back bone hit at §b" +
                            formatElapsedTimeMs(KuudraPhase.KILL.getTime(System.currentTimeMillis())) +
                            " §r| §7Holding - " + holding +
                            " §r| §7Wearing - " + wearing
            );

            if (KICConfig.rendNow) {
                final String msg = "§8-= §b§lRend NOW! §r§8=-";
                Notifications.showMessage(msg, 10);
                Multithreading.schedule(() -> Notifications.showMessage(msg, 10), 500, TimeUnit.MILLISECONDS);
                Multithreading.schedule(() -> Notifications.showMessage(msg, 10), 1000, TimeUnit.MILLISECONDS);
                Multithreading.schedule(() -> Notifications.showMessage(msg, 10), 1500, TimeUnit.MILLISECONDS);
                Multithreading.schedule(() -> Notifications.showMessage(msg, 10), 2000, TimeUnit.MILLISECONDS);
            }
        }
    }

    private static long getSessionServerTicks() { return Math.max((ServerTickUtils.getServerTick() - serverTickBaseline), 0); }
    public static long getTotalLagTimeMs() { return Math.max(tickCount - getSessionServerTicks(), 0) * 50; }
    public static double getTotalLagTimeS() { return getTotalLagTimeMs() / 1000.0; }
    public static long getTotalLagTimeTicks() { return Math.max(tickCount - getSessionServerTicks(), 0); }
    public static List<TimedEvent> getFreshTimes() { return freshTimes; }
}
