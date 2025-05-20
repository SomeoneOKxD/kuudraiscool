package someoneok.kic.utils.overlay;

import net.minecraft.client.gui.GuiChat;
import someoneok.kic.models.Island;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static someoneok.kic.KIC.mc;

public class DualOverlay extends MovableOverlay {
    private final MovableOverlay movableOverlay;
    private final InteractiveOverlay interactiveOverlay;

    public DualOverlay(Supplier<Boolean> configOption, String name, String displayName, Set<Island> islands, Supplier<Boolean> renderCondition, String exampleText) {
        super(configOption, name, displayName, islands, renderCondition, OverlayType.DUAL, exampleText);

        movableOverlay = new MovableOverlay(configOption, name, displayName, islands, renderCondition, OverlayType.NORMAL, exampleText) {
            @Override public void render() {
                this.x = DualOverlay.this.x;
                this.y = DualOverlay.this.y;
                this.scale = DualOverlay.this.scale;
                super.render();
            }
        };

        interactiveOverlay = new InteractiveOverlay(configOption, name, displayName, islands, renderCondition, OverlayType.INGUI, exampleText) {
            @Override public void render() {
                this.x = DualOverlay.this.x;
                this.y = DualOverlay.this.y;
                this.scale = DualOverlay.this.scale;
                super.render();
            }
        };

        this.x = movableOverlay.getX();
        this.y = movableOverlay.getY();
        this.scale = movableOverlay.scale;
    }

    public void setPositionAndScale(int x, int y, double scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.updateScale();

        movableOverlay.x = x;
        movableOverlay.y = y;
        movableOverlay.scale = scale;
        movableOverlay.updateScale();

        interactiveOverlay.x = x;
        interactiveOverlay.y = y;
        interactiveOverlay.scale = scale;
        interactiveOverlay.updateScale();
    }

    @Override
    public void setEditingMode(boolean editing) {
        super.setEditingMode(editing);
        movableOverlay.setEditingMode(editing);
        setPositionAndScale(this.x, this.y, this.scale);
    }

    @Override
    public void setExampleText(String exampleText) {
        super.setExampleText(exampleText);
        movableOverlay.setExampleText(exampleText);
    }

    public void setBaseText(String text) {
        movableOverlay.updateText(text);
    }

    public void setInteractiveSegments(List<OverlaySegment> segments) {
        interactiveOverlay.setSegments(segments);
    }

    @Override
    public void render() {
        if (!shouldRender()) return;

        if (editing) {
            movableOverlay.render();
            return;
        }

        if (mc.currentScreen == null || mc.currentScreen instanceof GuiChat) {
            movableOverlay.render();
        } else {
            interactiveOverlay.render();
        }
    }

    @Override
    public void changeScale(double scaleChange) {
        super.changeScale(scaleChange);
        movableOverlay.scale = this.scale;
        interactiveOverlay.scale = this.scale;
        movableOverlay.updateScale();
        interactiveOverlay.updateScale();
    }

    @Override
    public void reset() {
        super.reset();
        setPositionAndScale(this.x, this.y, this.scale);
    }

    public void onMouseClick(int mouseX, int mouseY) {
        if (mc.currentScreen != null) {
            interactiveOverlay.onMouseClick(mouseX, mouseY);
        }
    }

    public void onMouseRelease() {
        interactiveOverlay.onMouseRelease();
    }

    public void updateHoverState(int mouseX, int mouseY) {
        interactiveOverlay.updateHoverState(mouseX, mouseY);
    }

    @Override
    public void setHovered(boolean hovered) {
        if (editing) {
            movableOverlay.setHovered(hovered);
        } else {
            interactiveOverlay.setHovered(hovered);
        }
    }

    @Override
    public boolean shouldRender() {
        return editing || movableOverlay.shouldRender() || interactiveOverlay.shouldRender();
    }

    public boolean isGuiActive() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat);
    }

    @Override
    public int getWidth() {
        if (editing) {
            return movableOverlay.getWidth();
        }
        return isGuiActive() ? interactiveOverlay.getWidth() : movableOverlay.getWidth();
    }

    @Override
    public int getHeight() {
        if (editing) {
            return movableOverlay.getHeight();
        }
        return isGuiActive() ? interactiveOverlay.getHeight() : movableOverlay.getHeight();
    }

    @Override
    public int getX() {
        if (editing) {
            return movableOverlay.getX();
        }
        return isGuiActive() ? interactiveOverlay.getX() : movableOverlay.getX();
    }

    @Override
    public int getY() {
        if (editing) {
            return movableOverlay.getY();
        }
        return isGuiActive() ? interactiveOverlay.getY() : movableOverlay.getY();
    }

    @Override
    public void startDragging(int mouseX, int mouseY) {
        if (editing) {
            movableOverlay.startDragging(mouseX, mouseY);
        } else {
            super.startDragging(mouseX, mouseY);
        }
    }

    @Override
    public void stopDragging() {
        if (editing) {
            movableOverlay.stopDragging();
        } else {
            super.stopDragging();
        }
    }

    @Override
    public void onMouseDragged(int mouseX, int mouseY) {
        if (editing) {
            movableOverlay.onMouseDragged(mouseX, mouseY);
            setPositionAndScale(movableOverlay.getX(), movableOverlay.getY(), this.scale);
        } else {
            super.onMouseDragged(mouseX, mouseY);
        }
    }
}
