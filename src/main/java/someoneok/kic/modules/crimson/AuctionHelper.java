package someoneok.kic.modules.crimson;

import cc.polyfrost.oneconfig.utils.IOUtils;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.mixin.AccessorGuiEditSign;
import someoneok.kic.models.crimson.AttributeItem;
import someoneok.kic.models.crimson.AttributeItemValue;
import someoneok.kic.models.crimson.Attributes;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.CacheManager;
import someoneok.kic.utils.ItemUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.InteractiveOverlay;
import someoneok.kic.utils.overlay.OverlayManager;
import someoneok.kic.utils.overlay.OverlaySegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static someoneok.kic.utils.StringUtils.formatPrice;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;

public class AuctionHelper {
    private TileEntitySign sign;
    private AttributeItem currentAuctionItem;
    private boolean shouldRender;
    private boolean wasOverlayCleared;
    private boolean stateChanged;

    @SubscribeEvent
    public void onGUIDrawnEvent(GuiContainerEvent.ForegroundDrawnEvent event) {
        if (shouldIgnore(event)) {
            clearStateIfChanged();
            return;
        }

        ItemStack auctionItem = ((ContainerChest) event.getContainer()).getLowerChestInventory().getStackInSlot(13);
        AttributeItem mappedItem = ItemUtils.mapAhItemToAttributeItem(auctionItem);

        if (mappedItem == null || !mappedItem.hasAttributes()) {
            clearStateIfChanged();
            return;
        }

        if (currentAuctionItem != null && mappedItem.getUuid().equals(currentAuctionItem.getUuid())) {
            return;
        }

        currentAuctionItem = mappedItem;
        stateChanged = true;
        shouldRender = true;
        sign = null;

        processItem();
    }

    private boolean shouldIgnore(GuiContainerEvent.ForegroundDrawnEvent event) {
        if (!ApiUtils.isVerified() || !KICConfig.crimsonAuctionHelper || !(event.getContainer() instanceof ContainerChest)) return true;

        String name = event.getChestName();
        return !("Create BIN Auction".equals(name) || "Create Auction".equals(name));
    }

    private void clearStateIfChanged() {
        if (!stateChanged) return;

        if (!wasOverlayCleared) {
            OverlayManager.getOverlay("AuctionHelper").updateText("");
            wasOverlayCleared = true;
        }

        sign = null;
        currentAuctionItem = null;
        shouldRender = false;
        stateChanged = false;
    }

    private void processItem() {
        KICLogger.info("Processing item");

        boolean needsUpdate = CacheManager.addItemAuctionHelper(new AttributeItemValue(currentAuctionItem));
        if (needsUpdate) {
            CacheManager.updateItemsCache(this::updateOverlay);
        } else {
            updateOverlay();
        }
    }

    private void updateOverlay() {
        KICLogger.info("Updating overlay");

        AttributeItemValue value = CacheManager.getAttributeItem(currentAuctionItem.getUuid());
        if (value == null || value.getAttributes() == null) return;

        Attributes attr = value.getAttributes();
        InteractiveOverlay overlay = (InteractiveOverlay) OverlayManager.getOverlay("AuctionHelper");

        overlay.setRenderCondition(() -> shouldRender);

        List<OverlaySegment> segments = new ArrayList<>();
        segments.add(new OverlaySegment("§7-= " + value.getName() + " §7=-"));

        if (attr.hasAttribute1()) addAttributeSegments(segments, attr.getFormattedName1(), attr.getLevel1(), attr.getLbPrice1(), attr.getAvgPrice1());
        if (attr.hasAttribute2()) addAttributeSegments(segments, attr.getFormattedName2(), attr.getLevel2(), attr.getLbPrice2(), attr.getAvgPrice2());
        if (attr.isGodroll()) addAttributeSegments(segments, attr.getFormattedName1() + " & " + attr.getFormattedName2(), -1, attr.getGodrollLbPrice(), attr.getGodrollAvgPrice());

        overlay.setSegments(segments);
        wasOverlayCleared = false;
    }

    private void addAttributeSegments(List<OverlaySegment> segments, String name, int level, long lb, long avg) {
        long lbPrice = Math.max(1, lb - KICConfig.ahHelperUnderCut);
        String levelText = level > 0 ? " " + level : "";

        segments.add(new OverlaySegment(String.format("\n§7- §b%s%s", name, levelText)));
        segments.add(new OverlaySegment(String.format("\n §7- §eLB -%s: §6%s", parseToShorthandNumber(KICConfig.ahHelperUnderCut), formatPrice(lbPrice)), () -> setSign(lbPrice), true));
        segments.add(new OverlaySegment(String.format("\n §7- §eAvg: §6%s", formatPrice(avg)), () -> setSign(avg), true));
    }

    private void setSign(long price) {
        String priceStr = String.valueOf(price);
        IOUtils.copyStringToClipboard(priceStr);
        if (sign != null) {
            sign.signText[0] = new ChatComponentText(priceStr);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!ApiUtils.isVerified() || !KICConfig.crimsonAuctionHelper || !(event.gui instanceof GuiEditSign)) return;

        TileEntitySign potentialSign = ((AccessorGuiEditSign) event.gui).getTileSign();

        if (isAuctionSign(potentialSign)) {
            sign = potentialSign;
        } else {
            sign = null;
        }
    }

    private boolean isAuctionSign(TileEntitySign s) {
        if (s == null || s.getPos().getY() != 0) return false;
        return Objects.equals(s.signText[1].getUnformattedText(), "^^^^^^^^^^^^^^^") &&
                Objects.equals(s.signText[2].getUnformattedText(), "Your auction") &&
                Objects.equals(s.signText[3].getUnformattedText(), "starting bid");
    }
}
