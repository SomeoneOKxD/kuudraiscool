package someoneok.kic.utils.overlay;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.Island;
import someoneok.kic.utils.LocationUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.applyEffect;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class InteractiveOverlay extends MovableOverlay {
    private final List<OverlaySegment> segments;
    private OverlaySegment hoveredSegment = null;
    private boolean mouseWasPressed = false;

    public InteractiveOverlay(Supplier<Boolean> configOption, String name, String displayName, Set<Island> islands, Supplier<Boolean> renderCondition, OverlayType type, String exampleText) {
        super(configOption, name, displayName, islands, renderCondition, type, exampleText);
        this.segments = new CopyOnWriteArrayList<>();
    }

    public void setSegments(List<OverlaySegment> segments) {
        this.segments.clear();
        this.segments.addAll(segments);

        updateText(segments.stream()
                .map(OverlaySegment::getText)
                .collect(Collectors.joining()));
    }

    @Override
    public void render() {
        if (!shouldRender()) return;

        if (editing) {
            FontRenderer fontRenderer = mc.fontRendererObj;
            String[] lines = exampleText.split("\n");

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

            if (hovered) drawOutline(bgWidth, bgHeight);

            GL11.glPopMatrix();
            return;
        }

        FontRenderer fontRenderer = mc.fontRendererObj;
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0);
        GL11.glScaled(scale, scale, 1.0);

        int bgWidth = width + padding * 2;
        int bgHeight = height + padding * 2;

        if (KICConfig.drawHudBackground) {
            Gui.drawRect(0, 0, bgWidth, bgHeight, 0x80000000);
        }

        int textX = padding, textY = padding;

        for (OverlaySegment segment : segments) {
            String text = segment.getText();
            String[] lines = text.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                String renderText = (segment == hoveredSegment) ? applyEffect(line, "§n") : line;

                fontRenderer.drawString(renderText, textX, textY, segment.getColor(), KICConfig.useTextShadowInHuds);

                if (i < lines.length - 1 || text.endsWith("\n")) {
                    textX = padding;
                    textY += fontRenderer.FONT_HEIGHT + 3;
                } else {
                    textX += fontRenderer.getStringWidth(line);
                }
            }
        }

        if (hovered) drawOutline(bgWidth, bgHeight);

        GL11.glPopMatrix();
    }

    public void onMouseRelease() {
        mouseWasPressed = false;
    }

    public void onMouseClick(int mouseX, int mouseY) {
        if (mouseWasPressed) return;

        mouseWasPressed = true;
        OverlaySegment segment = getSegmentAt(mouseX, mouseY, false);

        if (segment != null && segment.isClickable()) {
            segment.executeClick();
        }
    }

    public void updateHoverState(int mouseX, int mouseY) {
        OverlaySegment newHoveredSegment = getSegmentAt(mouseX, mouseY, false);

        if (hoveredSegment != null && hoveredSegment != newHoveredSegment) {
            hoveredSegment.executeHoverEnd();
            hoveredSegment = null;
        }

        if (newHoveredSegment != null && newHoveredSegment.isHoverable() && hoveredSegment != newHoveredSegment) {
            newHoveredSegment.executeHover();
            hoveredSegment = newHoveredSegment;
        }
    }

    public void onScroll(int scroll, int mouseX, int mouseY) {
        OverlaySegment segment = getSegmentAt(mouseX, mouseY, true);
        if (segment != null && segment.isScrollable()) {
            boolean up = scroll > 0;
            segment.executeScroll(up);
        }
    }

    private OverlaySegment getSegmentAt(int mouseX, int mouseY, boolean overShoot) {
        int localMouseX = (int) ((mouseX - x) / scale);
        int localMouseY = (int) ((mouseY - y) / scale);

        int textX = padding, textY = padding;

        for (OverlaySegment segment : segments) {
            String text = segment.getText();
            String[] lines = text.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                int textWidth = mc.fontRendererObj.getStringWidth(line);
                int textHeight = mc.fontRendererObj.FONT_HEIGHT;

                int yStart = textY - (overShoot ? 3 : 0);
                int yEnd = textY + textHeight + (overShoot ? 3 : 0);

                if (localMouseY >= yStart && localMouseY <= yEnd &&
                        localMouseX >= textX && localMouseX <= textX + textWidth) {
                    return segment;
                }

                if (i < lines.length - 1 || text.endsWith("\n")) {
                    textX = padding;
                    textY += textHeight + 3;
                } else {
                    textX += textWidth;
                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldRender() {
        if (editing) {
            return configOption.get();
        }

        return configOption.get() &&
                (islands.contains(Island.ALL) || islands.contains(LocationUtils.currentIsland)) &&
                renderCondition.get() &&
                !isNullOrEmpty(text) &&
                segments != null &&
                !segments.isEmpty();
    }
}
