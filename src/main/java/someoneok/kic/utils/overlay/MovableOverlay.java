package someoneok.kic.utils.overlay;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.Island;
import someoneok.kic.utils.ApiUtils;

import java.util.Set;
import java.util.function.Supplier;

import static someoneok.kic.KIC.mc;

public class MovableOverlay extends OverlayBase {
    protected boolean dragging = false;
    protected boolean hovered = false;
    protected int dragX, dragY;

    public MovableOverlay(Supplier<Boolean> configOption, String name, String displayName, Set<Island> islands, Supplier<Boolean> renderCondition, OverlayType type, String exampleText) {
        super(configOption, name, displayName, islands, renderCondition, type, exampleText);
    }

    public void startDragging(int mouseX, int mouseY) {
        dragging = true;
        dragX = mouseX - x;
        dragY = mouseY - y;
    }

    public void stopDragging() {
        dragging = false;
    }

    public void onMouseDragged(int mouseX, int mouseY) {
        if (dragging) {
            this.x = mouseX - dragX;
            this.y = mouseY - dragY;
        }
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    @Override
    public void render() {
        if (!ApiUtils.isVerified() || !shouldRender()) return;

        FontRenderer fontRenderer = mc.fontRendererObj;
        String[] lines = (editing ? exampleText : text).split("\n");

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0);
        GL11.glScaled(scale, scale, 1.0);

        int bgWidth = width + padding * 2;
        int bgHeight = height + padding * 2;

        if (KICConfig.drawHudBackground) {
            Gui.drawRect(0, 0, bgWidth, bgHeight, 0x80000000);
        }

        for (int i = 0; i < lines.length; i++) {
            fontRenderer.drawString(lines[i], padding, padding + (i * (fontRenderer.FONT_HEIGHT + 3)), 0xFFFFFF, KICConfig.useTextShadowInHuds);
        }

        if (hovered) {
            drawOutline(bgWidth, bgHeight);
        }

        GL11.glPopMatrix();
    }

    protected void drawOutline(int x, int y) {
        Gui.drawRect(0, 0, x, 1, -16711681);
        Gui.drawRect(0, y - 1, x, y, -16711681);
        Gui.drawRect(0, 0, 1, y, -16711681);
        Gui.drawRect(x - 1, 0, x, y, -16711681);
    }
}
