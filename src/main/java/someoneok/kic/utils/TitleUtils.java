package someoneok.kic.utils;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static someoneok.kic.KIC.mc;

@SideOnly(Side.CLIENT)
public class TitleUtils {
    private final static int FADE_IN = 5;
    private final static int FADE_OUT = 5;

    private static String title = null;
    private static int titleTicks = 0;
    private static int titleElapsed = -1;

    private static String subtitle = null;
    private static int subtitleTicks = 0;
    private static int subtitleElapsed = -1;

    public static void showTitle(String text, int totalTicks) {
        title = text;
        titleTicks = Math.max(0, totalTicks - FADE_IN - FADE_OUT);
        titleElapsed = 0;
    }

    public static void showSubtitle(String text, int totalTicks) {
        subtitle = text;
        subtitleTicks = Math.max(0, totalTicks - FADE_IN - FADE_OUT);
        subtitleElapsed = 0;
    }

    public static void showTitleAndSubTitle(String titleText, String subtitleText, int totalTicks) {
        title = titleText;
        subtitle = subtitleText;

        int ticks = Math.max(0, totalTicks - FADE_IN - FADE_OUT);
        titleTicks = ticks;
        subtitleTicks = ticks;

        titleElapsed = 0;
        subtitleElapsed = 0;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        renderTitles();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (title != null) {
            titleElapsed++;
            int total = FADE_IN + titleTicks + FADE_OUT;
            if (titleElapsed > total) {
                title = null;
                titleElapsed = -1;
            }
        }

        if (subtitle != null) {
            subtitleElapsed++;
            int total = FADE_IN + subtitleTicks + FADE_OUT;
            if (subtitleElapsed > total) {
                subtitle = null;
                subtitleElapsed = -1;
            }
        }
    }

    private void renderTitles() {
        if (mc.theWorld == null) return;

        ScaledResolution res = new ScaledResolution(mc);
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();

        if (title != null && titleElapsed >= 0) {
            int alpha = computeAlpha(titleElapsed, titleTicks);
            if (alpha > 0) {
                float scale = 4f;
                int stringWidth = mc.fontRendererObj.getStringWidth(title);
                if (stringWidth * scale > w * 0.9f) {
                    scale = w * 0.9f / stringWidth;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(w / 2f, h / 2f, 0.0f);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.color(1.0f, 1.0f, 1.0f, alpha / 255.0f);
                mc.fontRendererObj.drawString(title, -stringWidth / 2f, -20.0f, (alpha << 24) | 0xFFFFFF, true);
                GlStateManager.popMatrix();
                GlStateManager.color(1f, 1f, 1f, 1f);
            }
        }
        if (subtitle != null && subtitleElapsed >= 0) {
            int alpha = computeAlpha(subtitleElapsed, subtitleTicks);
            if (alpha > 0) {
                float scale = 2f;
                int stringWidth = mc.fontRendererObj.getStringWidth(subtitle);
                if (stringWidth * scale > w * 0.9f) {
                    scale = w * 0.9f / stringWidth;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(w / 2f, h / 2f, 0.0f);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.color(1.0f, 1.0f, 1.0f, alpha / 255.0f);
                mc.fontRendererObj.drawString(subtitle, -stringWidth / 2f, -23.0f, (alpha << 24) | 0xFFFFFF, true);
                GlStateManager.popMatrix();
                GlStateManager.color(1f, 1f, 1f, 1f);
            }
        }
    }

    private int computeAlpha(int elapsed, int visibleTicks) {
        int total = FADE_IN + visibleTicks + FADE_OUT;
        if (elapsed < FADE_IN) {
            return (int) ((elapsed / (float) FADE_IN) * 255);
        } else if (elapsed < FADE_IN + visibleTicks) {
            return 255;
        } else if (elapsed < total) {
            return (int) (((total - elapsed) / (float) FADE_OUT) * 255);
        }
        return 0;
    }
}
