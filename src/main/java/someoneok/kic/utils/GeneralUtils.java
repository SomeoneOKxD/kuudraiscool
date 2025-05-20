package someoneok.kic.utils;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import someoneok.kic.KIC;
import someoneok.kic.utils.dev.KICLogger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.mc;

public class GeneralUtils {
    private static final Map<String, String> ATTRIBUTE_SHORTENINGS = new HashMap<>();
    public static final String[] colors = {"Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
            "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
            "Yellow", "White"};

    static {
        ATTRIBUTE_SHORTENINGS.put("mana_pool", "MP");
        ATTRIBUTE_SHORTENINGS.put("mana_regeneration", "MR");
        ATTRIBUTE_SHORTENINGS.put("veteran", "VET");
        ATTRIBUTE_SHORTENINGS.put("dominance", "DOM");
        ATTRIBUTE_SHORTENINGS.put("mending", "VIT");
        ATTRIBUTE_SHORTENINGS.put("vitality", "VIT");
        ATTRIBUTE_SHORTENINGS.put("magic_find", "MF");
        ATTRIBUTE_SHORTENINGS.put("speed", "SP");
        ATTRIBUTE_SHORTENINGS.put("breeze", "BR");
        ATTRIBUTE_SHORTENINGS.put("arachno", "ARA");
        ATTRIBUTE_SHORTENINGS.put("arachno_resistance", "AR");
        ATTRIBUTE_SHORTENINGS.put("attack_speed", "AS");
        ATTRIBUTE_SHORTENINGS.put("combo", "COM");
        ATTRIBUTE_SHORTENINGS.put("elite", "ELI");
        ATTRIBUTE_SHORTENINGS.put("ignition", "IGN");
        ATTRIBUTE_SHORTENINGS.put("life_recovery", "LRY");
        ATTRIBUTE_SHORTENINGS.put("midas_touch", "MT");
        ATTRIBUTE_SHORTENINGS.put("undead", "UND");
        ATTRIBUTE_SHORTENINGS.put("undead_resistance", "UR");
        ATTRIBUTE_SHORTENINGS.put("mana_steal", "MS");
        ATTRIBUTE_SHORTENINGS.put("ender", "END");
        ATTRIBUTE_SHORTENINGS.put("ender_resistance", "ER");
        ATTRIBUTE_SHORTENINGS.put("blazing", "BLA");
        ATTRIBUTE_SHORTENINGS.put("blazing_resistance", "BLR");
        ATTRIBUTE_SHORTENINGS.put("warrior", "WAR");
        ATTRIBUTE_SHORTENINGS.put("deadeye", "DEA");
        ATTRIBUTE_SHORTENINGS.put("experience", "EXP");
        ATTRIBUTE_SHORTENINGS.put("lifeline", "LL");
        ATTRIBUTE_SHORTENINGS.put("life_regeneration", "LR");
        ATTRIBUTE_SHORTENINGS.put("fortitude", "FOR");
        ATTRIBUTE_SHORTENINGS.put("blazing_fortune", "BF");
        ATTRIBUTE_SHORTENINGS.put("fishing_experience", "FE");
        ATTRIBUTE_SHORTENINGS.put("double_hook", "DH");
        ATTRIBUTE_SHORTENINGS.put("fisherman", "FM");
        ATTRIBUTE_SHORTENINGS.put("fishing_speed", "FS");
        ATTRIBUTE_SHORTENINGS.put("hunter", "HUN");
        ATTRIBUTE_SHORTENINGS.put("trophy_hunter", "TH");
        ATTRIBUTE_SHORTENINGS.put("infection", "INF");
    }

    public static IChatComponent createHoverComponent(boolean showHover, String message, String hoverText) {
        ChatComponentText mainComponent = new ChatComponentText(message);

        if (showHover) {
            ChatStyle hoverStyle = new ChatStyle();
            hoverStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hoverText)));
            mainComponent.setChatStyle(hoverStyle);
        }

        return mainComponent;
    }

    public static IChatComponent createClickComponent(boolean runClick, String message, ClickEvent.Action action, String command) {
        ChatComponentText mainComponent = new ChatComponentText(message);

        if (runClick) {
            ChatStyle clickStyle = new ChatStyle();
            clickStyle.setChatClickEvent(new ClickEvent(action, command));
            mainComponent.setChatStyle(clickStyle);
        }

        return mainComponent;
    }

    public static IChatComponent createHoverAndClickComponent(boolean showHoverAndRunClick, String message, String hoverText, String command) {
        ChatComponentText mainComponent = new ChatComponentText(message);

        if (showHoverAndRunClick) {
            ChatStyle chatStyle = new ChatStyle();
            chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hoverText)));
            chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            mainComponent.setChatStyle(chatStyle);
        }

        return mainComponent;
    }

    public static IChatComponent createHoverAndClickComponentSuggest(boolean hasItem, String message, String hoverText, String command) {
        ChatComponentText mainComponent = new ChatComponentText(message);

        if (hasItem) {
            ChatStyle chatStyle = new ChatStyle();
            chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hoverText)));
            chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
            mainComponent.setChatStyle(chatStyle);
        }

        return mainComponent;
    }

    public static void sendMessageToPlayer(String message) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }

    public static void sendMessageToPlayer(ChatComponentText message) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.addChatMessage(message);
    }

    public static void sendMessageToPlayer(IChatComponent message) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.addChatMessage(message);
    }

    public static void sendChatMessage(String message) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.sendChatMessage(message);
    }

    public static void sendCommand(String command) {
        if (mc.thePlayer == null) return;
        KICLogger.info("Executing command: " + command);
        mc.thePlayer.sendChatMessage(command);
    }

    public static void clickSlot(Slot slot, int type, int mode) {
        if (slot == null || slot.inventory == null || !slot.getHasStack()) return;
        if (KIC.mc.thePlayer == null || KIC.mc.thePlayer.openContainer == null || slot.inventory == mc.thePlayer.inventory) return;

        Container container = KIC.mc.thePlayer.openContainer;

        if (container.inventorySlots == null || container.inventorySlots.isEmpty()) return;
        if (slot.slotNumber < 0 || slot.slotNumber >= container.inventorySlots.size()) return;
        if (!container.inventorySlots.contains(slot)) return;
        if (container.inventorySlots.get(slot.slotNumber) != slot) return;

        KIC.mc.playerController.windowClick(container.windowId, slot.slotNumber, type, mode, KIC.mc.thePlayer);
    }

    public static void closeScreen(Runnable callback) {
        if (mc.currentScreen instanceof GuiContainer
                && mc.thePlayer != null
                && mc.thePlayer.openContainer != null
                && mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer) {
            mc.addScheduledTask(() -> {
                mc.thePlayer.closeScreen();
                if (callback != null) {
                    callback.run();
                }
            });
        }
    }

    public static String shortenAttribute(String attribute) {
        String lower = attribute.toLowerCase();
        if (ATTRIBUTE_SHORTENINGS.containsKey(lower)) {
            return ATTRIBUTE_SHORTENINGS.get(lower);
        }

        return Arrays.stream(attribute.replace("_", " ").toUpperCase().split(" "))
                .map(word -> String.valueOf(word.charAt(0)))
                .collect(Collectors.joining());
    }

    public static boolean vecEquals(Vec3 a, Vec3 b) {
        return a.distanceTo(b) < 0.05;
    }

    public static float round2(float value) {
        return Math.round(value * 100f) / 100f;
    }
}
