package someoneok.kic.events.hooks;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import someoneok.kic.events.GuiContainerEvent;

public class GuiContainerHook {

    private final GuiContainer gui;

    public GuiContainerHook(Object guiAny) {
        this.gui = (GuiContainer) guiAny;
    }

    public void closeWindow(CallbackInfo ci) {
        GuiContainerEvent.CloseWindowEvent event = new GuiContainerEvent.CloseWindowEvent(gui, gui.inventorySlots);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            ci.cancel();
        }
    }

    public void backgroundDrawn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiContainerEvent.BackgroundDrawnEvent event = new GuiContainerEvent.BackgroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks);
        MinecraftForge.EVENT_BUS.post(event);
    }

    public void foregroundDrawn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiContainerEvent.ForegroundDrawnEvent event = new GuiContainerEvent.ForegroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks);
        MinecraftForge.EVENT_BUS.post(event);
    }

    public void onDrawSlot(Slot slot, CallbackInfo ci) {
        GuiContainerEvent.DrawSlotEvent.Pre event = new GuiContainerEvent.DrawSlotEvent.Pre(gui, gui.inventorySlots, slot);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            ci.cancel();
        }
    }

    public void onDrawSlotPost(Slot slot, CallbackInfo ci) {
        GuiContainerEvent.DrawSlotEvent.Post event = new GuiContainerEvent.DrawSlotEvent.Post(gui, gui.inventorySlots, slot);
        MinecraftForge.EVENT_BUS.post(event);
    }

    public void onMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        GuiContainerEvent.SlotClickEvent event = new GuiContainerEvent.SlotClickEvent(gui, gui.inventorySlots, slot, slotId, clickedButton, clickType);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            ci.cancel();
        }
    }

    public void onMouseClicked(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        GuiContainerEvent.SlotClickedEvent event = new GuiContainerEvent.SlotClickedEvent(gui, gui.inventorySlots, slot, slotId, clickedButton, clickType);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            ci.cancel();
        }
    }
}
