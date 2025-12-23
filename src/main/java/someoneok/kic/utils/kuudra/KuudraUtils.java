package someoneok.kic.utils.kuudra;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.Supply;
import someoneok.kic.models.kuudra.SupplyStatus;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.models.kuudra.SupplySpot.*;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phaseOrdinal;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class KuudraUtils {
    private static EntityArmorStand elleEntity;
    private static EntityMagmaCube kuudraEntity;

    private static final List<EntityMagmaCube> magmaCubes = new ArrayList<>();
    private static final List<EntityZombie> zombies = new ArrayList<>();
    private static final List<Vec3> crates = new ArrayList<>();
    private static final Supply[] supplies = {
            new Supply(SUPPLY1),
            new Supply(SUPPLY2),
            new Supply(SUPPLY3),
            new Supply(SUPPLY4),
            new Supply(SUPPLY5),
            new Supply(SUPPLY6),
    };

    private boolean dirty = false;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (dirty) {
            dirty = false;
            elleEntity = null;
            kuudraEntity = null;
            magmaCubes.clear();
            zombies.clear();
            crates.clear();
            resetSupplies();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;
        if (!ApiUtils.isVerified() || !LocationUtils.inKuudra()) return;

        final KuudraPhase currentPhase = phase();
        final int phaseOrdinal = phaseOrdinal();
        final boolean earlyPhase = phaseOrdinal <= 3;
        final boolean hitboxesEnabled = KICConfig.supplyBox && currentPhase == KuudraPhase.SUPPLIES;

        magmaCubes.clear();
        zombies.clear();
        crates.clear();
        dirty = true;

        List<Entity> list = mc.theWorld.loadedEntityList;
        for (int i = 0, size = list.size(); i < size; i++) processEntity(list.get(i), earlyPhase, hitboxesEnabled);
    }

    private void processEntity(Entity entity,
                               boolean earlyPhase,
                               boolean hitboxesEnabled) {
        if (entity instanceof EntityArmorStand) {
            String name = entity.getName();
            if (isNullOrEmpty(name)) return;

            name = removeFormatting(name);

            if ("Elle".equals(name)) {
                elleEntity = (EntityArmorStand) entity;
                return;
            }

            if (earlyPhase) {
                final int x = (int) entity.posX;
                final int z = (int) entity.posZ;
                if (x == SUPPLY1.getX() && z == SUPPLY1.getZ())      processSupply(0, name);
                else if (x == SUPPLY2.getX() && z == SUPPLY2.getZ()) processSupply(1, name);
                else if (x == SUPPLY3.getX() && z == SUPPLY3.getZ()) processSupply(2, name);
                else if (x == SUPPLY4.getX() && z == SUPPLY4.getZ()) processSupply(3, name);
                else if (x == SUPPLY5.getX() && z == SUPPLY5.getZ()) processSupply(4, name);
                else if (x == SUPPLY6.getX() && z == SUPPLY6.getZ()) processSupply(5, name);
            }
        } else if (entity instanceof EntityMagmaCube) {
            EntityMagmaCube magmaCube = (EntityMagmaCube) entity;
            magmaCubes.add(magmaCube);
            if (isKuudraEntity(magmaCube)) kuudraEntity = magmaCube;
        } else if (earlyPhase && entity instanceof EntityGiantZombie) {
            if (entity.posY < 67) {
                final float yawRad = (float) ((entity.rotationYaw + 130.0f) * (Math.PI / 180.0));
                final double offsetX = 3.7 * Math.cos(yawRad);
                final double offsetZ = 3.7 * Math.sin(yawRad);
                final double x = entity.posX + 0.5 + offsetX;
                final double z = entity.posZ + 0.5 + offsetZ;
                crates.add(new Vec3(x, 75, z));
            }
        } else if (hitboxesEnabled && entity instanceof EntityZombie) {
            final double y = entity.posY;
            if (entity.isInvisible() && y <= 78 && y >= 72) {
                zombies.add((EntityZombie) entity);
            }
        }
    }

    private void processSupply(int pos, String name) {
        SupplyStatus status = getStatusFromName(name);
        if (status == null) return;
        supplies[pos].setStatus(status);

        if (status == SupplyStatus.INPROGRESS && name.startsWith("PROGRESS:")) {
            try {
                String percentStr = name.substring("PROGRESS:".length()).replace("%", "").trim();
                float percent = Float.parseFloat(percentStr);
                supplies[pos].setProgressColor(getProgressColor(percent));
            } catch (NumberFormatException ignored) {}
        }
    }

    private SupplyStatus getStatusFromName(String name) {
        if (name.contains("BRING SUPPLY CHEST HERE")) return SupplyStatus.NOTHING;
        if (name.contains("SUPPLIES RECEIVED")) return SupplyStatus.RECEIVED;
        if (name.contains("PROGRESS: ")) return name.contains("COMPLETE") ? SupplyStatus.COMPLETED : SupplyStatus.INPROGRESS;
        return null;
    }

    private float[] getProgressColor(float percent) {
        percent = Math.max(0, Math.min(100, percent));
        float ratio = (float) Math.pow(percent / 100f, 1.5f);
        float red = 1.0f - ratio;
        return new float[] { red, ratio};
    }

    private boolean isKuudraEntity(EntityMagmaCube magmaCube) {
        return magmaCube.width > 14 && magmaCube.getHealth() <= 100_000;
    }

    private void resetSupplies() {
        for (Supply supply : supplies) supply.reset();
    }

    public static EntityArmorStand getElle() { return elleEntity; }
    public static EntityMagmaCube getKuudra() { return kuudraEntity; }

    public static List<Vec3> getCrates() { return crates; }
    public static List<EntityMagmaCube> getMagmaCubes() { return magmaCubes; }
    public static List<EntityZombie> getZombies() { return zombies; }
    public static Supply[] getSupplies() { return supplies; }

    public static List<Vec3> getAllUncompletedSupplies() {
        List<Vec3> uncompleted = new ArrayList<>();
        for (Supply supply : supplies) {
            if (supply.getStatus() == SupplyStatus.NOTHING) uncompleted.add(supply.getSpot().getLocation());
        }
        return uncompleted;
    }
}
