package someoneok.kic.utils.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.NEUCompatibility;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class OverlayManager {
    private static final Map<String, MovableOverlay> overlays = new HashMap<>();
    private static MovableOverlay draggingOverlay = null;
    private static InteractiveOverlay clickedOverlay = null;
    private static MovableOverlay lastHoveredOverlay = null;

    public static void addOverlay(MovableOverlay overlay) {
        overlays.put(overlay.name, overlay);
    }

    public static MovableOverlay getOverlay(String name) {
        return overlays.get(name);
    }

    public static Collection<MovableOverlay> getOverlays() {
        return overlays.values();
    }

    private static Stream<MovableOverlay> getFilteredOverlays(OverlayType type) {
        return getOverlays().stream()
                .filter(overlay -> {
                    if (overlay instanceof DualOverlay) {
                        return overlay.type == OverlayType.DUAL && (type == OverlayType.NORMAL || type == OverlayType.INGUI);
                    }
                    return overlay.type == type && !overlay.text.isEmpty();
                });
    }

    public static MovableOverlay getHoveredOverlay(int mouseX, int mouseY) {
        return getOverlays().stream()
                .filter(overlay -> {
                    if (overlay instanceof DualOverlay && !overlay.shouldRender()) return false;
                    if (overlay instanceof InteractiveOverlay && !overlay.shouldRender()) return false;
                    if (overlay instanceof MovableOverlay && !overlay.shouldRender()) return false;
                    return mouseX >= overlay.getX() && mouseX <= overlay.getX() + overlay.getWidth() &&
                            mouseY >= overlay.getY() && mouseY <= overlay.getY() + overlay.getHeight();
                })
                .findFirst()
                .orElse(null);
    }

    @SubscribeEvent
    public void onDrawGuiBackground(GuiScreenEvent.BackgroundDrawnEvent e) {
        if (EditHudScreen.isEditingMode() || isExcludedGui(e.gui) || NEUCompatibility.isStorageMenuActive()) return;
        getFilteredOverlays(OverlayType.INGUI).forEach(MovableOverlay::render);
    }

//    TODO: Fix NEU integration
//    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
//    public void onGuiScreenDrawPre(GuiScreenEvent.DrawScreenEvent.Pre e) {
//        if (isExcludedGui(e.gui) || (!e.isCanceled() || !NEUCompatibility.isStorageMenuActive())) return;
//        getFilteredOverlays(OverlayType.INGUI).forEach(MovableOverlay::render);
//    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (EditHudScreen.isEditingMode()) return;
        getFilteredOverlays(OverlayType.NORMAL).forEach(MovableOverlay::render);
    }

    @SubscribeEvent
    public void onMouseDrag(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (EditHudScreen.isEditingMode() || NEUCompatibility.isStorageMenuActive()) return;
        if (draggingOverlay != null) {
            int[] mousePos = getMousePosition(event);
            draggingOverlay.onMouseDragged(mousePos[0], mousePos[1]);
        }
    }

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (EditHudScreen.isEditingMode() || event.gui instanceof net.minecraft.client.gui.GuiChat || NEUCompatibility.isStorageMenuActive()) return;

        int[] mousePos = getMousePosition(event);
        int mouseX = mousePos[0], mouseY = mousePos[1];
        MovableOverlay hoveredOverlay = getHoveredOverlay(mouseX, mouseY);

        if (hoveredOverlay instanceof InteractiveOverlay && !(hoveredOverlay).shouldRender()) return;
        if (hoveredOverlay instanceof DualOverlay && !(hoveredOverlay).shouldRender()) return;
        clickedOverlay = null;

        if (KICConfig.hudMoveKeybind.isActive()) {
            if (draggingOverlay == null) {
                if (Mouse.isButtonDown(0) && (draggingOverlay = hoveredOverlay) != null) {
                    draggingOverlay.startDragging(mouseX, mouseY);
                    draggingOverlay.setHovered(true);
                } else if (Mouse.isButtonDown(2) && hoveredOverlay != null) {
                    hoveredOverlay.reset();
                }
            }
        } else if (hoveredOverlay instanceof DualOverlay) {
            DualOverlay dual = (DualOverlay) hoveredOverlay;
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                dual.onMouseClick(mouseX, mouseY);
                clickedOverlay = null;
            }
        } else if (hoveredOverlay instanceof InteractiveOverlay) {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                clickedOverlay = (InteractiveOverlay) hoveredOverlay;
                clickedOverlay.onMouseClick(mouseX, mouseY);
            }
        }
    }

    @SubscribeEvent
    public void onMouseRelease(GuiScreenEvent.MouseInputEvent.Post event) {
        if (EditHudScreen.isEditingMode() || NEUCompatibility.isStorageMenuActive()) return;
        if (draggingOverlay != null && !Mouse.isButtonDown(0)) {
            draggingOverlay.stopDragging();
            draggingOverlay.setHovered(false);
            draggingOverlay = null;
            OverlayDataHandler.saveOverlays();
        }
        if (clickedOverlay != null) {
            clickedOverlay.onMouseRelease();
            clickedOverlay = null;
        }

        for (MovableOverlay overlay : getOverlays()) {
            if (overlay instanceof DualOverlay) {
                ((DualOverlay) overlay).onMouseRelease();
            }
        }
    }

    @SubscribeEvent
    public void onMouseMove(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (EditHudScreen.isEditingMode() || NEUCompatibility.isStorageMenuActive()) return;
        int[] mousePos = getMousePosition(event);
        int mouseX = mousePos[0], mouseY = mousePos[1];
        MovableOverlay hoveredOverlay = getHoveredOverlay(mouseX, mouseY);

        if (hoveredOverlay instanceof InteractiveOverlay && !(hoveredOverlay).shouldRender()) return;
        if (hoveredOverlay instanceof DualOverlay && !(hoveredOverlay).shouldRender()) return;

        if (hoveredOverlay != lastHoveredOverlay) {
            if (lastHoveredOverlay instanceof DualOverlay) {
                ((DualOverlay) lastHoveredOverlay).updateHoverState(-1, -1);
            } else if (lastHoveredOverlay instanceof InteractiveOverlay) {
                ((InteractiveOverlay) lastHoveredOverlay).updateHoverState(-1, -1);
            }
            lastHoveredOverlay = hoveredOverlay;
        }

        if (hoveredOverlay instanceof DualOverlay) {
            ((DualOverlay) hoveredOverlay).updateHoverState(mouseX, mouseY);
        } else if (hoveredOverlay instanceof InteractiveOverlay) {
            ((InteractiveOverlay) hoveredOverlay).updateHoverState(mouseX, mouseY);
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (EditHudScreen.isEditingMode() || event.gui instanceof net.minecraft.client.gui.GuiChat) return;
        if (NEUCompatibility.isStorageMenuActive()) return;

        int scroll = Mouse.getDWheel();
        if (scroll == 0) return;

        int[] mousePos = getMousePosition(event);
        MovableOverlay hoveredOverlay = getHoveredOverlay(mousePos[0], mousePos[1]);

        if (hoveredOverlay instanceof InteractiveOverlay && !(hoveredOverlay).shouldRender()) return;
        if (hoveredOverlay instanceof DualOverlay && !(hoveredOverlay).shouldRender()) return;

        if (hoveredOverlay != null) {
            if (KICConfig.hudMoveKeybind.isActive()) {
                hoveredOverlay.changeScale(scroll > 0 ? 0.1 : -0.1);
                OverlayDataHandler.saveOverlays();
                event.setCanceled(true);
            } else if (hoveredOverlay instanceof InteractiveOverlay) {
                ((InteractiveOverlay) hoveredOverlay).onScroll(scroll, mousePos[0], mousePos[1]);
                event.setCanceled(true);
            }
        }
    }

    private static int[] getMousePosition(GuiScreenEvent event) {
        int width = event.gui.width, height = event.gui.height;
        int mouseX = Mouse.getEventX() * width / event.gui.mc.displayWidth;
        int mouseY = height - (Mouse.getEventY() * height / event.gui.mc.displayHeight);
        return new int[]{mouseX, mouseY};
    }

    private static boolean isExcludedGui(GuiScreen gui) {
        return gui instanceof GuiIngameMenu || gui instanceof GuiMainMenu || gui instanceof GuiOptions;
    }

    public static void setEditingForAll(boolean editing) {
        for (MovableOverlay overlay : getOverlays()) {
            if (isEditable(overlay)) {
                overlay.setEditingMode(editing);
                overlay.setHovered(false);
            }
        }
    }

    public static boolean isEditable(MovableOverlay overlay) {
        return overlay instanceof MovableOverlay || overlay instanceof InteractiveOverlay || overlay instanceof DualOverlay;
    }
    public static Dimension getScaledScreen() {
        Minecraft mc = Minecraft.getMinecraft();

        int scale = mc.gameSettings.guiScale;
        if (scale == 0) scale = 1000;

        int scaleFactor = 0;
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        while (scaleFactor < scale && width / (scaleFactor + 1) >= 320 && height / (scaleFactor + 1) >= 240) {
            scaleFactor++;
        }

        int scaledWidth = width / scaleFactor;
        int scaledHeight = height / scaleFactor;

        return new Dimension(scaledWidth, scaledHeight);
    }

}

