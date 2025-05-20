package someoneok.kic.utils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import static someoneok.kic.KIC.mc;

public class PlayerUtils {
    public static void rightClick() {
        mc.addScheduledTask(() -> KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode()));
    }

    public static void leftClick() {
        mc.addScheduledTask(() -> KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode()));
    }

    public static void swapToIndex(int index) {
        mc.addScheduledTask(() -> KeyBinding.onTick(mc.gameSettings.keyBindsHotbar[index].getKeyCode()));
    }

    public static int getHotbarSlotIndex(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static ItemStack getHotbarItemStack(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem().equals(item)) {
                return stack;
            }
        }
        return null;
    }

    public static Vec3 getPlayerEyePos() {
        if (mc.thePlayer == null) return null;
        return new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }

    // I am not trying to rat you im simply getting your username.
    public static String getPlayerName() {
        if (mc != null) {
            if (mc.thePlayer != null) {
                return mc.thePlayer.getName();
            } else if (mc.getSession() != null) {
                return mc.getSession().getUsername();
            }
        }
        return "Unknown";
    }
}
