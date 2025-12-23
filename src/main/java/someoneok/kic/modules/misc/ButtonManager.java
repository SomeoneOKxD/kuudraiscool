package someoneok.kic.modules.misc;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Contract;
import org.lwjgl.input.Mouse;
import someoneok.kic.config.KICConfig;
import someoneok.kic.modules.crimson.HuntingBoxValue;
import someoneok.kic.modules.kuudra.KuudraPfGuiInfo;
import someoneok.kic.modules.kuudra.Vesuvius;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static someoneok.kic.KIC.mc;

public class ButtonManager {
    private static final Map<String, GuiCheckBox> checkBoxes = new HashMap<>();
    private static final Map<String, BooleanSupplier> conditions = new HashMap<>();

    private static final int CHECKBOX_SPACING = 20;
    private static final int INITIAL_Y_OFFSET = 25;
    private static final int X_OFFSET_LEFT = 5;
    private static final int X_OFFSET_RIGHT = 150;

    private static boolean alignRight = false;

    static {
        register("huntingBoxValue", new GuiCheckBox(0, 0, 0, "Hunting Box Value", true), () -> HuntingBoxValue.shouldRender);
        register("partyFinderGuiStats", new GuiCheckBox(1, 0, 0, "Show PF Stats", true), () -> KuudraPfGuiInfo.shouldRender);
        register("partyFinderGuiStatsExample", new GuiCheckBox(2, 0, 0, "Show PF Stats Example", false), () -> KuudraPfGuiInfo.shouldRender);
        register("vesuviusHighlightUnopened", new GuiCheckBox(3, 0, 0, "Highlight unopened chests", true), () -> KICConfig.highlightUnopenedChests && Vesuvius.shouldRender);
    }

    @SubscribeEvent
    public void guiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiChest)
                || !LocationUtils.onSkyblock
                || !ApiUtils.isVerified()) return;

        GuiScreen gui = event.gui;

        int y = gui.height - INITIAL_Y_OFFSET;

        for (Map.Entry<String, GuiCheckBox> entry : checkBoxes.entrySet()) {
            String key = entry.getKey();
            GuiCheckBox box = entry.getValue();
            BooleanSupplier condition = conditions.get(key);

            if (condition != null && condition.getAsBoolean()) {
                box.xPosition = alignRight ? (gui.width - X_OFFSET_RIGHT) : X_OFFSET_LEFT;
                box.yPosition = y;

                box.drawButton(gui.mc, 0, 0);
                y -= CHECKBOX_SPACING;
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onGuiClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Mouse.getEventButtonState()
                || !LocationUtils.onSkyblock
                || !ApiUtils.isVerified()) return;

        GuiScreen gui = event.gui;

        int mouseX = Mouse.getEventX() * gui.width / mc.displayWidth;
        int mouseY = gui.height - Mouse.getEventY() * gui.height / mc.displayHeight - 1;

        checkBoxes.entrySet().stream()
                .filter(e -> {
                    BooleanSupplier cond = conditions.get(e.getKey());
                    return cond != null && cond.getAsBoolean();
                }).forEach(e -> {
                    GuiCheckBox box = e.getValue();
                    box.mousePressed(mc, mouseX, mouseY);
                });
    }

    private static void register(String key, GuiCheckBox box, BooleanSupplier condition) {
        checkBoxes.put(key, box);
        conditions.put(key, condition);
    }

    @Contract(pure = true)
    public static boolean isChecked(String key) {
        GuiCheckBox box = checkBoxes.get(key);
        BooleanSupplier condition = conditions.get(key);
        return box != null && condition != null && box.isChecked() && condition.getAsBoolean();
    }

    public static void updateCheckboxAlignment() {
        alignRight = KICConfig.invButtonLoc == 1;
    }
}
