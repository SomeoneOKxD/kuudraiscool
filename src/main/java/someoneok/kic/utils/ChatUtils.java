package someoneok.kic.utils;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import someoneok.kic.utils.dev.KICLogger;

import static someoneok.kic.KIC.mc;

public class ChatUtils {
    public static void sendMessageToPlayer(String message) {
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }

    public static void sendMessageToPlayer(IChatComponent message) {
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(message);
    }

    public static void sendChatMessage(String message) {
        if (mc.thePlayer != null) mc.thePlayer.sendChatMessage(message);
    }

    public static void sendCommand(String command) {
        if (mc.thePlayer == null) return;
        if (!command.startsWith("/")) command = "/" + command;
        KICLogger.info("Executing command: " + command);
        int result = ClientCommandHandler.instance.executeCommand(mc.thePlayer, command);
        if (result == 0) mc.thePlayer.sendChatMessage(command);
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

    public static IChatComponent createHoverAndClickComponent(boolean shouldTrigger, String message, String hoverText, ClickEvent.Action action, String command) {
        ChatComponentText mainComponent = new ChatComponentText(message);

        if (shouldTrigger) {
            ChatStyle chatStyle = new ChatStyle();
            chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hoverText)));
            chatStyle.setChatClickEvent(new ClickEvent(action, command));
            mainComponent.setChatStyle(chatStyle);
        }

        return mainComponent;
    }
}
