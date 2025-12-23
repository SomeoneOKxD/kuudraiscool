package someoneok.kic.modules.misc;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.data.DataHandler;
import someoneok.kic.utils.data.DataManager;

import java.util.Map;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.ChatUtils.sendCommand;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.LocationUtils.onSkyblock;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class TrackEmptySlots {
    private static Map<Integer, Integer> enderChestPages() { return DataManager.getEmptySlotData().getEnderChestPages(); }
    private static Map<Integer, Integer> backpacks() { return DataManager.getEmptySlotData().getBackpacks(); }
    private int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!shouldProcess(event)) return;
        ticks = 0;

        if (!(mc.currentScreen instanceof GuiChest)) return;

        GuiChest guiChest = (GuiChest) mc.currentScreen;
        if (!(guiChest.inventorySlots instanceof ContainerChest)) return;

        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        String chestName = container.getLowerChestInventory().getName();
        if (!isEnderChestOrBackpack(chestName) || container.inventorySlots == null || container.inventorySlots.isEmpty()) return;

        int totalEmptySlots = (int) container.inventorySlots.stream()
                .filter(slot -> !slot.getHasStack() && slot.slotNumber > 8 && slot.inventory != mc.thePlayer.inventory)
                .count();

        try {
            if (chestName.contains("Slot #")) {
                int slotIndex = extractIndex(chestName, "#", ")");
                backpacks().put(slotIndex, totalEmptySlots);
            } else if (chestName.contains("Ender Chest (") && chestName.contains("/")) {
                int pageIndex = extractIndex(chestName, "(", "/");
                enderChestPages().put(pageIndex, totalEmptySlots);
            }
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        if (!ApiUtils.isVerified() || !KICConfig.trackEmptyECBP || !(event.getContainer() instanceof ContainerChest)) return;

        String chestName = event.getChestName();
        if (chestName == null) return;

        chestName = removeFormatting(chestName);
        if (chestName.startsWith("Ender Chest") || chestName.contains("Backpack (Slot #")) {
            DataHandler.saveData();
        }
    }

    private boolean shouldProcess(TickEvent.ClientTickEvent event) {
        return ApiUtils.isVerified()
                && KICConfig.trackEmptyECBP
                && onSkyblock
                && mc.theWorld != null
                && mc.thePlayer != null
                && event.phase == TickEvent.Phase.END
                && (++ticks % 5 == 0);
    }

    private boolean isEnderChestOrBackpack(String name) {
        return name != null && (name.contains("Backpack") || name.startsWith("Ender Chest ("));
    }

    private int extractIndex(String source, String from, String to) {
        return Integer.parseInt(source.split(Pattern.quote(from))[1].split(Pattern.quote(to))[0].trim());
    }

    public static void openEmptyEcOrBp() {
        if (!KICConfig.trackEmptyECBP || (!KICConfig.trackEnderChestPages && !KICConfig.trackBackpacks)) return;

        if (KICConfig.trackEnderChestPages && enderChestPages().isEmpty()) {
            sendMessageToPlayer(KICPrefix + " §aPlease navigate through your enabled Ender Chest pages so they can be scanned.");
            return;
        }

        if (KICConfig.trackBackpacks && backpacks().isEmpty()) {
            sendMessageToPlayer(KICPrefix + " §aPlease navigate through your enabled Backpacks so they can be scanned.");
            return;
        }

        if (KICConfig.trackEnderChestPages) {
            for (int ec = 1; ec <= 9; ec++) {
                if (isECPageEnabled(ec)) {
                    Integer emptySlots = enderChestPages().get(ec);
                    if (emptySlots != null && emptySlots > 0) {
                        sendCommand("/enderchest " + ec);
                        return;
                    }
                }
            }
        }

        if (KICConfig.trackBackpacks) {
            for (int bp = 1; bp <= 18; bp++) {
                if (isBPPageEnabled(bp)) {
                    Integer emptySlots = backpacks().get(bp);
                    if (emptySlots != null && emptySlots > 0) {
                        sendCommand("/backpack " + bp);
                        return;
                    }
                }
            }
        }

        String storageTypeMissing = null;
        if (KICConfig.trackEnderChestPages && KICConfig.trackBackpacks) {
            storageTypeMissing = "Ender Chest or Backpack";
        } else if (KICConfig.trackEnderChestPages) {
            storageTypeMissing = "Ender Chest";
        } else if (KICConfig.trackBackpacks) {
            storageTypeMissing = "Backpack";
        }

        if (storageTypeMissing != null) {
            sendMessageToPlayer(KICPrefix + " §cNo enabled " + storageTypeMissing + " with empty slots found.");
        }
    }

    private static boolean isECPageEnabled(int page) {
        switch (page) {
            case 1: return KICConfig.ec1;
            case 2: return KICConfig.ec2;
            case 3: return KICConfig.ec3;
            case 4: return KICConfig.ec4;
            case 5: return KICConfig.ec5;
            case 6: return KICConfig.ec6;
            case 7: return KICConfig.ec7;
            case 8: return KICConfig.ec8;
            case 9: return KICConfig.ec9;
            default: return false;
        }
    }

    private static boolean isBPPageEnabled(int slot) {
        switch (slot) {
            case 1: return KICConfig.bp1;
            case 2: return KICConfig.bp2;
            case 3: return KICConfig.bp3;
            case 4: return KICConfig.bp4;
            case 5: return KICConfig.bp5;
            case 6: return KICConfig.bp6;
            case 7: return KICConfig.bp7;
            case 8: return KICConfig.bp8;
            case 9: return KICConfig.bp9;
            case 10: return KICConfig.bp10;
            case 11: return KICConfig.bp11;
            case 12: return KICConfig.bp12;
            case 13: return KICConfig.bp13;
            case 14: return KICConfig.bp14;
            case 15: return KICConfig.bp15;
            case 16: return KICConfig.bp16;
            case 17: return KICConfig.bp17;
            case 18: return KICConfig.bp18;
            default: return false;
        }
    }
}
