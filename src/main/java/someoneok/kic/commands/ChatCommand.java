package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.ws.KICWS;

import java.util.Arrays;
import java.util.List;

import static someoneok.kic.utils.EmojiUtils.replaceEmojis;

public class ChatCommand extends CommandBase {
    public static boolean plusChat = false;

    @Override
    public String getCommandName() {
        return "kc";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("kchat", "kicchat");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kc <message>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) return;

        String message = replaceEmojis(String.join(" ", args).trim());

        boolean hasPremium = ApiUtils.hasPremium();
        boolean isToggle = message.equals("toggle");
        boolean isPlusChat = message.equals("+");
        boolean isNormal = message.equals("normal");

        if (isToggle) {
            if (plusChat && hasPremium) {
                KICConfig.kicPlusChat = !KICConfig.kicPlusChat;
                boolean enabled = KICConfig.kicPlusChat;
                sendToggleMessage(sender, enabled, true);
            } else {
                KICConfig.kicChat = !KICConfig.kicChat;
                boolean enabled = KICConfig.kicChat;
                sendToggleMessage(sender, enabled, false);
            }
        } else if (isPlusChat && hasPremium) {
            boolean alreadyInPlus = plusChat;
            if (!alreadyInPlus) {
                plusChat = true;
            }
            sendSwitchMessage(sender, alreadyInPlus, true);
        } else if (isNormal && hasPremium) {
            boolean alreadyInNormal = !plusChat;
            if (!alreadyInNormal) {
                plusChat = false;
            }
            sendSwitchMessage(sender, alreadyInNormal, false);
        } else {
            KICWS.sendChatMessage(message, plusChat);
        }
    }

    private void sendToggleMessage(ICommandSender sender, boolean enabled, boolean isPlus) {
        String prefix = isPlus ? KIC.KICPlusPrefix : KIC.KICPrefix;
        String state = enabled ? "§a§lON" : "§c§lOFF";
        sender.addChatMessage(new ChatComponentText(prefix + " §aToggled " + (isPlus ? "KIC+ " : "KIC ") + "Chat " + state));
    }

    private void sendSwitchMessage(ICommandSender sender, boolean alreadyInChat, boolean toPlus) {
        String prefix = toPlus ? KIC.KICPlusPrefix : KIC.KICPrefix;
        String chatName = toPlus ? "KIC+ Chat" : "KIC Chat";
        String message = alreadyInChat
                ? " §aAlready in " + chatName + "."
                : " §aSwitched to " + chatName + ".";
        sender.addChatMessage(new ChatComponentText(prefix + message));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
