package someoneok.kic.modules.kuudra;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.dev.KICLogger;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.utils.ChatUtils.sendCommand;

public class HollowHelper {
    private static final int ICHOR_RADIUS = 8;
    private static final long POOL_LIFETIME_MS = 20_000L;

    private static final float LINE_WIDTH = 10.0f;

    private int cachedSegments = -1;
    private double[] unitX;
    private double[] unitZ;

    public boolean poolActive = false;
    public long lastPool = 0;

    public int x = 0;
    public int y = 0;
    public int z = 0;

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!KICConfig.ichorPool
                || !LocationUtils.inKuudra()
                || !ApiUtils.isVerified()
                || phase() != KuudraPhase.KILL) return;

        String msg = event.message.getUnformattedText();
        if (msg == null) return;

        if (msg.contains("Casting Spell: Ichor Pool!")) {
            BlockPos pos = mc.thePlayer != null ? mc.thePlayer.getPosition() : null;
            if (pos != null) sendCommand("/pc [KIC] Casting Ichor Pool at " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
            return;
        }

        if (msg.contains("[KIC] Casting Ichor Pool at ") || msg.contains("[HH] Casting Ichor Pool at ")) {
            int[] coords = tryParseLast3Ints(msg);
            if (coords == null) {
                KICLogger.error("Error while parsing Ichor Pool coords from: " + msg);
                return;
            }

            poolActive = true;
            lastPool = System.currentTimeMillis();
            x = coords[0];
            y = coords[1];
            z = coords[2];
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!KICConfig.ichorPool
                || !poolActive
                || !LocationUtils.inKuudra()
                || !ApiUtils.isVerified()
                || phase() != KuudraPhase.KILL) return;

        if (mc.thePlayer == null || mc.theWorld == null) return;

        long now = System.currentTimeMillis();
        long since = now - lastPool;

        if (since >= POOL_LIFETIME_MS) {
            poolActive = false;
            return;
        }

        float secondsLeft = (POOL_LIFETIME_MS - since) / 1000.0f;
        if (secondsLeft < 0f) secondsLeft = 0f;
        if (secondsLeft > 20f) secondsLeft = 20f;

        float t = secondsLeft / 20.0f;
        float r = 1.0f - t;
        float g = t;
        float b = 0.0f;

        renderCircleBatched(x + 0.5, y + 0.02, z + 0.5, ICHOR_RADIUS, event.partialTicks, r, g, b, 1.0f);
    }

    private void renderCircleBatched(double centerX, double centerY, double centerZ,
                                           double radius, float partialTicks,
                                           float r, float g, float b, float a) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        ensureCircleCache();

        double cameraX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double cameraY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double cameraZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        final double baseX = centerX - cameraX;
        final double baseY = centerY - cameraY;
        final double baseZ = centerZ - cameraZ;

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                1, 0
        );

        GL11.glLineWidth(LINE_WIDTH);
        GlStateManager.color(r, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();

        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        for (int i = 0; i < cachedSegments; i++) {
            buffer.pos(
                    baseX + radius * unitX[i],
                    baseY,
                    baseZ + radius * unitZ[i]
            ).endVertex();
        }
        buffer.pos(
                baseX + radius * unitX[0],
                baseY,
                baseZ + radius * unitZ[0]
        ).endVertex();

        tessellator.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    private void ensureCircleCache() {
        int segments = KICConfig.ichorCircleQuality;
        if (segments < 8) segments = 8;
        if (segments > 256) segments = 256;

        if (segments == cachedSegments && unitX != null && unitZ != null) {
            return;
        }

        cachedSegments = segments;
        unitX = new double[segments];
        unitZ = new double[segments];

        double step = (2.0 * Math.PI) / segments;
        for (int i = 0; i < segments; i++) {
            double ang = step * i;
            unitX[i] = Math.cos(ang);
            unitZ[i] = Math.sin(ang);
        }
    }

    private static int[] tryParseLast3Ints(String message) {
        if (message == null) return null;

        final String marker = "Casting Ichor Pool at ";
        int idx = message.indexOf(marker);
        if (idx == -1) return null;

        String coordPart = message.substring(idx + marker.length()).trim();
        String[] parts = coordPart.split("\\s+");
        if (parts.length != 3) return null;

        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            return new int[]{x, y, z};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
