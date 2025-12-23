package someoneok.kic.models;

import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import someoneok.kic.KIC;
import someoneok.kic.models.misc.PatcherScale;
import someoneok.kic.models.misc.TriConsumer;

import java.awt.Color;
import java.io.IOException;
import java.util.function.Supplier;

import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

@SideOnly(Side.CLIENT)
public class KICCustomGUI extends GuiScreen {
    private final int GUI_WIDTH = 176;
    private final int GUI_HEIGHT = 134;
    private final int SLOT_SIZE = 18;
    private static final int COLUMNS = 9;
    private static final int ROWS = 6;
    private final ResourceLocation GUI_TEXTURE_1 = new ResourceLocation("kic", "textures/gui/KICGui1.png");
    private final ResourceLocation GUI_TEXTURE_2 = new ResourceLocation("kic", "textures/gui/KICGui2.png");
    private final ResourceLocation GUI_TEXTURE_3 = new ResourceLocation("kic", "textures/gui/KICGui3.png");
    private final ResourceLocation[] textures = { GUI_TEXTURE_1, GUI_TEXTURE_2, GUI_TEXTURE_3 };
    private final RenderItem itemRenderer;
    private final InventoryBasic inventory;
    private final Supplier<Integer> configOption;
    private final TriConsumer<Integer, ItemStack, Integer> onSlotClicked;
    private final Runnable onGuiClose;
    private int previousGuiScale = -1;

    public KICCustomGUI(InventoryBasic inventory, Supplier<Integer> configOption, TriConsumer<Integer, ItemStack, Integer> onSlotClicked, Runnable onGuiClose) {
        this.inventory = inventory;
        this.itemRenderer = KIC.mc.getRenderItem();
        this.configOption = configOption;
        this.onSlotClicked = onSlotClicked;
        this.onGuiClose = onGuiClose;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        int patcherScale = PatcherScale.getInvScale();
        if (patcherScale > 0 && patcherScale <= 5) {
            previousGuiScale = mc.gameSettings.guiScale;
            mc.gameSettings.guiScale = patcherScale;

            ScaledResolution res = new ScaledResolution(mc);
            this.width = res.getScaledWidth();
            this.height = res.getScaledHeight();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;

        mc.getTextureManager().bindTexture(getGuiTexture());
        drawTexturedModalRect(x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        drawCenteredString(mc.fontRendererObj, inventory.getName(), width / 2, y + 6, new java.awt.Color(0, 0, 0, 0).getRGB());

        drawItems(x + 8, y + 18, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawItems(int startX, int startY, int mouseX, int mouseY) {
        RenderHelper.enableGUIStandardItemLighting();
        ItemStack hoveredStack = null;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            int slotX = startX + (i % COLUMNS) * SLOT_SIZE;
            int slotY = startY + (i / COLUMNS) * SLOT_SIZE;

            boolean mouseOver = mouseX >= slotX && mouseX <= slotX + 17 && mouseY >= slotY && mouseY <= slotY + 17;

            if (stack != null) {
                itemRenderer.renderItemIntoGUI(stack, slotX, slotY);
                itemRenderer.renderItemOverlayIntoGUI(mc.fontRendererObj, stack, slotX, slotY, null);

                if (mouseOver && !isNullOrEmpty(stack.getDisplayName())) {
                    GlStateManager.disableDepth();
                    drawRect(slotX, slotY, slotX + 16, slotY + 16, new Color(255, 255, 255, 140).getRGB());
                    GlStateManager.enableDepth();
                }
            }

            if (mouseOver && stack != null) {
                hoveredStack = stack;
            }
        }
        RenderHelper.disableStandardItemLighting();

        if (hoveredStack != null && !isNullOrEmpty(hoveredStack.getDisplayName())) {
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            this.renderToolTip(hoveredStack, mouseX, mouseY);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;

        int startX = x + 8;
        int startY = y + 18;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            int slotX = startX + (i % COLUMNS) * SLOT_SIZE;
            int slotY = startY + (i / COLUMNS) * SLOT_SIZE;

            if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                ItemStack clickedStack = inventory.getStackInSlot(i);

                if (clickedStack != null && !isNullOrEmpty(clickedStack.getDisplayName())) {
                    onSlotClicked.accept(i, clickedStack, mouseButton);
                }
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == mc.gameSettings.keyBindInventory.getKeyCode()) {
            GuiUtils.closeScreen();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (onGuiClose != null) onGuiClose.run();

        if (previousGuiScale != -1) {
            mc.gameSettings.guiScale = previousGuiScale;
        }
    }

    public static int getCOLUMNS() {
        return COLUMNS;
    }

    public static int getROWS() {
        return ROWS;
    }

    private ResourceLocation getGuiTexture() {
        return (configOption != null && configOption.get() >= 0 && configOption.get() < textures.length)
                ? textures[configOption.get()]
                : GUI_TEXTURE_1;
    }
}
