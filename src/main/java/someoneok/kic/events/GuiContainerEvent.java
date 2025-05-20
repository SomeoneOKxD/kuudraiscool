package someoneok.kic.events;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiContainerEvent extends Event {
    protected final GuiContainer gui;
    protected final Container container;
    private String chestName;

    public GuiContainerEvent(GuiContainer gui, Container container) {
        this.gui = gui;
        this.container = container;
    }

    public GuiContainer getGui() {
        return gui;
    }

    public Container getContainer() {
        return container;
    }

    public String getChestName() {
        if (chestName == null) { // Lazy loading
            if (!(container instanceof ContainerChest)) {
                throw new IllegalStateException("Container is not a chest");
            }
            chestName = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText().trim();
        }
        return chestName;
    }

    // Background Drawn Event
    public static class BackgroundDrawnEvent extends GuiContainerEvent {
        private final int mouseX;
        private final int mouseY;
        private final float partialTicks;

        public BackgroundDrawnEvent(GuiContainer gui, Container container, int mouseX, int mouseY, float partialTicks) {
            super(gui, container);
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.partialTicks = partialTicks;
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }

        public float getPartialTicks() {
            return partialTicks;
        }
    }

    // Close Window Event (Cancelable)
    @Cancelable
    public static class CloseWindowEvent extends GuiContainerEvent {
        public CloseWindowEvent(GuiContainer gui, Container container) {
            super(gui, container);
        }
    }

    // Abstract Draw Slot Event
    public abstract static class DrawSlotEvent extends GuiContainerEvent {
        protected final Slot slot;

        public DrawSlotEvent(GuiContainer gui, Container container, Slot slot) {
            super(gui, container);
            this.slot = slot;
        }

        public Slot getSlot() {
            return slot;
        }

        // Pre Event (Cancelable)
        @Cancelable
        public static class Pre extends DrawSlotEvent {
            public Pre(GuiContainer gui, Container container, Slot slot) {
                super(gui, container, slot);
            }
        }

        // Post Event
        public static class Post extends DrawSlotEvent {
            public Post(GuiContainer gui, Container container, Slot slot) {
                super(gui, container, slot);
            }
        }
    }

    // Foreground Drawn Event
    public static class ForegroundDrawnEvent extends GuiContainerEvent {
        private final int mouseX;
        private final int mouseY;
        private final float partialTicks;

        public ForegroundDrawnEvent(GuiContainer gui, Container container, int mouseX, int mouseY, float partialTicks) {
            super(gui, container);
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.partialTicks = partialTicks;
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }

        public float getPartialTicks() {
            return partialTicks;
        }
    }

    // Slot Click Event (Cancelable)
    @Cancelable
    public static class SlotClickEvent extends GuiContainerEvent {
        private final Slot slot;
        private final int slotId;
        private final int clickedButton;
        private final int clickType;

        public SlotClickEvent(GuiContainer gui, Container container, Slot slot, int slotId, int clickedButton, int clickType) {
            super(gui, container);
            this.slot = slot;
            this.slotId = slotId;
            this.clickedButton = clickedButton;
            this.clickType = clickType;
        }

        public Slot getSlot() {
            return slot;
        }

        public int getSlotId() {
            return slotId;
        }

        public int getClickedButton() {
            return clickedButton;
        }

        public int getClickType() {
            return clickType;
        }
    }

    // Slot Click Event (Cancelable)
    @Cancelable
    public static class SlotClickedEvent extends GuiContainerEvent {
        private final Slot slot;
        private final int slotId;
        private final int clickedButton;
        private final int clickType;

        public SlotClickedEvent(GuiContainer gui, Container container, Slot slot, int slotId, int clickedButton, int clickType) {
            super(gui, container);
            this.slot = slot;
            this.slotId = slotId;
            this.clickedButton = clickedButton;
            this.clickType = clickType;
        }

        public Slot getSlot() {
            return slot;
        }

        public int getSlotId() {
            return slotId;
        }

        public int getClickedButton() {
            return clickedButton;
        }

        public int getClickType() {
            return clickType;
        }
    }
}
