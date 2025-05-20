package someoneok.kic.mixin;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import someoneok.kic.events.hooks.GuiContainerHook;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Unique
    private final GuiContainerHook kIC$hook = new GuiContainerHook(this);

    @Inject(method = "onGuiClosed", at = @At("HEAD"), cancellable = true)
    private void onGuiClosedInject(CallbackInfo ci) {
        kIC$hook.closeWindow(ci);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 1, shift = At.Shift.AFTER))
    private void backgroundDrawn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        kIC$hook.backgroundDrawn(mouseX, mouseY, partialTicks, ci);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V", shift = At.Shift.AFTER))
    private void onForegroundDraw(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        kIC$hook.foregroundDrawn(mouseX, mouseY, partialTicks, ci);
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slot, CallbackInfo ci) {
        kIC$hook.onDrawSlot(slot, ci);
    }

    @Inject(method = "drawSlot", at = @At("RETURN"), cancellable = true)
    private void onDrawSlotPost(Slot slot, CallbackInfo ci) {
        kIC$hook.onDrawSlotPost(slot, ci);
    }

    @Inject(method = "handleMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;windowClick(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        kIC$hook.onMouseClick(slot, slotId, clickedButton, clickType, ci);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void interceptClick(int mouseX, int mouseY, int button, CallbackInfo ci) {
        Slot slot = ((InvokerGuiContainer) this).callGetSlotAtPosition(mouseX, mouseY);
        if (slot == null) return;

        kIC$hook.onMouseClicked(slot, slot.slotNumber, button, 0, ci);
    }
}
