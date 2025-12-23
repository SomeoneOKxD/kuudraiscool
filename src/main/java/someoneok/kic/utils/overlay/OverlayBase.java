package someoneok.kic.utils.overlay;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.Island;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.data.DataHandler;

import java.util.Set;
import java.util.function.Supplier;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class OverlayBase {
    private static final double MAX_SCALE = 5;
    private static final double MIN_SCALE = 0.25;

    protected String name;
    protected String displayName;
    protected int x, y, width, height;
    protected String text;
    protected OverlayType type;
    protected double scale;
    protected int padding;
    protected Supplier<Boolean> configOption;
    protected Supplier<Boolean> renderCondition;
    protected Set<Island> islands;
    protected String exampleText;
    protected boolean editing = false;

    public OverlayBase(Supplier<Boolean> configOption, String name, String displayName, Set<Island> islands, Supplier<Boolean> renderCondition, OverlayType type, String exampleText) {
        this.configOption = configOption;
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.renderCondition = renderCondition;
        this.islands = islands;

        this.text = "";
        this.x = 50;
        this.y = 50;
        this.scale = 1;
        this.padding = 3;

        this.exampleText = exampleText;

        updateScale();
    }

    public void setEditingMode(boolean editing) {
        this.editing = editing;
        this.updateScale();
    }

    public void setExampleText(String exampleText) {
        this.exampleText = exampleText;
    }

    public void updateText(String text) {
        this.text = text;
        updateScale();
    }

    public void updateScale() {
        String content = editing ? exampleText : text;
        TextDimensions dimensions = calculateTextDimensions(content);
        this.width = dimensions.width;
        this.height = dimensions.height;
    }

    public void reset() {
        this.x = 50;
        this.y = 50;
        this.scale = 1;
        DataHandler.saveOverlays();
    }

    private TextDimensions calculateTextDimensions(String text) {
        FontRenderer fontRenderer = mc.fontRendererObj;
        String[] lines = text.split("\n");

        int maxWidth = 0;
        int totalHeight = lines.length * (fontRenderer.FONT_HEIGHT + 3);

        for (String line : lines) {
            boolean bold = false;
            int lineWidth = 0;

            for (int i = 0, len = line.length(); i < len; i++) {
                char c = line.charAt(i);

                if (c == '§' && i + 1 < len) {
                    char code = Character.toLowerCase(line.charAt(++i));
                    switch (code) {
                        case 'l': bold = true; break;
                        case 'r': bold = false; break;
                    }
                    continue;
                }

                int charWidth = fontRenderer.getCharWidth(c);
                if (bold) charWidth++;

                lineWidth += charWidth;
            }

            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        return new TextDimensions(maxWidth, totalHeight);
    }

    public void changeScale(double scaleChange) {
        this.scale = Math.max(MIN_SCALE, Math.min(this.scale + scaleChange, MAX_SCALE));
        updateScale();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return (int) ((this.width + padding * 2) * scale);
    }

    public int getHeight() {
        return (int) ((this.height + padding * 2) * scale);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setRenderCondition(Supplier<Boolean> renderCondition) {
        this.renderCondition = renderCondition;
    }

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

        GL11.glPopMatrix();
    }

    private static class TextDimensions {
        int width, height;

        public TextDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public boolean shouldRender() {
        if (editing) {
            return configOption.get();
        }

        return configOption.get() &&
                (islands.contains(Island.ALL) || islands.contains(LocationUtils.currentIsland)) &&
                renderCondition.get() &&
                !isNullOrEmpty(text);
    }

    public String getText() {
        return this.text;
    }
}
