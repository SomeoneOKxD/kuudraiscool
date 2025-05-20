package someoneok.kic.modules.misc;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;

import static someoneok.kic.KIC.mc;

public class ArmorHud {
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!KICConfig.showArmorInHud || event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        ScaledResolution res = new ScaledResolution(mc);
        EntityPlayer player = mc.thePlayer;

        renderArmorHUD(player, res);
    }

    private void renderArmorHUD(EntityPlayer player, ScaledResolution res) {
        RenderItem itemRenderer = mc.getRenderItem();
        int screenWidth = res.getScaledWidth();
        int screenHeight = res.getScaledHeight();

        int iconSize = 16;
        int verticalSpacing = 14;
        int horizontalPadding = 8;

        int hotbarY = screenHeight - iconSize;
        int hotbarCenterX = screenWidth / 2;
        int hotbarHalfWidth = 91;

        int leftX = hotbarCenterX - hotbarHalfWidth - iconSize - horizontalPadding;
        renderArmorItem(itemRenderer, player.getCurrentArmor(3), leftX, hotbarY - verticalSpacing);
        renderArmorItem(itemRenderer, player.getCurrentArmor(2), leftX, hotbarY);

        int rightX = hotbarCenterX + hotbarHalfWidth + horizontalPadding;
        renderArmorItem(itemRenderer, player.getCurrentArmor(1), rightX, hotbarY - verticalSpacing);
        renderArmorItem(itemRenderer, player.getCurrentArmor(0), rightX, hotbarY);
    }

    private static void renderArmorItem(RenderItem renderer, ItemStack stack, int x, int y) {
        if (stack != null && stack.getItem() != null) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            renderer.renderItemAndEffectIntoGUI(stack, x, y);
            renderer.renderItemOverlayIntoGUI(mc.fontRendererObj, stack, x, y, null);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }
}
