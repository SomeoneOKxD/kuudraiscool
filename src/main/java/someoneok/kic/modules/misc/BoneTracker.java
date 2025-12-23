package someoneok.kic.modules.misc;

import com.google.common.base.Predicate;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.BoneHitEvent;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.models.Island;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.PlayerUtils;
import someoneok.kic.utils.ServerTickUtils;

import java.util.*;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.ItemUtils.getItemId;

public class BoneTracker {
    private static final int EXPECT_WINDOW_TICKS = 12;
    private static final double MAX_CANDIDATE_DIST_SQ = 4.0 * 4.0;
    private static final double OWNER_PROMOTE_DIST_HARD_SQ = 6.0 * 6.0;
    private static final int CANDIDATE_MAX_AGE_TICKS = 40;
    private static final double FWD_RAY_DOT_MIN = 0.85;
    private static final double NEARBY_ENTITY_CHECK_RADIUS = 4.0;
    private static final int TRACK_MAX_AGE_TICKS = 220;
    private static final double MARKER_HEAD_Y_BIG = 1.78;
    private static final double MARKER_HEAD_Y_SMALL = 0.90;
    private static final double HEAD_HALF_XZ = 0.75;
    private static final double HEAD_HALF_Y = 0.15;
    private static final int MAX_TARGETS_PER_BONE = 10;

    private static final Predicate<Entity> LIVING_FILTER = entity -> {
        if (!(entity instanceof EntityLivingBase)) return false;
        if (entity instanceof EntityPlayer) return false;
        if (entity instanceof EntityArmorStand) return false;
        return !entity.isInvisible();
    };

    private long expectWindowEndTick = -1L;
    private boolean throwingBone = false;
    private Vec3 throwOrigin = null;
    private Vec3 throwForward = null;
    private int currentThrowSeq = 0;
    private Vec3 throwPlayerPos = null;

