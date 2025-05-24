package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.models.Island;
import someoneok.kic.models.crimson.AttributeItemValue;
import someoneok.kic.models.crimson.AuctionItemValue;
import someoneok.kic.models.crimson.BazaarItemValue;
import someoneok.kic.models.kuudra.KuudraChest;
import someoneok.kic.models.kuudra.KuudraKey;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.GeneralUtils;
import someoneok.kic.utils.ItemUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.OverlayManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraProfitTracker.onChestBought;
import static someoneok.kic.modules.kuudra.KuudraProfitTracker.onReroll;
import static someoneok.kic.utils.GeneralUtils.clickSlot;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.*;

public class KuudraProfitCalculator {
    private static final Pattern ESSENCE_REGEX = Pattern.compile("§d(?<type>\\w+) Essence §8x(?<count>\\d+)");
    private KuudraChest currentChest;
    private boolean rerolled = false;
    private boolean rerollReset = false;
    private boolean bought = false;

    private boolean profitCalculated = false;
    private boolean profitTrackerUpdated = false;
    private boolean instaOpened = false;
    private KuudraChest boughtChest = null;
    private KuudraChest instaBoughtChest = null;

    @SubscribeEvent
    public void onGUIDrawnEvent(GuiContainerEvent.ForegroundDrawnEvent event) {
        if (!KICConfig.kuudraProfitCalculator
                || LocationUtils.currentIsland != Island.KUUDRA
                || !(event.getContainer() instanceof ContainerChest)
                || !ApiUtils.isVerified()) return;

        IInventory inv = ((ContainerChest) event.getContainer()).getLowerChestInventory();

        if (event.getChestName().endsWith(" Chest")) {
            KuudraChest chestType = KuudraChest.getFromName(event.getChestName()).orElse(null);
            if (chestType == null) return;

            ItemStack openChest = inv.getStackInSlot(31);
            if (openChest == null || !validBuyItem(openChest)) return;

            if (chestType.hasItems() || (chestType == KuudraChest.FREE && chestType.getRawEssence() != 0)) {
                changeType(chestType);
            } else {
                try {
                    KuudraKey key = getKeyNeeded(ItemUtils.getItemLore(openChest));
                    chestType.setKeyNeeded(key);

                    for (int i = 9; i <= 17; i++) {
                        ItemStack lootSlot = inv.getStackInSlot(i);
                        if (lootSlot == null) continue;

                        String lootDisplayName = lootSlot.getDisplayName();
                        int essence = getCrimsonEssenceCount(lootDisplayName);
                        if (essence != -1) {
                            chestType.setEssence(essence);
                            continue;
                        }

                        NBTTagCompound itemNBT = lootSlot.writeToNBT(new NBTTagCompound());
                        if (itemNBT.hasKey("id")) {
                            String id = itemNBT.getString("id");

                            if (id == null || id.isEmpty() ||
                                    id.equals("minecraft:stained_glass_pane") ||
                                    id.equals("minecraft:chest") ||
                                    id.equals("minecraft:barrier")) {
                                continue;
                            }

                            chestType.addItem(lootSlot.copy());
                        }
                    }
                } catch (Exception e) {
                    KICLogger.error(e.getMessage());
                }
            }
        }
    }

    private void changeType(KuudraChest kuudraChest) {
        if (currentChest != kuudraChest) {
            KICLogger.info("[CT] Chest opened: " + kuudraChest.getDisplayText());
            currentChest = kuudraChest;
            currentChest.updateValues(this::updateChestProfit);
            instaOpen();
        }
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        if (rerolled && !rerollReset) {
            KICLogger.info("[OCC] Chest was rerolled, resetting current chest.");
            rerollReset = true;
            profitCalculated = false;
            if (currentChest != null) currentChest.reset();
        }
        reset();
    }

    private void reset() {
        currentChest = null;
        OverlayManager.getOverlay("ProfitCalculator").updateText("");
    }

    private void closeChest() {
        if (!KICConfig.ACAutoCloseGui) return;
        GeneralUtils.closeScreen(null);
    }

