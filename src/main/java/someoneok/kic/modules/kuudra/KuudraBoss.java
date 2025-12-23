package someoneok.kic.modules.kuudra;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.RenderUtils;
import someoneok.kic.utils.kuudra.KuudraUtils;
import someoneok.kic.utils.overlay.OverlayUtils;

import java.awt.*;
import java.text.DecimalFormat;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phaseOrdinal;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;

public class KuudraBoss {
    private static final DecimalFormat healthFormatter = new DecimalFormat("###,###");
    private static final DecimalFormat percentFormatter = new DecimalFormat("##.##");

    private String bossHPMessage = null;
    private float bossHPScale = 0;
    private Dir lastDir = Dir.NONE;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if (!KICConfig.showKuudraBossBar
                || !LocationUtils.inKuudra()
                || event.type != RenderGameOverlayEvent.ElementType.TEXT
                || isNullOrEmpty(bossHPMessage)
                || !ApiUtils.isVerified()) return;

        KuudraPhase currentPhase = phase();
        if (currentPhase == KuudraPhase.NONE || currentPhase == KuudraPhase.END) return;

        ScaledResolution res = new ScaledResolution(mc);
        int screenWidth = res.getScaledWidth();
        int gameWidth = mc.displayWidth / res.getScaleFactor();
        int offsetX = (screenWidth - gameWidth) / 2;
        int x = offsetX + gameWidth / 2;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, 10, 400);
        OverlayUtils.drawString(0, 0, bossHPMessage, OverlayUtils.TextStyle.Shadow, OverlayUtils.Alignment.Center);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null
                || mc.theWorld == null
                || !LocationUtils.inKuudra()
                || !ApiUtils.isVerified()) return;
        KuudraPhase currentPhase = phase();
        if (currentPhase == KuudraPhase.NONE || currentPhase == KuudraPhase.END) return;

        EntityMagmaCube boss = KuudraUtils.getKuudra();
        if (boss == null) return;

        if (KICConfig.showKuudraBossBar) updateBossBar();
        if (KICConfig.showKuudraHealth)  renderBossHealthTag(boss);
        if (KICConfig.showKuudraBossBar) updateBossBarValues(boss);
        if (KICConfig.showKuudraOutline) RenderUtils.drawEntityBox(boss, Color.GREEN, 3, event.partialTicks);
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        bossHPMessage = null;
        bossHPScale = 0;
        lastDir = Dir.NONE;
        healthFormatter.applyPattern("###,###");
        percentFormatter.applyPattern("##.##");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !KICConfig.kuudraDirection
                || !LocationUtils.inKuudra()
                || !ApiUtils.isVerified()) return;

        EntityMagmaCube boss = KuudraUtils.getKuudra();
        if (boss == null) return;

        float hp = boss.getHealth();
        if (hp <= 25_000 && hp >= 24_900) {
            Vec3 pos = boss.getPositionVector();
            if (pos.xCoord < -128 && lastDir != Dir.RIGHT) {
                lastDir = Dir.RIGHT;
                Notifications.showMessage(lastDir.display, 25);
            } else if (pos.xCoord > -72 && lastDir != Dir.LEFT) {
                lastDir = Dir.LEFT;
                Notifications.showMessage(lastDir.display, 25);
            } else if (pos.zCoord > -84 && lastDir != Dir.FRONT) {
                lastDir = Dir.FRONT;
                Notifications.showMessage(lastDir.display, 25);
            } else if (pos.zCoord < -132 && lastDir != Dir.BACK) {
                lastDir = Dir.BACK;
                Notifications.showMessage(lastDir.display, 25);
            }
        }
    }

    private void updateBossBarValues(EntityMagmaCube boss) {
        float health = boss.getHealth();
        String hpColor = "§c";
        float scale;

        if (LocationUtils.kuudraTier() == 5) {
            if (phaseOrdinal() >= KuudraPhase.KILL.ordinal()) {
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

    private void renderBossHealthTag(EntityMagmaCube boss) {
        float health = boss.getHealth();
        String tag = "§c" + healthFormatter.format(health) + "/100,000";
        if (LocationUtils.kuudraTier() == 5) {
            tag = phaseOrdinal() >= KuudraPhase.KILL.ordinal()
                    ? "§e" + parseToShorthandNumber(health * 9600) + "/240M"
                    : "§c" + healthFormatter.format((health - 25000) / 3 * 4) + "/100,000";
        }
        RenderUtils.renderNameTag(tag, boss.posX, boss.posY + boss.height / 2, boss.posZ, 4.0f, true);
    }

    private enum Dir {
        RIGHT("§c§lRIGHT!"),
        LEFT("§a§lLEFT!"),
        FRONT("§2§lFRONT!"),
        BACK("§4§lBACK!"),
        NONE("");
        private final String display;
        Dir(String display) { this.display = display; }
    }
}