    private final Map<Integer, CandidateStand> candidates = new HashMap<>();
    private final Map<Integer, BoneStand> tracked = new HashMap<>();
    private final Map<Integer, ArrayList<EntityLivingBase>> byBone = new HashMap<>();

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        expectWindowEndTick = -1L;
        throwingBone = false;
        throwOrigin = null;
        throwForward = null;
        throwPlayerPos = null;
        candidates.clear();
        tracked.clear();
        byBone.clear();
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent e) {
        if (!(Mouse.getEventButtonState() && Mouse.getEventButton() == 1) || mc.currentScreen != null) return;

        final EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;

        ItemStack held = p.getHeldItem();
        if (held == null) return;

        Item heldItem = held.getItem();
        if (heldItem != Items.bone) return;

        String itemId = getItemId(held);
        if (itemId == null || !(itemId.equals("STARRED_BONE_BOOMERANG") || itemId.equals("BONE_BOOMERANG"))) return;

        long nowTick = ServerTickUtils.getServerTick();

        currentThrowSeq++;
        if (currentThrowSeq == Integer.MAX_VALUE) currentThrowSeq = 1;

        throwingBone = true;
        expectWindowEndTick = nowTick + EXPECT_WINDOW_TICKS;
        throwOrigin = PlayerUtils.getPlayerEyePos();
        throwForward = getLookVec(p);
        throwPlayerPos = new Vec3(p.posX, p.posY, p.posZ);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        if (!throwingBone) return;
        if (!(e.entity instanceof EntityArmorStand) || !e.world.isRemote) return;

        long nowTick = ServerTickUtils.getServerTick();
        if (expectWindowEndTick < 0 || nowTick > expectWindowEndTick) return;

        final EntityArmorStand s = (EntityArmorStand) e.entity;
        final int id = s.getEntityId();
        if (tracked.containsKey(id) || candidates.containsKey(id)) return;

        final EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        final double dxNow = s.posX - player.posX;
        final double dyNow = s.posY - player.posY;
        final double dzNow = s.posZ - player.posZ;
        final double distSqNow = dxNow*dxNow + dyNow*dyNow + dzNow*dzNow;

        double distSqThrow = Double.POSITIVE_INFINITY;
        if (throwPlayerPos != null) {
            final double dxT = s.posX - throwPlayerPos.xCoord;
            final double dyT = s.posY - throwPlayerPos.yCoord;
            final double dzT = s.posZ - throwPlayerPos.zCoord;
            distSqThrow = dxT*dxT + dyT*dyT + dzT*dzT;
        }
        final double best = Math.min(distSqNow, distSqThrow);
        if (best > MAX_CANDIDATE_DIST_SQ) return;

        boolean ownerLocal = false;
        double rayDot = -2.0;
        if (throwOrigin != null && throwForward != null) {
            Vec3 head = getStandHeadPos(s);
            double sx = head.xCoord - throwOrigin.xCoord;
            double sy = head.yCoord - throwOrigin.yCoord;
            double sz = head.zCoord - throwOrigin.zCoord;

            double lenSq = sx * sx + sy * sy + sz * sz;
            if (lenSq > 1e-6 && lenSq <= OWNER_PROMOTE_DIST_HARD_SQ) {
                double len = Math.sqrt(lenSq);
                double rx = sx / len;
                double ry = sy / len;
                double rz = sz / len;

                rayDot = throwForward.xCoord * rx +
                         throwForward.yCoord * ry +
                         throwForward.zCoord * rz;
                if (rayDot >= FWD_RAY_DOT_MIN) ownerLocal = true;
            }
        }

        CandidateStand c = new CandidateStand(id, ownerLocal);
        candidates.put(id, c);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        final World world = mc.theWorld;
        final EntityPlayerSP player = mc.thePlayer;
        if (world == null || player == null) return;

        long nowTick = ServerTickUtils.getServerTick();
        if (throwingBone && expectWindowEndTick >= 0 && nowTick > expectWindowEndTick) {
            throwingBone = false;
            expectWindowEndTick = -1L;
        }
        if (!throwingBone && candidates.isEmpty() && tracked.isEmpty()) return;

        if (!candidates.isEmpty()) {
            for (Iterator<Map.Entry<Integer, CandidateStand>> it = candidates.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Integer, CandidateStand> en = it.next();
                final int id = en.getKey();
                if (tracked.containsKey(id)) {
                    it.remove();
                    continue;
                }

                final Entity ent = world.getEntityByID(id);
                if (!(ent instanceof EntityArmorStand)) {
                    it.remove();
                    continue;
                }
                final EntityArmorStand s = (EntityArmorStand) ent;

                final CandidateStand c = en.getValue();
                c.ageTicks++;
                boolean promoted = tryPromoteCandidate(s, c);
                if (promoted) {
                    it.remove();
                } else if (c.ageTicks > CANDIDATE_MAX_AGE_TICKS) {
                    it.remove();
                }
            }
        }

        if (tracked.isEmpty()) return;
        final double px = player.posX, py = player.posY, pz = player.posZ;

        for (Iterator<Map.Entry<Integer, BoneStand>> it = tracked.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, BoneStand> en = it.next();
            final int id = en.getKey();
            final BoneStand bs = en.getValue();

            final Entity ent = world.getEntityByID(id);
            if (!(ent instanceof EntityArmorStand)) {
                it.remove();
                continue;
            }
            final EntityArmorStand s = (EntityArmorStand) ent;

            final Vec3 currHead = getStandHeadPos(s);
            if (bs.prevHeadPos == null) bs.prevHeadPos = currHead;

            final double mx = currHead.xCoord - bs.prevHeadPos.xCoord;
            final double my = currHead.yCoord - bs.prevHeadPos.yCoord;
            final double mz = currHead.zCoord - bs.prevHeadPos.zCoord;
            final double m2 = mx*mx + my*my + mz*mz;

            Phase oldPhase = bs.phase;

            if (bs.origin != null && bs.forwardUsed != null) {
                // Fix forward direction once we have a reliable movement sample
                if (!bs.forwardFixed && m2 > 1e-6) {
                    double mdot =
                            mx * bs.forwardUsed.xCoord +
                                    my * bs.forwardUsed.yCoord +
                                    mz * bs.forwardUsed.zCoord;
                    if (mdot < 0.0) {
                        bs.forwardUsed = new Vec3(
                                -bs.forwardUsed.xCoord,
                                -bs.forwardUsed.yCoord,
                                -bs.forwardUsed.zCoord
                        );
                    }
                    bs.forwardFixed = true;
                }

                Vec3 d = new Vec3(
                        currHead.xCoord - bs.origin.xCoord,
                        currHead.yCoord - bs.origin.yCoord,
                        currHead.zCoord - bs.origin.zCoord
                );

                double along =
                        bs.forwardUsed.xCoord * d.xCoord +
                                bs.forwardUsed.yCoord * d.yCoord +
                                bs.forwardUsed.zCoord * d.zCoord;

                if (Double.isNaN(bs.prevAlong)) {
                    bs.prevAlong = along;
                } else {
                    double delta = along - bs.prevAlong;
                    final double EPS = 1e-4;

                    if (delta > EPS) {
                        bs.phase = Phase.OUTBOUND;
                        bs.sawOutbound = true;
                    } else if (delta < -EPS && bs.sawOutbound) {
                        bs.phase = Phase.RETURNING;
                    }

                    if (!bs.hasTurned && oldPhase == Phase.OUTBOUND && bs.phase == Phase.RETURNING) {
                        bs.hasTurned = true;
                    }

                    // Once turned, never go back
                    if (bs.hasTurned) bs.phase = Phase.RETURNING;

                    bs.prevAlong = along;
                }
            } else {
                // fallback to old logic (player-relative) if something is missing
                final double tx = currHead.xCoord - px;
                final double ty = currHead.yCoord - py;
                final double tz = currHead.zCoord - pz;
                final double lb2 = tx*tx + ty*ty + tz*tz;

                double dot = 0.0;
                if (m2 >= 1e-12 && lb2 >= 1e-12) dot = (mx*tx + my*ty + mz*tz) / Math.sqrt(m2 * lb2);
                if (dot > 0.02) bs.phase = Phase.OUTBOUND;
                else if (dot < -0.02) bs.phase = Phase.RETURNING;

                if (oldPhase != Phase.RETURNING && bs.phase == Phase.RETURNING) {
                    bs.hasTurned = true;
                }
            }

            bs.currHeadPos = currHead;
            bs.updateHeadBox(currHead);
            bs.ticksAlive++;

            if (s.isDead || bs.ticksAlive > TRACK_MAX_AGE_TICKS) {
                it.remove();
            }
        }

        if (tracked.isEmpty()) return;

        AxisAlignedBB union = computeUnion(tracked.values());
        List<EntityLivingBase> pool = world.getEntitiesWithinAABB(EntityLivingBase.class, union, LIVING_FILTER);

        byBone.keySet().retainAll(tracked.keySet());
        for (ArrayList<EntityLivingBase> lst : byBone.values()) lst.clear();
        for (Integer id : tracked.keySet()) byBone.computeIfAbsent(id, k -> new ArrayList<>(8));

        for (EntityLivingBase tgt : pool) {
            AxisAlignedBB mobBox = tgt.getEntityBoundingBox();
            for (Map.Entry<Integer, BoneStand> en : tracked.entrySet()) {
                BoneStand bs = en.getValue();
                if (mobBox.intersectsWith(bs.toAABB())) byBone.get(en.getKey()).add(tgt);
            }
        }

        for (Map.Entry<Integer, BoneStand> en : tracked.entrySet()) {
            BoneStand bs = en.getValue();
            List<EntityLivingBase> local = byBone.get(en.getKey());
            detectHits(bs, bs.prevHeadPos, bs.currHeadPos, local, px, py, pz);
        }

        for (BoneStand bs : tracked.values()) bs.prevHeadPos = bs.currHeadPos;
    }

    private static AxisAlignedBB computeUnion(Collection<BoneStand> bones) {
        double uxmin = Double.POSITIVE_INFINITY, uymin = Double.POSITIVE_INFINITY, uzmin = Double.POSITIVE_INFINITY;
        double uxmax = Double.NEGATIVE_INFINITY, uymax = Double.NEGATIVE_INFINITY, uzmax = Double.NEGATIVE_INFINITY;

        for (BoneStand bs : bones) {
            uxmin = Math.min(uxmin, bs.minX);
            uymin = Math.min(uymin, bs.minY);
            uzmin = Math.min(uzmin, bs.minZ);
            uxmax = Math.max(uxmax, bs.maxX);
            uymax = Math.max(uymax, bs.maxY);
            uzmax = Math.max(uzmax, bs.maxZ);
        }

        return new AxisAlignedBB(uxmin, uymin, uzmin, uxmax, uymax, uzmax);
    }

    private static boolean segmentIntersectsAABB(AxisAlignedBB bb, Vec3 a, Vec3 b) {
        double tMin = 0.0;
        double tMax = 1.0;

        double dx = b.xCoord - a.xCoord;
        double dy = b.yCoord - a.yCoord;
        double dz = b.zCoord - a.zCoord;

        // X slab
        if (Math.abs(dx) < 1e-12) {
            if (a.xCoord < bb.minX || a.xCoord > bb.maxX) return false;
        } else {
            double inv = 1.0 / dx;
            double t1 = (bb.minX - a.xCoord) * inv;
            double t2 = (bb.maxX - a.xCoord) * inv;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMin > tMax) return false;
        }

        // Y slab
        if (Math.abs(dy) < 1e-12) {
            if (a.yCoord < bb.minY || a.yCoord > bb.maxY) return false;
        } else {
            double inv = 1.0 / dy;
            double t1 = (bb.minY - a.yCoord) * inv;
            double t2 = (bb.maxY - a.yCoord) * inv;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMin > tMax) return false;
        }

        // Z slab
        if (Math.abs(dz) < 1e-12) {
            return !(a.zCoord < bb.minZ) && !(a.zCoord > bb.maxZ);
        } else {
            double inv = 1.0 / dz;
            double t1 = (bb.minZ - a.zCoord) * inv;
            double t2 = (bb.maxZ - a.zCoord) * inv;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            return !(tMin > tMax);
        }
    }

    private boolean tryPromoteCandidate(EntityArmorStand s, CandidateStand c) {
        ItemStack main = s.getEquipmentInSlot(0);
        if (main == null || main.getItem() != Items.bone) return false;

        final int id = s.getEntityId();
        if (tracked.containsKey(id)) return true;

        boolean owner = c.ownerLocal;
        double distSq = -1.0;
        double rayDot = -2.0;
        if (owner && throwForward != null && throwOrigin != null) {
            Vec3 head = getStandHeadPos(s);
            double sx = head.xCoord - throwOrigin.xCoord;
            double sy = head.yCoord - throwOrigin.yCoord;
            double sz = head.zCoord - throwOrigin.zCoord;
            distSq = sx*sx + sy*sy + sz*sz;

            if (distSq > OWNER_PROMOTE_DIST_HARD_SQ) {
                owner = false;
            } else {
                final double len = Math.sqrt(distSq);
                if (len > 1e-6) {
                    rayDot =
                            throwForward.xCoord * (sx / len)
                                    + throwForward.yCoord * (sy / len)
                                    + throwForward.zCoord * (sz / len);
                    if (rayDot < FWD_RAY_DOT_MIN) owner = false;
                } else {
                    owner = false;
                }
            }
        }

        claimStand(s, owner);
        return true;
    }

    private void claimStand(EntityArmorStand stand, boolean ownerLocal) {
        for (BoneStand existing : tracked.values()) {
            if (existing.throwSeq == currentThrowSeq) return;
        }

        Vec3 originSnap = throwOrigin;
        Vec3 forwardSnap = throwForward;

        BoneStand bs = new BoneStand(
                stand.getEntityId(),
                ownerLocal,
                getStandHeadPos(stand),
                currentThrowSeq,
                originSnap,
                forwardSnap
        );
        tracked.put(stand.getEntityId(), bs);
    }

    private void detectHits(BoneStand bs, Vec3 prev, Vec3 curr, List<EntityLivingBase> localLiving, double px, double py, double pz) {
        if (prev == null || curr == null || localLiving == null || localLiving.isEmpty()) return;

        AxisAlignedBB pathBox = new AxisAlignedBB(
                Math.min(prev.xCoord, curr.xCoord), Math.min(prev.yCoord, curr.yCoord), Math.min(prev.zCoord, curr.zCoord),
                Math.max(prev.xCoord, curr.xCoord), Math.max(prev.yCoord, curr.yCoord), Math.max(prev.zCoord, curr.zCoord))
                .expand(NEARBY_ENTITY_CHECK_RADIUS, NEARBY_ENTITY_CHECK_RADIUS, NEARBY_ENTITY_CHECK_RADIUS);

        final double vx = curr.xCoord - prev.xCoord;
        final double vy = curr.yCoord - prev.yCoord;
        final double vz = curr.zCoord - prev.zCoord;
        double v2 = vx*vx + vy*vy + vz*vz;
        if (v2 < 1.0e-9) v2 = 1.0e-9;

        final double fPrev = vx*(prev.xCoord - px) + vy*(prev.yCoord - py) + vz*(prev.zCoord - pz);
        final double fCurr = vx*(curr.xCoord - px) + vy*(curr.yCoord - py) + vz*(curr.zCoord - pz);

        boolean crossedTurnThisTick = (fPrev > 0.0) && (fCurr < 0.0);
        double tStar = -fPrev / v2;
        boolean tIn = (tStar >= 0.0 && tStar <= 1.0);
        if (!tIn) crossedTurnThisTick = false;

        final double tx = prev.xCoord + vx * tStar;
        final double ty = prev.yCoord + vy * tStar;
        final double tz = prev.zCoord + vz * tStar;

        for (EntityLivingBase target : localLiving) {
            if (target.isDead || target.isInvisible()) continue;
            final int tid = target.getEntityId();

            HitState hs = bs.hitsByTarget.get(tid);
            if (hs == null) {
                if (bs.hitsByTarget.size() >= MAX_TARGETS_PER_BONE) continue;
                hs = new HitState();
                bs.hitsByTarget.put(tid, hs);
            } else if (hs.backDone) continue;

            AxisAlignedBB mobBox = target.getEntityBoundingBox();
            if (!mobBox.intersectsWith(pathBox)) continue;

            AxisAlignedBB expanded = mobBox.expand(HEAD_HALF_XZ, HEAD_HALF_Y, HEAD_HALF_XZ);

            boolean gotFront = false, gotBack = false;

            if (crossedTurnThisTick) {
                if (!hs.frontDone || !hs.backDone) {
                    boolean check = tx >= expanded.minX && tx <= expanded.maxX
                            && ty >= expanded.minY && ty <= expanded.maxY
                            && tz >= expanded.minZ && tz <= expanded.maxZ;
                    Vec3 turn = new Vec3(tx, ty, tz);

                    if (!hs.frontDone) {
                        boolean hit = segmentIntersectsAABB(expanded, prev, turn)
                                || expanded.isVecInside(prev)
                                || check;
                        if (hit) gotFront = true;
                    }
                    if (!hs.backDone) {
                        boolean hit = segmentIntersectsAABB(expanded, turn, curr)
                                || expanded.isVecInside(curr)
                                || check;
                        if (hit) gotBack = true;
                    }
                    if (bs.phase == Phase.RETURNING) gotFront = false;
                }
            } else {
                boolean segmentHits =
                        segmentIntersectsAABB(expanded, prev, curr)
                                || expanded.isVecInside(prev)
                                || expanded.isVecInside(curr);
                Phase effective = bs.hasTurned ? Phase.RETURNING : Phase.OUTBOUND;
                if (segmentHits) {
                    if (effective == Phase.OUTBOUND && !hs.frontDone) gotFront = true;
                    else if (effective == Phase.RETURNING && !hs.backDone) gotBack = true;
                }
            }

            if (gotFront && !hs.frontDone) {
                hs.frontDone = true;
                KICEventBus.post(new BoneHitEvent(target, BoneHitEvent.Hit.FRONT));
            }
            if (gotBack && !hs.backDone) {
                hs.backDone = true;
                KICEventBus.post(new BoneHitEvent(target, BoneHitEvent.Hit.BACK));
            }
        }
    }

    private Vec3 getStandHeadPos(EntityArmorStand s) {
        double eye = s.isSmall() ? MARKER_HEAD_Y_SMALL : MARKER_HEAD_Y_BIG;
        return new Vec3(s.posX, s.posY + eye, s.posZ);
    }

    private Vec3 getLookVec(EntityPlayerSP p) {
        float yaw = p.rotationYaw;
        float pitch = p.rotationPitch;
        float cy = (float) Math.cos(-yaw * 0.017453292F - Math.PI);
        float sy = (float) Math.sin(-yaw * 0.017453292F - Math.PI);
        float cp = (float) Math.cos(-pitch * 0.017453292F);
        float sp = (float) Math.sin(-pitch * 0.017453292F);
        return new Vec3(sy * cp, sp, cy * cp);
    }

    @SubscribeEvent
    public void onBoneHit(BoneHitEvent e) {
        if (!KICConfig.backboneOnIsland || LocationUtils.currentIsland != Island.PRIVATE_ISLAND) return;

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
                            " §7Back bone hit" +
                            " §r| §7Holding - " + holding +
                            " §r| §7Wearing - " + wearing
            );
        } else if (e.getType() == BoneHitEvent.Hit.FRONT && KICConfig.backboneOnIslandFront) {
            sendMessageToPlayer(
                    KICPrefix +
                            " §7Front bone hit" +
                            " §r| §7Holding - " + holding +
                            " §r| §7Wearing - " + wearing
            );
        }
    }

    private enum Phase { UNKNOWN, OUTBOUND, RETURNING }

    private static class HitState {
        boolean frontDone = false;
        boolean backDone  = false;
    }

    public static class BoneStand {
        final int entityId;
        final boolean ownerLocal;
        final int throwSeq;

        final Vec3 origin;

        Vec3 forwardUsed;
        boolean forwardFixed = false;

        Phase phase = Phase.UNKNOWN;
        int ticksAlive = 0;

        Vec3 prevHeadPos;
        Vec3 currHeadPos;

        double prevAlong = Double.NaN;
        boolean sawOutbound = false;

        boolean hasTurned = false;
        final Map<Integer, HitState> hitsByTarget = new HashMap<>();

        double minX, minY, minZ, maxX, maxY, maxZ;

        BoneStand(int id, boolean ownerLocal, Vec3 firstHeadPos, int throwSeq, Vec3 origin, Vec3 forward) {
            this.entityId = id;
            this.ownerLocal = ownerLocal;
            this.prevHeadPos = firstHeadPos;
            this.currHeadPos = firstHeadPos;
            this.throwSeq = throwSeq;
            this.origin = origin;
            this.forwardUsed = forward;
        }

        void updateHeadBox(Vec3 head) {
            this.minX = head.xCoord - BoneTracker.NEARBY_ENTITY_CHECK_RADIUS;
            this.minY = head.yCoord - BoneTracker.NEARBY_ENTITY_CHECK_RADIUS;
            this.minZ = head.zCoord - BoneTracker.NEARBY_ENTITY_CHECK_RADIUS;
            this.maxX = head.xCoord + BoneTracker.NEARBY_ENTITY_CHECK_RADIUS;
            this.maxY = head.yCoord + BoneTracker.NEARBY_ENTITY_CHECK_RADIUS;
            this.maxZ = head.zCoord + BoneTracker.NEARBY_ENTITY_CHECK_RADIUS;
        }

        AxisAlignedBB toAABB() {
            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static class CandidateStand {
        final int entityId;
        final boolean ownerLocal;
        int ageTicks = 0;

        CandidateStand(int id, boolean ownerLocal) {
            this.entityId = id;
            this.ownerLocal = ownerLocal;
        }
    }
}