    private KuudraKey getKeyNeeded(List<String> lore) {
        for (int i = 0; i < lore.size() - 1; i++) {
            String line = lore.get(i);

            if (line.equals("§7Cost")) {
                String cost = lore.get(i + 1);

                if (cost.equals("§aThis Chest is Free!")) {
                    return null;
                }

                String strippedCost = removeFormatting(cost);

                for (KuudraKey key : KuudraKey.values()) {
                    if (key.getDisplayName().equals(strippedCost)) {
                        return key;
                    }
                }

                throw new IllegalStateException("Could not find key needed for chest");
            }
        }

        throw new IllegalStateException("Could not find key needed for chest");
    }

    private int getCrimsonEssenceCount(String text) {
        Matcher matcher = ESSENCE_REGEX.matcher(text);
        if (!matcher.matches()) {
            return -1;
        }

        String type = matcher.group("type");
        String countStr = matcher.group("count");

        if (countStr == null || !"crimson".equalsIgnoreCase(type)) {
            return -1;
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            return -1;
        }

        return count;
    }

    private void updateChestProfit() {
        KICLogger.info("[UCP] Triggered");
        profitCalculated = true;
        if (instaOpened) {
            KICLogger.info(String.format("[UCP] profitTrackerUpdated? %s | bought? %s | instaBoughtChest? %s", profitTrackerUpdated, bought, instaBoughtChest == null ? "null" : instaBoughtChest.getDisplayText()));
            if (!profitTrackerUpdated && bought && instaBoughtChest != null) {
                profitTrackerUpdated = true;
                long profit = instaBoughtChest.getTotalValue(rerolled);
                KICLogger.info("[UCP] Updating tracker with insta-bought chest total value: " + profit);
                onChestBought(instaBoughtChest, rerolled);
                sendMessageToPlayer(String.format("%s §aAdded %s to profit tracker from insta-bought chest.", KIC.KICPrefix, parseToShorthandNumber(profit)));
                handleNotification(profit);
            }
        } else {
            KICLogger.info(String.format("[UCP] profitTrackerUpdated? %s | bought? %s | boughtChest? %s", profitTrackerUpdated, bought, boughtChest == null ? "null" : boughtChest.getDisplayText()));
            if (!profitTrackerUpdated && bought && boughtChest != null) {
                profitTrackerUpdated = true;
                long profit = boughtChest.getTotalValue(rerolled);
                KICLogger.info("[UCP] Updating tracker with already bought chest total value: " + profit);
                onChestBought(boughtChest, rerolled);
                sendMessageToPlayer(String.format("%s §aAdded %s to profit tracker from manually bought chest.", KIC.KICPrefix, parseToShorthandNumber(profit)));
                handleNotification(profit);
            } else {
                KICLogger.info("[UCP] Triggering autoChest");
                autoChest();
            }
        }

        if (currentChest == null) return;
        KICLogger.info("[UCP] currentChest available");

        StringBuilder text = new StringBuilder(String.format("%s §a§lChest Profit\n", KIC.KICPrefix));

        // Total profit
        long totalProfit = currentChest.getTotalValue(rerolled);
        String totalPrefix = totalProfit > 0 ? "§a+" : "§c";
        text.append("\n").append("§eTotal: §r")
                .append(totalPrefix)
                .append(parseToShorthandNumber(totalProfit))
                .append("\n");

        // Key
        KuudraKey key = currentChest.getKeyNeeded();
        if (key != null) {
            text.append("\n").append("§c-")
                    .append(parseToShorthandNumber(key.getPrice()))
                    .append(" §7| ")
                    .append(key.getRarity().getColorCode())
                    .append(key.getDisplayName());
        }

        // Sorted item value from highest to lowest
        Map<String, Long> items = new HashMap<>();

        currentChest.getValues().values().forEach(value -> {
            String name = null;
            long itemValue = 0;
            if (value instanceof BazaarItemValue) {
                BazaarItemValue item = (BazaarItemValue) value;
                if ("KUUDRA_TEETH".equals(item.getItemId()) && KuudraProfitCalculatorOptions.ignoreTeeth) return;
                itemValue = item.getValue();
                name = item.getName();
                if (item.getItemCount() > 1) {
                    name += " §ex" + item.getItemCount();
                }
            } else if (value instanceof AuctionItemValue) {
                AuctionItemValue item = (AuctionItemValue) value;
                itemValue = item.getValue();
                name = item.getName();

                if ("RUNIC_STAFF".equals(item.getItemId()) && KuudraProfitCalculatorOptions.ignoreAuroraStaff) {
                    name = "§f§m" + removeFormatting(item.getName());
                }
            } else if (value instanceof AttributeItemValue) {
                AttributeItemValue item = (AttributeItemValue) value;
                name = item.getFullName();
                itemValue = item.getValue(currentChest.getEssenceValue());

                if (item.isUsingCustomValue(currentChest.getEssenceValue())) {
                    if ("ATTRIBUTE_SHARD".equals(item.getItemId())) {
                        name = "§f§m" + removeFormatting(item.getFullName());
                    } else {
                        name = "§e§m" + removeFormatting(item.getFullName());
                    }
                }

                if ("HOLLOW_WAND".equals(item.getItemId()) && KuudraProfitCalculatorOptions.ignoreHollowWands) {
                    name = "§f§m" + removeFormatting(item.getFullName());
                }
            }
            if (!isNullOrEmpty(name)) {
                items.put(name, itemValue);
            }
        });

        int essence = currentChest.getEssence();
        if (essence > 0) {
            BazaarItemValue essenceValue = currentChest.getEssenceValue();
            long value = essenceValue.getValue() * essence;
            String name = essenceValue.getName() + " §ex" + essence;
            items.put(name, value);
        }

        items.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    String name = entry.getKey();
                    long value = entry.getValue();
                    String prefix = value > 0 ? "§a+" : "§c";

                    text.append("\n").append(prefix)
                            .append(parseToShorthandNumber(value))
                            .append(" §7| ")
                            .append(name);
                });

        if (rerolled) {
            BazaarItemValue kismetValue = currentChest.getKismetValue();
            long value = kismetValue.getValue();
            text.append("\n").append("§c-")
                    .append(parseToShorthandNumber(value))
                    .append(" §7| ")
                    .append(kismetValue.getName());
        } else if (KuudraProfitCalculatorOptions.rerollNotifier && currentChest.shouldReroll()) {
            text.append("\n\n").append("§a§l-= REROLL THIS CHEST! =-");
        }

        OverlayManager.getOverlay("ProfitCalculator").updateText(text.toString());
    }

    private void instaOpen() {
        if (profitCalculated
                || bought
                || !KICConfig.ACInstaBuy
                || currentChest == null
                || currentChest == KuudraChest.FREE
                || instaOpened) return;

        Container container = KIC.mc.thePlayer.openContainer;
        if (!(container instanceof ContainerChest)) return;

        String chestName = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
        if (!chestName.equals(currentChest.getDisplayText())) return;

        Slot slot31 = container.getSlot(31);

        boolean validBuy = slot31 != null && slot31.getHasStack() && validBuyItem(slot31.getStack()) && canBuy(currentChest);
        if (!validBuy) return;

        KICLogger.info("[IO] InstaOpen triggered.");
        instaOpened = true;
        int delay = KICConfig.ACInitialInstaBuyDelay + KIC.RNG.nextInt(51);

        scheduleClick(slot31, delay, () -> {
            instaBoughtChestTrigger();
            Multithreading.schedule(this::closeChest, KICConfig.ACAutoCloseDelay, TimeUnit.MILLISECONDS);
        });
    }

    private void autoChest() {
        if (!KICConfig.ACAutoReroll && !KICConfig.ACAutoBuy) return;
        if (currentChest == null || instaOpened) return;
        KICLogger.info("[AC] AutoChest Triggered");

        Container container = KIC.mc.thePlayer.openContainer;
        if (!(container instanceof ContainerChest)) return;

        String chestName = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
        if (!chestName.equals(currentChest.getDisplayText())) return;

        Slot slot31 = container.getSlot(31);
        Slot slot50 = container.getSlot(50);

        boolean validBuy = slot31 != null && slot31.getHasStack() && validBuyItem(slot31.getStack()) && canBuy(currentChest);
        boolean validReroll = slot50 != null && slot50.getHasStack() && validRerollItem(slot50.getStack());

        boolean shouldBuy = currentChest.shouldBuy();
        boolean shouldReroll = currentChest.shouldReroll();

        int delay1 = 450 + KIC.RNG.nextInt(151);
        int delay2 = 250 + KIC.RNG.nextInt(101);

        KICLogger.info(String.format("[AC] Variable checks: Chest: %s | Should Reroll: %b | Should Buy: %b | Valid Buy: %b | Valid Reroll: %b | Bought: %b | Rerolled: %b",
                chestName, shouldReroll, shouldBuy, validBuy, validReroll, bought, rerolled));

        if (shouldReroll && KICConfig.ACAutoReroll && !rerolled) {
            if (!validReroll) {
                KICLogger.info("[AC] Chest was not auto-rerolled because the slot is invalid.");
                sendMessageToPlayer(KIC.KICPrefix + " §aCannot auto-reroll chest: missing a Kismet Feather or the chest has already been rerolled.");
                return;
            }
            if (KICConfig.ACAutoBuy && !validBuy) {
                KICLogger.info("[AC] Chest was not auto-bought because the required key is missing.");
                sendMessageToPlayer(KIC.KICPrefix + " §aCannot auto-buy chest: missing the required key.");
                return;
            }

            if (KICConfig.ACAutoBuy) {
                KICLogger.info("[AC] Auto-rerolling and auto-buying the chest!");
                scheduleClick(slot31, delay1, () -> scheduleClick(slot50, delay2, this::rerollChestTrigger));
                sendMessageToPlayer(KIC.KICPrefix + " §aAuto-rerolled & auto-bought the paid chest!");
            } else {
                KICLogger.info("[AC] Auto-rerolling the chest!");
                scheduleClick(slot50, delay1, this::rerollChestTrigger);
                sendMessageToPlayer(KIC.KICPrefix + " §aAuto-rerolled the paid chest!");
            }
            return;
        }

        if (shouldBuy && KICConfig.ACAutoBuy && !bought) {
            if (!validBuy) {
                KICLogger.info("[AC] Chest was not auto-bought because the required key is missing.");
                sendMessageToPlayer(KIC.KICPrefix + " §aCannot auto-buy chest: missing the required key.");
                return;
            }
            KICLogger.info("[AC] Auto-buying the chest!");
            scheduleClick(slot31, delay1, () -> {
                boughtChestTrigger(false);
                Multithreading.schedule(this::closeChest, KICConfig.ACAutoCloseDelay, TimeUnit.MILLISECONDS);
            });
            long profit = currentChest.getTotalValue(false);
            sendMessageToPlayer(KIC.KICPrefix + String.format(" §aAuto-bought the paid chest! (Profit: %s)",
                    parseToShorthandNumber(profit)));
            handleNotification(profit);
            return;
        }

        KICLogger.info(String.format("[AC] Chest not auto-bought. Profit: %d, Min Buy Threshold: %d",
                currentChest.getTotalValue(rerolled), KICConfig.ACAutoBuyMinProfit));
        sendMessageToPlayer(KIC.KICPrefix + String.format(" §aChest not auto-bought as total profit (%s) was below the threshold (%s).",
                parseToShorthandNumber(currentChest.getTotalValue(rerolled)), parseToShorthandNumber(KICConfig.ACAutoBuyMinProfit)));
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        for (KuudraChest chest : KuudraChest.values()) {
            chest.reset();
        }
        rerolled = false;
        rerollReset = false;
        bought = false;
        instaOpened = false;
        profitCalculated = false;
        profitTrackerUpdated = false;
        boughtChest = null;
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (LocationUtils.currentIsland != Island.KUUDRA
                || !(event.getContainer() instanceof ContainerChest)
                || !event.getChestName().endsWith(" Chest")
                || !ApiUtils.isVerified()) return;

        KICLogger.info(String.format("[OSC] Processing slot clicked event. Clicked Button: %d | Click Type: %d", event.getClickedButton(), event.getClickType()));
        KICLogger.info(String.format("[OSC] Slot %d clicked | Slot null? %s | Stack null? %s", event.getSlotId(), event.getSlot() == null, event.getSlot() != null && event.getSlot().getStack() != null));

        if (event.getSlot() == null || event.getSlot().getStack() == null) return;

        ItemStack itemStack = event.getSlot().getStack();
        KICLogger.info(String.format("[OSC] Stack: %s | Stack tag: %s", itemStack.getDisplayName(), itemStack.getTagCompound().toString()));

        KICLogger.info(String.format("[OSC] Phase: %d | currentChest null? %s | currentChest: %s", Kuudra.currentPhase, currentChest == null, currentChest != null ? currentChest.getDisplayText() : "null"));
        KICLogger.info(String.format("[OSC] bought? %s | rerolled? %s | profitTrackerUpdated? %s", bought, rerolled, profitTrackerUpdated));
        if (event.getSlotId() >= 9 && event.getSlotId() <= 17) {
            // Cancel clicking items in chest
            KICLogger.info("[OSC] Item slot clicked, canceling event");
            event.setCanceled(true);
        } else if (event.getSlotId() == 31 && Kuudra.currentPhase == 8 && canBuy(currentChest) && !bought) {
            // Bought chest stuff
            KICLogger.info("[OSC] Buy slot clicked, processing buy...");
            boughtChestTrigger(true);
        } else if (event.getSlotId() == 50 && Kuudra.currentPhase == 8 && validRerollItem(itemStack) && !rerolled) {
            // Reroll chest stuff
            KICLogger.info("[OSC] Reroll slot clicked, processing reroll...");
            rerollChestTrigger();
        }
    }

    private boolean validRerollItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        return itemStack.getDisplayName().equals("§aReroll Kuudra Chest");
    }

    private boolean validBuyItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        return itemStack.getDisplayName().equals("§aOpen Reward Chest");
    }

    private boolean canBuy(KuudraChest chest) {
        if (chest == null) return false;

        KuudraKey key = chest.getKeyNeeded();
        if (chest == KuudraChest.FREE || key == null) return true;

        IInventory inventory = KIC.mc.thePlayer.inventory;
        String requiredKeyName = key.getDisplayName();

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack != null && itemStack.hasDisplayName()) {
                if (requiredKeyName.equals(removeFormatting(itemStack.getDisplayName()))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void scheduleClick(Slot slot, int delay, Runnable callback) {
        if (slot == null || !slot.getHasStack() || slot.inventory == mc.thePlayer.inventory) {
            KICLogger.info("[SC] Skipping click: Slot is invalid");
            return;
        }

        KICLogger.info(String.format("[SC] Scheduling click | Slot: %d | Delay: %dms",
                slot.getSlotIndex(), delay));

        Multithreading.schedule(() -> {
            clickSlot(slot, 0, 0);
            KICLogger.info(String.format("[SC] Clicked slot: %d", slot.getSlotIndex()));
            if (callback != null) {
                callback.run();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void instaBoughtChestTrigger() {
        if (currentChest == null || bought) return;

        bought = true;
        instaBoughtChest = currentChest;
        KICLogger.info("[IBCT] Insta-bought chest: " + instaBoughtChest.getDisplayText());

        if (profitCalculated) {
            profitTrackerUpdated = true;
            long profit = instaBoughtChest.getTotalValue(false);
            KICLogger.info("[IBCT] Profit tracker updated. Calling onChestBought().");
            sendMessageToPlayer(KIC.KICPrefix + String.format(" §aInsta-bought the paid chest! (Profit: %s)",
                    parseToShorthandNumber(profit)));
            onChestBought(instaBoughtChest, rerolled);
            handleNotification(profit);
        } else {
            KICLogger.info("[IBCT] Profit tracker not updated. Waiting for profit to be calculated...");
        }
    }

    private void boughtChestTrigger(boolean manual) {
        if (currentChest == null || bought) return;

        bought = true;
        boughtChest = currentChest;
        KICLogger.info("[BCT] Bought chest: " + boughtChest.getDisplayText());

        if (profitCalculated) {
            profitTrackerUpdated = true;
            KICLogger.info("[BCT] Profit tracker updated. Calling onChestBought().");
            onChestBought(boughtChest, rerolled);
            if (manual) {
                long profit = boughtChest.getTotalValue(rerolled);
                sendMessageToPlayer(String.format("%s §aAdded %s to profit tracker from manually bought chest.", KIC.KICPrefix, parseToShorthandNumber(profit)));
                handleNotification(profit);
            }
        }
    }

    private void rerollChestTrigger() {
        if (currentChest == null || rerolled) return;

        rerolled = true;
        KICLogger.info("[RCT] Chest rerolled.");

        onReroll();
    }

    private void handleNotification(long totalValue) {
        if (!KICConfig.kuudraNotiTotalProfit) return;

        String prefix = totalValue >= 0 ? "§a+" : "§c";
        String message = "§aTotal Profit: " + prefix + parseToShorthandNumber(totalValue);

        Notifications.showMessage(message, KICConfig.kuudraNotiTotalProftTime);
    }
}
