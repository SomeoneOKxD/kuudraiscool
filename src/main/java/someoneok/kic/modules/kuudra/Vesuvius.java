package someoneok.kic.modules.kuudra;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.events.InventoryOpenEvent;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.events.kuudra.KuudraChestOpenedEvent;
import someoneok.kic.events.kuudra.KuudraChestProfitUpdatedEvent;
import someoneok.kic.models.Island;
import someoneok.kic.models.kuudra.chest.KuudraChest;
import someoneok.kic.models.kuudra.chest.KuudraChestType;
import someoneok.kic.modules.misc.ButtonManager;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.RenderUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.kuudra.KuudraChestUtils;
import someoneok.kic.utils.overlay.OverlayManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraProfitTracker.*;
import static someoneok.kic.utils.ItemUtils.getItemLore;
import static someoneok.kic.utils.kuudra.KuudraChestUtils.*;

public class Vesuvius {
    private static final int[] CHEST_SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

    private final Map<Integer, KuudraChest> chestsById = new HashMap<>();
    private final List<Integer> unopenedSlots = new ArrayList<>();

    private boolean dirty = false;
    private int vesuviusWindowId = -1;
    public static boolean shouldRender = false;

    @SubscribeEvent
    public void onInventoryOpenEvent(InventoryOpenEvent.FullyOpened event) {
        if (!shouldRun() || !ApiUtils.isVerified()) return;

        String invName = event.getInventoryName();

        if ("Vesuvius".equals(invName) || "Croesus".equals(invName)) {
            vesuviusWindowId = event.getInventoryId();
            unopenedSlots.clear();
            unopenedSlots.addAll(getUnopenedChests(event.getInventoryItems()));
            shouldRender = true;
            return;
        }

        KuudraChest chest;
        if (invName.contains("Paid Chest")) chest = new KuudraChest(KuudraChestType.PAID);
        else if (invName.contains("Free Chest")) chest = new KuudraChest(KuudraChestType.FREE);
        else return;

        Map<Integer, ItemStack> invItems = event.getInventoryItems();
        if (!validBuyItem(invItems.get(31))) return;

        chestsById.put(event.getInventoryId(), chest);

        if (KuudraChestUtils.processChest(chest, invItems) && KICConfig.kuudraProfitCalculator) {
            final KuudraChest chestRef = chest;
            KICEventBus.post(new KuudraChestOpenedEvent(chestRef, false));
            chest.updateValues(() -> updateChestProfit(chestRef));
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!ApiUtils.isVerified() || !shouldRun()) return;
        if (!(event.getContainer() instanceof ContainerChest)) return;
        if (event.getSlot() == null || event.getSlot().getStack() == null) return;

        KuudraChest chest = chestsById.get(event.getContainer().windowId);
        if (chest == null || chest.isBought()) return;

        ItemStack stack = event.getSlot().getStack();
        int slotId = event.getSlotId();

        if (slotId == 31 && canBuy(chest, stack)) handleChestBuy(chest);
        else if (slotId == 50 && canReroll(stack) && !chest.isRerolled()) rerollChestTrigger(chest);
        else if (slotId == 51 && canRerollShard(stack) && !chest.isShardRerolled()) rerollShardTrigger(chest);
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        int windowId = event.getContainer().windowId;
        if (chestsById.remove(windowId) != null) resetOverlay();
        if (windowId == vesuviusWindowId) {
            vesuviusWindowId = -1;
            unopenedSlots.clear();
            shouldRender = false;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (chestsById.isEmpty()) return;
        chestsById.clear();
        vesuviusWindowId = -1;
        unopenedSlots.clear();
        shouldRender = false;
        resetOverlay();
    }

    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent e) {
        if (!KICConfig.highlightUnopenedChests) return;
        if (vesuviusWindowId == -1) return;
        if (unopenedSlots.isEmpty()) return;
        if (!(e.gui instanceof GuiChest)) return;
        if (!ButtonManager.isChecked("vesuviusHighlightUnopened")) return;

        GuiChest guiChest = (GuiChest) e.gui;
        Container container = guiChest.inventorySlots;

        if (container.windowId != vesuviusWindowId) return;

        for (int slotId : unopenedSlots) {
            if (slotId < 0 || slotId >= container.inventorySlots.size()) continue;
            Slot slot = container.inventorySlots.get(slotId);
            if (slot == null || slot.getHasStack() && slot.getStack() == null) continue;
            RenderUtils.highlight(new Color(0, 255, 13, 175), guiChest, slot);
        }
    }

    private void resetOverlay() {
        if (!dirty) return;
        OverlayManager.getOverlay("ProfitCalculator").updateText("");
        dirty = false;
    }

    private boolean shouldRun() {
        boolean inForgottenSkull = LocationUtils.currentIsland == Island.CRIMSON_ISLE
                && "Forgotten Skull".equals(LocationUtils.subArea);
        boolean inDungeonHub = LocationUtils.currentIsland == Island.DUNGEON_HUB
                && "Croesus".equals(LocationUtils.subArea);
        return KICConfig.kuudraProfitCalculator && (inForgottenSkull || inDungeonHub);
    }

    public static void handleChestBuy(KuudraChest chest) {
        if (chest == null || chest.isBought()) return;
        chest.setBought(true);
        if (chest.isProfitCalculated() && chest.markAddedToTracker()) onChestBought(chest);
    }

    public static void rerollChestTrigger(KuudraChest chest) {
        if (chest != null && !chest.isRerolled()) onChestReroll();
    }

    public static void rerollShardTrigger(KuudraChest chest) {
        if (chest != null && !chest.isShardRerolled()) onShardReroll();
    }

    private void updateChestProfit(KuudraChest chest) {
        if (chest == null) return;
        KICLogger.info("[UCP] Triggered");
        KICEventBus.post(new KuudraChestProfitUpdatedEvent(chest, false));

        if (chest.isBought() && chest.markAddedToTracker()) {
            long profit = chest.getTotalValue();
            KICLogger.info("[UCP] Updating tracker with bought chest total value: " + profit);
            onChestBought(chest);
            return;
        }

        mc.addScheduledTask(() -> {
            if (mc.thePlayer == null) return;
            Container current = mc.thePlayer.openContainer;
            if (current == null || chestsById.get(current.windowId) != chest) {
                resetOverlay();
                return;
            }
            OverlayManager.getOverlay("ProfitCalculator").updateText(getProfitText(chest));
            dirty = true;
        });
    }

    private List<Integer> getUnopenedChests(Map<Integer, ItemStack> items) {
        List<Integer> unopened = new ArrayList<>();

        for (int slotId : CHEST_SLOTS) {
            ItemStack item = items.get(slotId);
            if (item == null) continue;

            if (!Items.skull.equals(item.getItem())) continue;

            List<String> lore = getItemLore(item);
            if (lore == null || lore.isEmpty()) continue;

            boolean isUnopened = false;
            for (String line : lore) {
                if (line.contains("Chests expire in ")) {
                    isUnopened = true;
                    break;
                }
            }

            if (isUnopened) unopened.add(slotId);
        }

        return unopened;
    }
}
