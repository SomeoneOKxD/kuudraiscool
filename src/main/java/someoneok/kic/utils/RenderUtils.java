package someoneok.kic.utils;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Slot;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import someoneok.kic.utils.dev.KICLogger;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static someoneok.kic.KIC.mc;

public class RenderUtils {
    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();

    private static final ResourceLocation BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");
    public static MethodHandle xSizeField = ReflectionUtil.getField(GuiContainer.class, "xSize", "field_146999_f", "f");
    public static MethodHandle ySizeField = ReflectionUtil.getField(GuiContainer.class, "ySize", "field_147000_g", "g");

    public static void highlight(Color color, GuiContainer gui, Slot slot) {
        highlight(color, null, gui, slot);
    }

    public static void highlight(Color color1, Color color2, GuiContainer gui, Slot slot) {
        try {
            int guiTop = (gui.height - (int) ySizeField.invokeExact(gui)) / 2;
            int guiLeft = (gui.width - (int) xSizeField.invokeExact(gui)) / 2;
            int slotX = slot.xDisplayPosition + guiLeft;
            int slotY = slot.yDisplayPosition + guiTop;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 0.1);

            if (color2 == null) {
                Gui.drawRect(slotX, slotY, slotX + 16, slotY + 16, color1.getRGB());
            } else {
                Gui.drawRect(slotX, slotY, slotX + 8, slotY + 16, color1.getRGB());
                Gui.drawRect(slotX + 8, slotY, slotX + 16, slotY + 16, color2.getRGB());
            }

            GlStateManager.popMatrix();
        } catch (Throwable e) {
            KICLogger.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void drawPixelBox(final Vec3 vec, final Color color, final double size, boolean filled, float partialTicks) {
        Entity viewer = mc.getRenderViewEntity();

        final double halfSize = size / 2.0;
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        final double x = vec.xCoord - viewerX - halfSize;
        final double y = vec.yCoord - viewerY - halfSize;
        final double z = vec.zCoord - viewerZ - halfSize;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + size, y + size, z + size);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), 35);
        if (filled) {
            drawFilledBox(axisAlignedBB);
        }

        glLineWidth(2F);
        enableGlCap(GL_LINE_SMOOTH);
        glColor(color);

        drawSelectionBoundingBox(axisAlignedBB);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

    public static void drawBox(final AxisAlignedBB axis, final Color color, boolean filled, boolean esp, float partialTicks) {
        Entity viewer = mc.getRenderViewEntity();

        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        AxisAlignedBB renderBB = axis.offset(-viewerX, -viewerY, -viewerZ);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D);

        if (esp) {
            disableGlCap(GL_DEPTH_TEST);
            glDepthMask(false);
        }

        glColor(color.getRed(), color.getGreen(), color.getBlue(), 35);
        if (filled) {
            drawFilledBox(renderBB);
        }

        glLineWidth(2F);
        enableGlCap(GL_LINE_SMOOTH);
        glColor(color);
        drawSelectionBoundingBox(renderBB);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (esp) glDepthMask(true);
        resetCaps();
    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();

        tessellator.draw();
    }

    public static void drawEntityBox(final Entity entity, final Color color, final int width, float partialTicks) {
        if (entity == null || width == 0) return;
        final RenderManager renderManager = mc.getRenderManager();

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
                - renderManager.viewerPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
                - renderManager.viewerPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
                - renderManager.viewerPosZ;

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05D,
                entityBox.maxX - entity.posX + x + 0.05D,
                entityBox.maxY - entity.posY + y + 0.15D,
                entityBox.maxZ - entity.posZ + z + 0.05D
        );

        glLineWidth((float) width);
        enableGlCap(GL_LINE_SMOOTH);
        glColor(color.getRed(), color.getGreen(), color.getBlue(), 95);
        drawSelectionBoundingBox(axisAlignedBB);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), 26);
        //drawFilledBox(axisAlignedBB);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawRect(final int x, final int y, final int x2, final int y2, final int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color);
        glBegin(GL_QUADS);

        glVertex2i(x2, y);
        glVertex2i(x, y);
        glVertex2i(x, y2);
        glVertex2i(x2, y2);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void glColor(final Color color) {
        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private static void glColor(final int hex) {
        glColor(hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF, hex >> 24 & 0xFF);
    }

    public static void renderNameTag(final String string, final double x, final double y, final double z, final float scale, final boolean background) {
        final RenderManager renderManager = mc.getRenderManager();

        glPushMatrix();
        glTranslated(x - renderManager.viewerPosX, y - renderManager.viewerPosY, z - renderManager.viewerPosZ);
        glNormal3f(0F, 1F, 0F);
        glRotatef(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        glRotatef(mc.getRenderManager().playerViewX, 1F, 0F, 0F);
        glScalef(-0.05F * scale, -0.05F * scale, 0.05F * scale); // Apply the scale factor
        setGlCap(GL_LIGHTING, false);
        setGlCap(GL_DEPTH_TEST, false);
        setGlCap(GL_BLEND, true);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        final int width = mc.fontRendererObj.getStringWidth(string) / 2;

        if (background) {
            drawRect(-width - 1, -1, width + 1, mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);
        }
        mc.fontRendererObj.drawString(string, -width, 0.3F, Color.WHITE.getRGB(), true);

        resetCaps();
        glColor4f(1F, 1F, 1F, 1F);
        glPopMatrix();
    }

    public static void resetCaps() {
        glCapMap.forEach(RenderUtils::setGlState);
    }

    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }

    public static void drawBeaconBeam(Vec3 vec3, Color color, int height, boolean glow, float partialTicks) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;
        drawBeaconBeam(vec3, r, g, b, a, height, glow, partialTicks);
    }

    public static void drawBeaconBeam(Vec3 vec3, float red, float green, float blue, float alpha, int height, boolean glow, float partialTicks) {
        final RenderManager renderManager = mc.getRenderManager();
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        final int bottomOffset = 0;
        final int topOffset = bottomOffset + height;

        double x = vec3.xCoord - renderManager.viewerPosX;
        double y = vec3.yCoord - renderManager.viewerPosY;
        double z = vec3.zCoord - renderManager.viewerPosZ;

        mc.getTextureManager().bindTexture(BEACON_BEAM);

        GlStateManager.pushMatrix();
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0f);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0f);

        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, 1, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        double time = mc.theWorld.getTotalWorldTime() + partialTicks;
        double d1 = MathHelper.func_181162_h(-time * 0.2 - Math.floor(-time * 0.1));

        double d2 = time * 0.025 * -1.5;
        double d4 = Math.cos(d2 + 2.356194490192345) * 0.2;
        double d5 = Math.sin(d2 + 2.356194490192345) * 0.2;
        double d6 = Math.cos(d2 + (Math.PI / 4.0)) * 0.2;
        double d7 = Math.sin(d2 + (Math.PI / 4.0)) * 0.2;
        double d8 = Math.cos(d2 + 3.9269908169872414) * 0.2;
        double d9 = Math.sin(d2 + 3.9269908169872414) * 0.2;
        double d10 = Math.cos(d2 + 5.497787143782138) * 0.2;
        double d11 = Math.sin(d2 + 5.497787143782138) * 0.2;

        double d14 = -1.0 + d1;
        double d15 = height * 2.5 + d14;

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        worldRenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0, d15).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0, d15).color(red, green, blue, alpha).endVertex();

        worldRenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0, d15).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0, d15).color(red, green, blue, alpha).endVertex();

        worldRenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0, d15).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0, d15).color(red, green, blue, alpha).endVertex();

        worldRenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0, d15).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0, d14).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0, d15).color(red, green, blue, alpha).endVertex();

        tessellator.draw();

        GlStateManager.disableCull();

        if (glow) {
            double d12 = -1.0 + d1;
            double d13 = height + d12;

            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            float glowAlpha = 0.25f;
            double glowRadius = 0.2;

            worldRenderer.pos(x - glowRadius, y + topOffset, z - glowRadius).tex(1.0, d13).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x - glowRadius, y + bottomOffset, z - glowRadius).tex(1.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x + glowRadius, y + bottomOffset, z - glowRadius).tex(0.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x + glowRadius, y + topOffset, z - glowRadius).tex(0.0, d13).color(red, green, blue, glowAlpha).endVertex();

            worldRenderer.pos(x + glowRadius, y + topOffset, z + glowRadius).tex(1.0, d13).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x + glowRadius, y + bottomOffset, z + glowRadius).tex(1.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x - glowRadius, y + bottomOffset, z + glowRadius).tex(0.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x - glowRadius, y + topOffset, z + glowRadius).tex(0.0, d13).color(red, green, blue, glowAlpha).endVertex();

            worldRenderer.pos(x + glowRadius, y + topOffset, z - glowRadius).tex(1.0, d13).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x + glowRadius, y + bottomOffset, z - glowRadius).tex(1.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x + glowRadius, y + bottomOffset, z + glowRadius).tex(0.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x + glowRadius, y + topOffset, z + glowRadius).tex(0.0, d13).color(red, green, blue, glowAlpha).endVertex();

            worldRenderer.pos(x - glowRadius, y + topOffset, z + glowRadius).tex(1.0, d13).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x - glowRadius, y + bottomOffset, z + glowRadius).tex(1.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x - glowRadius, y + bottomOffset, z - glowRadius).tex(0.0, d12).color(red, green, blue, glowAlpha).endVertex();
            worldRenderer.pos(x - glowRadius, y + topOffset, z - glowRadius).tex(0.0, d13).color(red, green, blue, glowAlpha).endVertex();

            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }
}
