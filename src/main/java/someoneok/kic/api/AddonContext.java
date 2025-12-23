package someoneok.kic.api;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.Event;
import someoneok.kic.models.Island;
import someoneok.kic.models.crimson.AuctionItemValue;
import someoneok.kic.models.crimson.BazaarItemValue;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.chest.KuudraChest;
import someoneok.kic.utils.overlay.MovableOverlay;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public interface AddonContext {
    void registerEventHandler(Object handler);
    void unregisterEventHandler(Object handler);
    boolean post(Event e);

    void registerCommand(Object command);
    void unregisterCommand(Object command);

    File addonDir();
    File configFile();
    File dataFile();

    String kicPrefix();
    Minecraft mc();
    Random rng();
    boolean isVerifiedUser();
    boolean onSkyblock();
    String serverName();
    Island currentIsland();
    boolean inKuudra();
    boolean isPlayerKuudraDead();
    boolean isPlayerSneaking();
    boolean isPickingUpSupply();
    boolean isHoldingSupplies();
    boolean isHoldingItem(Item iem);
    boolean isSneaking();
    void rightClick();
    void leftClick();
    void shiftClick();
    void sendMessageToPlayer(String message);
    void sendMessageToPlayer(ChatComponentText message);
    void sendCommand(String command);
    EntityMagmaCube getBoss();
    MovableOverlay getOverlay(String name);
    void addOverlay(String addonId, MovableOverlay overlay);
    void removeOverlay(String name);
    void lockMouse();
    void unlockMouse();
    int kuudraTier();
    KuudraPhase phase();
    int phaseOrdinal();
    void swapToIndex(int index);
    void setCurrentSlot(int slot);
    int getHotbarSlotIndex(Item item);
    int getHotbarSlotIndex(String skyblockId);
    int getCurrentHotbarSlot();
    void schedule(Runnable runnable, long delay, TimeUnit timeUnit);
    void drawBox(AxisAlignedBB axis, Color color, boolean filled, boolean esp, float partialTicks);
    void drawPixelBox(Vec3 vec, Color color, double size, boolean filled, float partialTicks);
    void renderNameTag(String string, double x, double y, double z, float scale, boolean background);
    String removeFormatting(String text);
    long serverTime();
    long serverTick();
    Vec3 getClosestCrate();
    Vec3 getPlayerEyePos();
    boolean hasAbility(ItemStack stack);
    ItemStack getInventoryItemStack(Item item);
    ItemStack getInventoryItemStack(String name);
    void serverTickQueueTask(Runnable runnable);
    void serverTickScheduleTask(Runnable fn, int delayTicks);
    String getItemUuid(ItemStack stack);
    String getItemId(ItemStack stack);
    String getItemLoreString(ItemStack stack);
    List<String> getItemLore(ItemStack stack);
    boolean isNullOrEmpty(String str);
    void clickSlot(Slot slot, int type, int mode);
    void clickSlot(int slotNumber, int type, int mode);
    void clickSlot(int expectedWindowId, int slotNumber, int type, int mode, boolean allowEmpty);
    void clickSlot(int expectedWindowId, Slot slot, int type, int mode, boolean allowEmpty);
    void closeScreen();
    void closeScreen(int expectedWindowId);
    String parseToShorthandNumber(double labelValue);
    void copyToClipboard(String s);

    // Config
    boolean pearlCalculator();
    boolean kuudraProfitCalculator();

    // Kuudra Chest
    KuudraChest getCurrentChest();
    void handleChestBuy(boolean manual);
    void rerollChestTrigger();

    // Kuudra Cache
    BazaarItemValue getBazaar(String itemId);
    AuctionItemValue getAuction(String itemId);
    BazaarItemValue kismet();
    long computeTotal(KuudraChest chest, boolean ignoreTeeth);

    // Logger
    void logInfo(String text);
    void forceLogInfo(String text);
    void logWarn(String text);
    void forceLogWarn(String text);
    void logError(String text);
    void forceLogError(String text);
}
