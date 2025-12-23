package someoneok.kic.addons;

import cc.polyfrost.oneconfig.utils.IOUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.Event;
import someoneok.kic.KIC;
import someoneok.kic.api.AddonContext;
import someoneok.kic.api.AddonEventBridge;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.models.Island;
import someoneok.kic.models.crimson.AuctionItemValue;
import someoneok.kic.models.crimson.BazaarItemValue;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.chest.KuudraChest;
import someoneok.kic.modules.kuudra.KuudraPhaseTracker;
import someoneok.kic.modules.kuudra.KuudraProfitCalculator;
import someoneok.kic.modules.kuudra.Pearls;
import someoneok.kic.modules.misc.ServerTickScheduler;
import someoneok.kic.utils.*;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.kuudra.KuudraUtils;
import someoneok.kic.utils.kuudra.KuudraValueCache;
import someoneok.kic.utils.mouse.LockMouseLook;
import someoneok.kic.utils.overlay.MovableOverlay;
import someoneok.kic.utils.overlay.OverlayManager;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AddonContextImpl implements AddonContext {
    private final File addonDir;
    private final File configFile;
    private final File dataFile;

    public AddonContextImpl(String id, File addonDataRoot) {
        this.addonDir = new File(addonDataRoot, id);
        this.configFile = new File(addonDir, "config.json");
        this.dataFile   = new File(addonDir, "data.json");
    }

    @Override public File addonDir() { return addonDir; }
    @Override public File configFile() { return configFile; }
    @Override public File dataFile() { return dataFile; }

    @Override public void registerEventHandler(Object h) { AddonEventBridge.register(h); }
    @Override public void unregisterEventHandler(Object h) { AddonEventBridge.unregister(h); }
    @Override public boolean post(Event e) { return KICEventBus.post(e); }

    @Override
    public void registerCommand(Object command) {
        if (command instanceof ICommand) {
            ForgeCommandUtils.registerCommand((ICommand) command);
        } else {
            throw new IllegalArgumentException("Command must implement ICommand");
        }
    }
    @Override
    public void unregisterCommand(Object command) {
        if (command instanceof ICommand) {
            ForgeCommandUtils.removeCommand((ICommand) command);
        } else {
            throw new IllegalArgumentException("Command must implement ICommand");
        }
    }

    @Override public String kicPrefix() { return KIC.KICPrefix; }
    @Override public Minecraft mc() { return KIC.mc; }
    @Override public Random rng() { return KIC.RNG; }
    @Override public boolean isVerifiedUser() { return ApiUtils.isVerified(); }
    @Override public boolean onSkyblock() { return LocationUtils.onSkyblock; }
    @Override public String serverName() { return LocationUtils.serverName; }
    @Override public Island currentIsland() { return LocationUtils.currentIsland; }
    @Override public boolean inKuudra() { return LocationUtils.inKuudra(); }
    @Override public boolean isPlayerKuudraDead() { return PlayerUtils.isPlayerKuudraDead(); }
    @Override public boolean isPlayerSneaking() { return PlayerUtils.isSneaking; }
    @Override public boolean isPickingUpSupply() { return Pearls.isPickingUpSupply(); }
    @Override public boolean isHoldingSupplies() { return PlayerUtils.isHoldingSupplies(); }
    @Override public boolean isHoldingItem(Item item) { return PlayerUtils.isHoldingItem(item); }
    @Override public boolean isSneaking() { return PlayerUtils.isSneaking; }
    @Override public void rightClick() { PlayerUtils.rightClick(); }
    @Override public void leftClick() { PlayerUtils.leftClick(); }
    @Override public void shiftClick() { PlayerUtils.shiftClick(); }
    @Override public void sendMessageToPlayer(String message) { ChatUtils.sendMessageToPlayer(message); }
    @Override public void sendMessageToPlayer(ChatComponentText message) { ChatUtils.sendMessageToPlayer(message); }
    @Override public void sendCommand(String command) { ChatUtils.sendCommand(command); }
    @Override public EntityMagmaCube getBoss() { return KuudraUtils.getKuudra(); }
    @Override public MovableOverlay getOverlay(String name) { return OverlayManager.getOverlay(name); }
    @Override public void addOverlay(String addonId, MovableOverlay overlay) { OverlayManager.addOverlay(addonId, overlay); }
    @Override public void removeOverlay(String name) { OverlayManager.removeOverlay(name); }
    @Override public void lockMouse() { LockMouseLook.lockMouse(); }
    @Override public void unlockMouse() { LockMouseLook.unlockMouse(); }
    @Override public int kuudraTier() { return LocationUtils.kuudraTier(); }
    @Override public KuudraPhase phase() { return KuudraPhaseTracker.phase(); }
    @Override public int phaseOrdinal() { return KuudraPhaseTracker.phaseOrdinal(); }
    @Override public void swapToIndex(int index) { PlayerUtils.swapToIndex(index); }
    @Override public void setCurrentSlot(int slot) { PlayerUtils.setCurrentSlot(slot); }
    @Override public int getHotbarSlotIndex(Item item) { return PlayerUtils.getHotbarSlotIndex(item); }
    @Override public int getHotbarSlotIndex(String skyblockId) { return PlayerUtils.getHotbarSlotIndex(skyblockId); }
    @Override public int getCurrentHotbarSlot() { return PlayerUtils.getCurrentHotbarSlot(); }
    @Override public void schedule(Runnable runnable, long delay, TimeUnit timeUnit) { Multithreading.schedule(runnable, delay, timeUnit); }
    @Override public void drawBox(AxisAlignedBB axis, Color color, boolean filled, boolean esp, float partialTicks) { RenderUtils.drawBox(axis, color, filled, esp, partialTicks); }
    @Override public void drawPixelBox(Vec3 vec, Color color, double size, boolean filled, float partialTicks) { RenderUtils.drawPixelBox(vec, color, size, filled, partialTicks); }
    @Override public void renderNameTag(String string, double x, double y, double z, float scale, boolean background) { RenderUtils.renderNameTag(string, x, y, z, scale, background); }
    @Override public String removeFormatting(String text) { return StringUtils.removeFormatting(text); }
    @Override public long serverTime() { return ServerTickUtils.getServerTime(); }
    @Override public long serverTick() { return ServerTickUtils.getServerTick(); }
    @Override public Vec3 getClosestCrate() { return null; }
    @Override public Vec3 getPlayerEyePos() { return PlayerUtils.getPlayerEyePos(); }
    @Override public boolean hasAbility(ItemStack stack) { return ItemUtils.hasAbility(stack); }
    @Override public ItemStack getInventoryItemStack(Item item) { return PlayerUtils.getInventoryItemStack(item); }
    @Override public ItemStack getInventoryItemStack(String name) { return PlayerUtils.getInventoryItemStack(name); }
    @Override public void serverTickQueueTask(Runnable runnable) { ServerTickScheduler.queueTask(runnable); }
    @Override public void serverTickScheduleTask(Runnable fn, int delayTicks) { ServerTickScheduler.scheduleTask(fn, delayTicks); }
    @Override public String getItemUuid(ItemStack stack) { return ItemUtils.getItemUuid(stack); }
    @Override public String getItemId(ItemStack stack) { return ItemUtils.getItemId(stack); }
    @Override public String getItemLoreString(ItemStack stack) { return ItemUtils.getItemLoreString(stack); }
    @Override public List<String> getItemLore(ItemStack stack) { return ItemUtils.getItemLore(stack); }
    @Override public boolean isNullOrEmpty(String str) { return StringUtils.isNullOrEmpty(str); }
    @Override public void clickSlot(Slot slot, int type, int mode) { PlayerUtils.clickSlot(slot, type, mode); }
    @Override public void clickSlot(int slotNumber, int type, int mode) { PlayerUtils.clickSlot(slotNumber, type, mode); }
    @Override public void clickSlot(int expectedWindowId, int slotNumber, int type, int mode, boolean allowEmpty) { PlayerUtils.clickSlot(expectedWindowId, slotNumber, type, mode, allowEmpty); }
    @Override public void clickSlot(int expectedWindowId, Slot slot, int type, int mode, boolean allowEmpty) { PlayerUtils.clickSlot(expectedWindowId, slot, type, mode, allowEmpty); }
    @Override public void closeScreen() { PlayerUtils.closeScreen(); }
    @Override public void closeScreen(int expectedWindowId) { PlayerUtils.closeScreen(expectedWindowId); }
    @Override public String parseToShorthandNumber(double labelValue) { return StringUtils.parseToShorthandNumber(labelValue); }
    @Override public void copyToClipboard(String s) { IOUtils.copyStringToClipboard(s); }

    // Config
    @Override public boolean pearlCalculator() { return KICConfig.pearlCalculator; }
    @Override public boolean kuudraProfitCalculator() { return KICConfig.kuudraProfitCalculator; }

    // Kuudra Chest
    @Override public KuudraChest getCurrentChest() { return KuudraProfitCalculator.getCurrentChest(); }
    @Override public void handleChestBuy(boolean manual) { KuudraProfitCalculator.handleChestBuy(manual); }
    @Override public void rerollChestTrigger() { KuudraProfitCalculator.rerollChestTrigger(); }

    // Kuudra Cache
    @Override public BazaarItemValue getBazaar(String itemId) { return KuudraValueCache.getBazaar(itemId); }
    @Override public AuctionItemValue getAuction(String itemId) { return KuudraValueCache.getAuction(itemId); }
    @Override public BazaarItemValue kismet() { return KuudraValueCache.kismet(); }
    @Override public long computeTotal(KuudraChest chest, boolean ignoreTeeth) { return KuudraValueCache.computeTotal(chest, ignoreTeeth); }

    // Logger
    @Override public void logInfo(String text) { KICLogger.info(text); }
    @Override public void forceLogInfo(String text) { KICLogger.forceInfo(text); }
    @Override public void logWarn(String text) { KICLogger.warn(text); }
    @Override public void forceLogWarn(String text) { KICLogger.forceWarn(text); }
    @Override public void logError(String text) { KICLogger.error(text); }
    @Override public void forceLogError(String text) { KICLogger.forceError(text); }
}
