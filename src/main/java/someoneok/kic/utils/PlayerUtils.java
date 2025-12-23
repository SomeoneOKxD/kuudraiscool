package someoneok.kic.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.events.PacketEvent;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.ItemUtils.getItemId;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class PlayerUtils {
    public static boolean isSneaking = false;

    public static void rightClick() { pressKey(mc.gameSettings.keyBindUseItem); }
    public static void leftClick() { pressKey(mc.gameSettings.keyBindAttack); }
    public static void shiftClick() { pressKey(mc.gameSettings.keyBindSneak); }

    private static void pressKey(KeyBinding keyBinding) {
        if (mc.currentScreen != null || keyBinding == null) return;
        int keyCode = keyBinding.getKeyCode();
        if (keyCode == 0) return;
        KeyBinding.onTick(keyCode);
    }

    public static void swapToIndex(int index) {
        if (index < 0 || index > 8 || mc.currentScreen != null) return;
        KeyBinding.onTick(mc.gameSettings.keyBindsHotbar[index].getKeyCode());
    }

    public static void setCurrentSlot(int index) {
        if (index < 0 || index > 8 || mc.thePlayer == null) return;
        mc.thePlayer.inventory.currentItem = index;
    }

    public static int getCurrentHotbarSlot() {
        if (mc.thePlayer == null) return 0;
        return mc.thePlayer.inventory.currentItem;
    }

    public static int getHotbarSlotIndex(Item item) {
        final EntityPlayerSP p = mc.thePlayer;
        if (p == null || item == null) return -1;

        final ItemStack[] inv = p.inventory.mainInventory;
        for (int i = 0; i < 9; i++) {
            final ItemStack s = inv[i];
            if (s == null) continue;
            if (item == s.getItem()) return i;
        }
        return -1;
    }

    public static int getHotbarSlotIndex(String skyblockId) {
        final EntityPlayerSP p = mc.thePlayer;
        if (p == null || isNullOrEmpty(skyblockId)) return -1;

        final ItemStack[] inv = p.inventory.mainInventory;
        for (int i = 0; i < 9; i++) {
            final ItemStack s = inv[i];
            if (s == null) continue;
            final String id = getItemId(s);
            if (id != null && id.equalsIgnoreCase(skyblockId)) return i;
        }
        return -1;
    }

    public static ItemStack getHotbarItemStack(Item item) {
        if (mc.thePlayer == null || item == null) return null;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && item == stack.getItem()) {
                return stack;
            }
        }
        return null;
    }

    public static ItemStack getInventoryItemStack(Item item) {
        if (mc.thePlayer == null || item == null) return null;

        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && item == stack.getItem()) {
                return stack;
            }
        }
        return null;
    }

    public static ItemStack getInventoryItemStack(String name) {
        if (mc.thePlayer == null || name == null || name.isEmpty()) return null;

        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.hasDisplayName()) {
                String displayName = removeFormatting(stack.getDisplayName());
                if (displayName.equalsIgnoreCase(name)) {
                    return stack;
                }
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

    public static boolean isHoldingItem(Item item) {
        if (mc.thePlayer == null) return false;
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return (heldItem != null && heldItem.getItem() == item);
    }

    public static boolean isHoldingSupplies() {
        if (mc.thePlayer == null) return false;
        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(8);
        return (stack != null && stack.getDisplayName().contains("Elle's Supplies"));
    }

    public static boolean isPlayerKuudraDead() {
        if (mc.thePlayer == null) return false;
        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(1);
        return (stack != null && stack.getDisplayName().contains("Haunt"));
    }

    public static boolean isHoldingEtherwarpItem() {
        if (mc.thePlayer == null) return false;

        ItemStack heldItem = mc.thePlayer.getHeldItem();
        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(heldItem);
        if (extraAttributes == null || !extraAttributes.hasKey("id")) return false;

        String itemId = extraAttributes.getString("id");
        return "ETHERWARP_CONDUIT".equals(itemId) ||
                (("ASPECT_OF_THE_END".equals(itemId) || "ASPECT_OF_THE_VOID".equals(itemId)) &&
                        extraAttributes.hasKey("ethermerge") && extraAttributes.getInteger("ethermerge") == 1);
    }

    public static void closeScreen() {
        closeScreenInternal(null);
    }

    public static void closeScreen(int expectedWindowId) {
        closeScreenInternal(expectedWindowId);
    }

    private static void closeScreenInternal(Integer expectedWindowId) {
        if (!(mc.currentScreen instanceof GuiContainer)) return;
        if (mc.thePlayer == null || mc.thePlayer.openContainer == null) return;
        if (mc.thePlayer.openContainer == mc.thePlayer.inventoryContainer) return;

        final int capturedWindowId = mc.thePlayer.openContainer.windowId;
        if (expectedWindowId != null && capturedWindowId != expectedWindowId) return;

        mc.addScheduledTask(() -> {
            // Re-check at execution time to avoid closing a different window if it changed meanwhile
            if (!(mc.currentScreen instanceof GuiContainer)) return;
            if (mc.thePlayer == null || mc.thePlayer.openContainer == null) return;
            if (mc.thePlayer.openContainer == mc.thePlayer.inventoryContainer) return;

            final int currentWindowId = mc.thePlayer.openContainer.windowId;

            boolean ok = (expectedWindowId != null && currentWindowId == expectedWindowId) ||
                            (expectedWindowId == null && currentWindowId == capturedWindowId);
            if (!ok) return;

            mc.thePlayer.closeScreen();
        });
    }

    public static float getEyeHeight() {
        return PlayerUtils.isSneaking
                ? mc.thePlayer.getEyeHeight() - 0.08F
                : mc.thePlayer.getEyeHeight();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof C0BPacketEntityAction)) return;

        C0BPacketEntityAction actionPacket = (C0BPacketEntityAction) event.getPacket();
        if (actionPacket.getAction() == C0BPacketEntityAction.Action.START_SNEAKING) {
            isSneaking = true;
        } else if (actionPacket.getAction() == C0BPacketEntityAction.Action.STOP_SNEAKING) {
            isSneaking = false;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        isSneaking = false;
    }

    public static void clickSlot(Slot slot, int type, int mode) {
        clickSlotInternal(null, slot, null, type, mode, false);
    }

    public static void clickSlot(int slotNumber, int type, int mode) {
        clickSlotInternal(null, null, slotNumber, type, mode, false);
    }

    public static void clickSlot(int expectedWindowId, int slotNumber, int type, int mode, boolean allowEmpty) {
        clickSlotInternal(expectedWindowId, null, slotNumber, type, mode, allowEmpty);
    }

    public static void clickSlot(int expectedWindowId, Slot slot, int type, int mode, boolean allowEmpty) {
        clickSlotInternal(expectedWindowId, slot, null, type, mode, allowEmpty);
    }

    private static void clickSlotInternal(Integer expectedWindowId,
                                          Slot slotRef,
                                          Integer slotIndex,
                                          int type,
                                          int mode,
                                          boolean allowEmpty) {
        if (mc.thePlayer == null || mc.thePlayer.openContainer == null) return;
        Container container = mc.thePlayer.openContainer;

        if (expectedWindowId != null && container.windowId != expectedWindowId) return;
        if (container.inventorySlots == null || container.inventorySlots.isEmpty()) return;

        final Slot slot;
        final int idx;

        if (slotRef != null) {
            if (slotRef.slotNumber < 0 || slotRef.slotNumber >= container.inventorySlots.size()) return;

            if (!container.inventorySlots.contains(slotRef)) return;
            if (container.inventorySlots.get(slotRef.slotNumber) != slotRef) return;

            slot = slotRef;
            idx = slotRef.slotNumber;
        } else {
            if (slotIndex == null) return;
            int i = slotIndex;
            if (i < 0 || i >= container.inventorySlots.size()) return;

            Slot s = container.inventorySlots.get(i);
            if (s == null) return;

            slot = s;
            idx = i;
        }

        if (slot.inventory == null || slot.inventory == mc.thePlayer.inventory) return;
        if (!allowEmpty && !slot.getHasStack()) return;

        mc.playerController.windowClick(container.windowId, idx, type, mode, mc.thePlayer);
    }
}
