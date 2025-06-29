package someoneok.kic.modules.dev;

import cc.polyfrost.oneconfig.utils.IOUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.ApiUtils;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

public class Dev {
    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!ApiUtils.isDev() && !ApiUtils.isTester()) return;
        if (KICConfig.copyNBT.isActive()) copyNBTToClipboard();
    }

    private void copyNBTToClipboard() {
        GuiScreen currentScreen = mc.currentScreen;

        if (currentScreen instanceof GuiContainer) {
            Slot currentSlot = ((GuiContainer) currentScreen).getSlotUnderMouse();

            if (currentSlot != null && currentSlot.getHasStack()) {
                String nbt = currentSlot.getStack().serializeNBT().toString();
                IOUtils.copyStringToClipboard(nbt);
                sendMessageToPlayer(KICPrefix + " §aItem data was copied to clipboard!");
            }
        }
    }
}
