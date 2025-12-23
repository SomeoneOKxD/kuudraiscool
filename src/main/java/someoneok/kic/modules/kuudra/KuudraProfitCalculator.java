package someoneok.kic.modules.kuudra;

import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.events.InventoryOpenEvent;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.events.kuudra.KuudraChestOpenedEvent;
import someoneok.kic.events.kuudra.KuudraChestProfitUpdatedEvent;
import someoneok.kic.models.Island;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.chest.KuudraChest;
import someoneok.kic.models.kuudra.chest.KuudraChestType;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.kuudra.KuudraChestUtils;
import someoneok.kic.utils.kuudra.KuudraValueCache;
import someoneok.kic.utils.overlay.OverlayManager;

import java.util.Map;

import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.modules.kuudra.KuudraProfitTracker.*;
import static someoneok.kic.utils.ChatUtils.createHoverComponent;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;
import static someoneok.kic.utils.kuudra.KuudraChestUtils.*;

public class KuudraProfitCalculator {
    private static KuudraChest paidChest = new KuudraChest(KuudraChestType.PAID);
    private static KuudraChest freeChest = new KuudraChest(KuudraChestType.FREE);

    private static KuudraChest currentChest = null;
    private static KuudraChest boughtChest = null;

    private static boolean rerollReset = false;
    private static boolean shardRerollReset = false;
    private static boolean manuallyBought = false;
    private static boolean dirty = false;

    @SubscribeEvent
    public void onInventoryOpenEvent(InventoryOpenEvent.FullyOpened event) {
        if (LocationUtils.currentIsland != Island.KUUDRA || !ApiUtils.isVerified()) return;

        String invName = event.getInventoryName();
        KuudraChest chest;
        if (invName.contains("Paid Chest")) chest = paidChest;
        else if (invName.contains("Free Chest")) chest = freeChest;
        else return;

        Map<Integer, ItemStack> invItems = event.getInventoryItems();
        if (!validBuyItem(invItems.get(31))) return;

        if (!(chest.hasItems() || (chest.getType() == KuudraChestType.FREE && chest.getEssenceRaw() != 0))) {
            if (!KuudraChestUtils.processChest(chest, invItems)) return;
            dirty = true;
        }
        changeType(chest);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (LocationUtils.currentIsland != Island.KUUDRA
                || !(event.getContainer() instanceof ContainerChest)
                || !ApiUtils.isVerified()
                || event.getSlot() == null
                || event.getSlot().getStack() == null
                || currentChest == null
                || currentChest.isBought()) return;

        KuudraPhase currentPhase = phase();
        if (currentPhase != KuudraPhase.END || currentChest.isBought()) return;

        String chestName = event.getChestName();
        if (!chestName.contains("Paid Chest") && !chestName.contains("Free Chest")) return;

        ItemStack stack = event.getSlot().getStack();
        int slotId = event.getSlotId();

        if (slotId == 31 && canBuy(currentChest, stack)) handleChestBuy(true);
        else if (slotId == 50 && canReroll(stack) && !currentChest.isRerolled()) rerollChestTrigger();
        else if (slotId == 51 && canRerollShard(stack) && !currentChest.isShardRerolled()) rerollShardTrigger();
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        if (currentChest != null) {
            if (currentChest.isRerolled() && !rerollReset) {
                rerollReset = true;
                currentChest.reset();
            }
            if (currentChest.isShardRerolled() && !shardRerollReset) {
                shardRerollReset = true;
                currentChest.reset();
            }
            currentChest = null;
            OverlayManager.getOverlay("ProfitCalculator").updateText("");
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (dirty) {
            paidChest = new KuudraChest(KuudraChestType.PAID);
            freeChest = new KuudraChest(KuudraChestType.FREE);
            rerollReset = false;
            shardRerollReset = false;
            manuallyBought = false;
            boughtChest = null;
            currentChest = null;
            dirty = false;
            KuudraValueCache.pruneExpired();
        }
    }

    private void changeType(KuudraChest kuudraChest) {
        if (currentChest != kuudraChest) {
            currentChest = kuudraChest;
            KICEventBus.post(new KuudraChestOpenedEvent(currentChest, true));
            if (KICConfig.kuudraProfitCalculator) currentChest.updateValues(this::updateChestProfit);
        }
    }

    public static void handleChestBuy(boolean manual) {
        if (currentChest == null || currentChest.isBought()) return;

        manuallyBought = manual;
        currentChest.setBought(true);
        boughtChest = currentChest;

        if (currentChest.isProfitCalculated() && currentChest.markAddedToTracker()) {
            long profit = boughtChest.getTotalValue();
            onChestBought(boughtChest);
            sendProfitMessage(profit);
            handleNotification(profit);
        }
    }

    public static void rerollChestTrigger() {
        if (currentChest != null && !currentChest.isRerolled()) onChestReroll();
    }

    public static void rerollShardTrigger() {
        if (currentChest != null && !currentChest.isShardRerolled()) onShardReroll();
    }

    private void updateChestProfit() {
        KICEventBus.post(new KuudraChestProfitUpdatedEvent(currentChest, true));

        if (boughtChest != null && boughtChest.isBought() && boughtChest.markAddedToTracker()) {
            long profit = boughtChest.getTotalValue();
            onChestBought(boughtChest);
            sendProfitMessage(profit);
            handleNotification(profit);
            return;
        }

        if (currentChest != null) {
            OverlayManager.getOverlay("ProfitCalculator").updateText(getProfitText(currentChest));
        }
    }

    private static void sendProfitMessage(long profit) {
        sendMessageToPlayer(createHoverComponent(true, String.format(
                "%s §aTracked %s profit from %s bought chest.",
                KIC.KICPrefix, parseToShorthandNumber(profit), manuallyBought ? "manually" : "auto"
        ), getProfitText(boughtChest)));
    }

    private static void handleNotification(long totalValue) {
        if (!KICConfig.kuudraNotiTotalProfit) return;
        String message = "§aTotal Profit: " + (totalValue >= 0 ? "§a+" : "§c") + parseToShorthandNumber(totalValue);
        Notifications.showMessage(message, KICConfig.kuudraNotiTotalProftTime);
    }

    public static KuudraChest getCurrentChest() { return currentChest; }
    public static boolean hasBoughtChest() { return boughtChest != null; }
}
