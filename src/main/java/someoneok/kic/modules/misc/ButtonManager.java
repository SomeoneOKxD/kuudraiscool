package someoneok.kic.modules.misc;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import someoneok.kic.config.KICConfig;
import someoneok.kic.modules.kuudra.KuudraPfGuiInfo;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static someoneok.kic.KIC.mc;

public class ButtonManager {
    private static final HashMap<String, GuiCheckBox> checkBoxes = new HashMap<>();
    private final ArrayList<String> activeBoxes = new ArrayList<>();

    private static final int CHECKBOX_SPACING = 20;
    private static final int INITIAL_Y_OFFSET = 25;
    private static final int X_OFFSET_LEFT = 5;
    private static final int X_OFFSET_RIGHT = 150;

    private static boolean alignRight = false;

    static {
        checkBoxes.put("containerValue", new GuiCheckBox(0, 0, 0, "Container Value", true));
        checkBoxes.put("partyFinderGuiStats", new GuiCheckBox(1, 0, 0, "Show PF Stats", true));
        checkBoxes.put("partyFinderGuiStatsExample", new GuiCheckBox(2, 0, 0, "Show PF Stats Example", false));
        checkBoxes.put("auctionHelper", new GuiCheckBox(3, 0, 0, "Auction Helper", true));
    }

    @SubscribeEvent
    public void guiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiChest)
                || !LocationUtils.onSkyblock
                || !ApiUtils.isVerified()) return;

        activeBoxes.clear();
        if (KICConfig.crimsonContainerHelper) activeBoxes.add("containerValue");
        if (KuudraPfGuiInfo.shouldRender) activeBoxes.add("partyFinderGuiStats");
        if (KuudraPfGuiInfo.shouldRender) activeBoxes.add("partyFinderGuiStatsExample");

        int y = event.gui.height - INITIAL_Y_OFFSET;

        for (Map.Entry<String, GuiCheckBox> entry : checkBoxes.entrySet()) {
            GuiCheckBox box = entry.getValue();
            boolean shouldDisplay = activeBoxes.contains(entry.getKey());

            box.setIsChecked(shouldDisplay && box.isChecked());

            if (shouldDisplay) {
                box.xPosition = alignRight ? (event.gui.width - X_OFFSET_RIGHT) : X_OFFSET_LEFT;
                box.yPosition = y;

                box.drawButton(event.gui.mc, 0, 0);
                y -= CHECKBOX_SPACING;
            }
        }
    }

    @SubscribeEvent
    public void onGuiClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Mouse.getEventButtonState()
                || !LocationUtils.onSkyblock
                || !ApiUtils.isVerified()) return;

        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        checkBoxes.values().forEach(box -> box.mousePressed(mc, mouseX, mouseY));
    }

    public static boolean isChecked(String key) {
        GuiCheckBox box = checkBoxes.get(key);
        return box != null && box.isChecked();
    }

    public static void updateCheckboxAlignment() {
        alignRight = KICConfig.invButtonLoc == 1;
    }
}
