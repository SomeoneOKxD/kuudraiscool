package someoneok.kic.modules.kuudra;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class BlockUselessPerks {
    private static final Pattern ROMAN_SUFFIX = Pattern.compile("(?i)\\s+[IVX]+$");
    private static final Set<String> BLOCKED_BASE = new HashSet<>(Arrays.asList(
            "Steady Hands",
            "Bomberman",
            "Mining Frenzy",
            "Elle's Lava Rod",
            "Elle's Pickaxe",
            "Auto Revive"
    ));

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent e) {
        if (!shouldApply(e.getGui(), e.getContainer())) return;
        ItemStack stack = e.getSlot() != null ? e.getSlot().getStack() : null;
        if (isBlocked(stack)) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onSlotRender(GuiContainerEvent.DrawSlotEvent.Pre e) {
        if (!shouldApply(e.getGui(), e.getContainer())) return;
        ItemStack stack = e.getSlot() != null ? e.getSlot().getStack() : null;
        if (isBlocked(stack)) e.setCanceled(true);
    }

    private boolean shouldApply(GuiContainer gui, Container container) {
        if (!KICConfig.blockUselessPerks) return false;
        if (!LocationUtils.inKuudra()) return false;
        if (!ApiUtils.isVerified()) return false;
        if (!isPerkMenu(gui, container)) return false;
        return !GuiScreen.isCtrlKeyDown();
    }

    private boolean isBlocked(ItemStack stack) {
        if (stack == null) return false;
        String name = stack.getDisplayName();
        if (isNullOrEmpty(name)) return false;
        name = ROMAN_SUFFIX.matcher(removeFormatting(name)).replaceFirst("");
        return BLOCKED_BASE.contains(name);
    }

    private boolean isPerkMenu(GuiContainer gui, Container container) {
        if (!(gui instanceof GuiChest)) return false;
        if (!(container instanceof ContainerChest)) return false;

        ContainerChest chest = (ContainerChest) container;
        String title = chest.getLowerChestInventory().getDisplayName().getUnformattedText();
        return "Perk Menu".equals(title);
    }
}
