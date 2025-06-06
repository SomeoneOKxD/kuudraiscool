package someoneok.kic.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainer.class)
public interface InvokerGuiContainer {
    @Invoker("getSlotAtPosition")
    Slot callGetSlotAtPosition(int x, int y);
}
