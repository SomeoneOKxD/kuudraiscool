package someoneok.kic.utils.overlay;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import someoneok.kic.models.misc.PatcherScale;

import java.io.IOException;
import java.util.*;

import static someoneok.kic.utils.overlay.OverlayManager.isEditable;

@SideOnly(Side.CLIENT)
public class EditHudScreen extends GuiScreen {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 222;
    private static final String INFO = "§7Left-Click: Drag | Right-Click: Reset | Scroll: Scale";
    private static int infoWidth = 0;

    private static boolean editHudMode = false;
    private boolean showPopup = false;
    private int previousGuiScale = -1;
    private OverlayType currentMode = OverlayType.NORMAL;
    private MovableOverlay draggingOverlay = null;

    private GuiButton toggleModeButton;
    private GuiButton manageOverlaysButton;
    private GuiButton resetOverlaysButton;
    private final List<GuiCheckBox> overlayCheckBoxes = new ArrayList<>();
    private final Map<OverlayType, Map<String, Boolean>> visibilityByMode = new EnumMap<>(OverlayType.class);

    @Override
    public void initGui() {
        super.initGui();

        int patcherScale = PatcherScale.getInvScale();
        if (patcherScale > 0 && patcherScale <= 5) {
            previousGuiScale = mc.gameSettings.guiScale;
            mc.gameSettings.guiScale = patcherScale;

            ScaledResolution res = new ScaledResolution(mc);
            this.width = res.getScaledWidth();
            this.height = res.getScaledHeight();
        }

        editHudMode = true;
        draggingOverlay = null;
        int btnWidth = 150;
        int btnHeight = 20;
        int x = (this.width / 2) - (btnWidth / 2);
        int y = this.height - btnHeight - 5;

        this.buttonList.clear();
        this.buttonList.add(toggleModeButton = new GuiButton(0, x, y, btnWidth, btnHeight, getModeText()));
        this.buttonList.add(manageOverlaysButton = new GuiButton(1, x, y - 50, btnWidth, btnHeight, "Manage Overlays"));
        this.buttonList.add(resetOverlaysButton = new GuiButton(2, x, y - 25, btnWidth, btnHeight, "Reset Overlays"));

        infoWidth = this.fontRendererObj.getStringWidth(INFO);

        OverlayManager.setEditingForAll(true);
        buildOverlayPopup();
    }

    private void buildOverlayPopup() {
        overlayCheckBoxes.clear();
        Map<String, Boolean> visibilityMap = visibilityByMode.computeIfAbsent(currentMode, k -> new HashMap<>());

        int maxWidth = 0;
        List<MovableOverlay> overlaysToDisplay = new ArrayList<>();

        for (MovableOverlay overlay : OverlayManager.getOverlays()) {
            if (!shouldRender(overlay)) continue;

            visibilityMap.putIfAbsent(overlay.displayName, true);
            overlaysToDisplay.add(overlay);

            int textWidth = this.fontRendererObj.getStringWidth(overlay.displayName);
            maxWidth = Math.max(maxWidth, textWidth);
        }

        maxWidth += 20;
        int popupX = manageOverlaysButton.xPosition + (manageOverlaysButton.width / 2) - (maxWidth / 2);
        int popupY = manageOverlaysButton.yPosition - 10 - (overlaysToDisplay.size() * 15);

        for (int i = 0; i < overlaysToDisplay.size(); i++) {
            MovableOverlay overlay = overlaysToDisplay.get(i);
            GuiCheckBox box = new GuiCheckBox(100 + i, popupX, popupY + (i * 15), overlay.displayName, visibilityMap.get(overlay.displayName));
            overlayCheckBoxes.add(box);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == toggleModeButton) {
            currentMode = (currentMode == OverlayType.NORMAL) ? OverlayType.INGUI : OverlayType.NORMAL;
            toggleModeButton.displayString = getModeText();
            showPopup = false;
        }
        else if (button== resetOverlaysButton){
            resetAllOverlays();
        }
        else if (button == manageOverlaysButton) {
            showPopup = !showPopup;
            if (showPopup) buildOverlayPopup();
        }
    }

