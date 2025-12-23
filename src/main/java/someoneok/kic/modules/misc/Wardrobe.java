package someoneok.kic.modules.misc;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.events.WardrobeSwappedEvent;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.PlayerUtils;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.sound.SoundUtils.playSound;
import static someoneok.kic.utils.sound.SoundUtils.validSound;

public class Wardrobe {
    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!KICConfig.wardrobeKeybinds || !ApiUtils.isVerified()) return;

        int button = Mouse.getEventButton();
        boolean isPressed = Mouse.getEventButtonState();

        if (!isPressed) return;
        if (button >= 0 && button <= 2) return;
        performWardrobeSwap(event.gui);
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!KICConfig.wardrobeKeybinds || !ApiUtils.isVerified()) return;
        performWardrobeSwap(event.gui);
    }

    private void performWardrobeSwap(GuiScreen gui) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        if (!(gui instanceof GuiContainer)) return;

        Container container = player.openContainer;
        if (!(container instanceof ContainerChest)) return;

        String containerName = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText().trim();
        if (!containerName.startsWith("Wardrobe")) return;

        int slot = getWardrobeSlot();
        if (slot == -1) return;

        Slot targetSlot = container.getSlot(slot);
        if (targetSlot == null) return;

        ItemStack stack = targetSlot.getStack();
        if (stack == null || !stack.hasDisplayName() || stack.getDisplayName().contains("Empty")) return;

        PlayerUtils.clickSlot(targetSlot, 0, 0);

        String sound = KICConfig.wardrobeSound;
        if (KICConfig.wardrobePlaySound && !isNullOrEmpty(sound)) {
            if (validSound(sound)) {
                playSound(sound, 2.0F, 1.0F, false);
            } else {
                KICConfig.wardrobeSound = "";
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            KICPrefix + " §cSound \"" + sound + "\" not found or not playable!"));
                }
            }
        }
        int page = containerName.contains("(3") ? 3 : containerName.contains("(2") ? 2 : 1;
        KICEventBus.post(new WardrobeSwappedEvent(page, slot));
    }

    private int getWardrobeSlot() {
        OneKeyBind[] bindings = {
                KICConfig.wardrobeSlot1, KICConfig.wardrobeSlot2, KICConfig.wardrobeSlot3,
                KICConfig.wardrobeSlot4, KICConfig.wardrobeSlot5, KICConfig.wardrobeSlot6,
                KICConfig.wardrobeSlot7, KICConfig.wardrobeSlot8, KICConfig.wardrobeSlot9
        };
        for (int i = 0; i < bindings.length; i++) if (bindings[i].isActive()) return 36 + i;
        return -1;
    }
}