    private String getModeText() {
        return "Mode: " + (currentMode == OverlayType.NORMAL ? "NORMAL" : "INGUI");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (currentMode == OverlayType.INGUI) {
            drawDefaultBackground();
            drawChestBackground();
        }

        for (MovableOverlay overlay : OverlayManager.getOverlays()) {
            if (!shouldRender(overlay) || !isOverlayVisible(overlay)) continue;

            overlay.setHovered(isMouseOver(mouseX, mouseY, overlay));
            overlay.render();
        }

        int infoX = (this.width - infoWidth) / 2;
        int infoY = toggleModeButton.yPosition - 65;
        this.fontRendererObj.drawStringWithShadow(INFO, infoX, infoY, 0xFFFFFF);

        if (showPopup && !overlayCheckBoxes.isEmpty()) {
            int padding = 5;
            int left = overlayCheckBoxes.get(0).xPosition - padding;
            int top = overlayCheckBoxes.get(0).yPosition - padding;

            int maxBoxWidth = 0;
            for (GuiCheckBox box : overlayCheckBoxes) {
                int width = this.fontRendererObj.getStringWidth(box.displayString) + 20; // checkbox padding
                maxBoxWidth = Math.max(maxBoxWidth, width);
            }

            int right = left + maxBoxWidth + (padding * 2);
            int bottom = overlayCheckBoxes.get(overlayCheckBoxes.size() - 1).yPosition + 15;

            drawRect(left, top, right, bottom, 0x99000000);

            for (GuiCheckBox box : overlayCheckBoxes) {
                box.drawButton(mc, mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawChestBackground() {
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (showPopup) {
            Map<String, Boolean> visibilityMap = visibilityByMode.get(currentMode);
            for (GuiCheckBox box : overlayCheckBoxes) {
                if (box.mousePressed(mc, mouseX, mouseY)) {
                    visibilityMap.put(box.displayString, box.isChecked());
                }
            }
        }

        for (MovableOverlay overlay : OverlayManager.getOverlays()) {
            if (!shouldRender(overlay) || !isOverlayVisible(overlay) || !isMouseOver(mouseX, mouseY, overlay)) continue;

            if (mouseButton == 0) {
                draggingOverlay = overlay;
                overlay.startDragging(mouseX, mouseY);
            } else if (mouseButton == 2) {
                overlay.reset();
            }
            break;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingOverlay != null) {
            draggingOverlay.stopDragging();
            draggingOverlay = null;
            OverlayDataHandler.saveOverlays();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggingOverlay != null) {
            draggingOverlay.onMouseDragged(mouseX, mouseY);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;

        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - (Mouse.getEventY() * height / mc.displayHeight);
        double scaleDelta = wheel > 0 ? 0.1 : -0.1;

        if (draggingOverlay != null && isOverlayVisible(draggingOverlay)) {
            draggingOverlay.changeScale(scaleDelta);
            OverlayDataHandler.saveOverlays();
            return;
        }

        for (MovableOverlay overlay : OverlayManager.getOverlays()) {
            if (!shouldRender(overlay) || !isOverlayVisible(overlay) || !isMouseOver(mouseX, mouseY, overlay)) continue;

            overlay.changeScale(scaleDelta);
            OverlayDataHandler.saveOverlays();
            return;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        editHudMode = false;
        draggingOverlay = null;
        OverlayDataHandler.saveOverlays();

        if (previousGuiScale != -1) {
            mc.gameSettings.guiScale = previousGuiScale;
        }

        OverlayManager.setEditingForAll(false);
    }

    private boolean isMouseOver(int mouseX, int mouseY, MovableOverlay overlay) {
        if (draggingOverlay != null) return draggingOverlay == overlay;
        int x = overlay.getX(), y = overlay.getY(), w = overlay.getWidth(), h = overlay.getHeight();
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static boolean isEditingMode() {
        return editHudMode;
    }

    private boolean isOverlayVisible(MovableOverlay overlay) {
        Map<String, Boolean> visibilityMap = visibilityByMode.get(currentMode);
        return visibilityMap == null || visibilityMap.getOrDefault(overlay.displayName, true);
    }

    private boolean shouldRender(MovableOverlay overlay) {
        return overlay.configOption.get() && isEditable(overlay)
                && (overlay instanceof DualOverlay || overlay.type == currentMode);
    }

    private void resetAllOverlays() {
        for (OverlayBase overlay : OverlayManager.getOverlays()) {
            if (overlay.type == currentMode) {
                overlay.reset();
            }
        }
    }
}
