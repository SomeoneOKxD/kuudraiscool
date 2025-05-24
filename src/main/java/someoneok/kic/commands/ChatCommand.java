package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.misc.ChatMode;
import someoneok.kic.modules.misc.ChatHandler;
import someoneok.kic.utils.ws.KICWS;

import java.util.Arrays;
import java.util.List;

import static someoneok.kic.utils.EmojiUtils.replaceEmojis;

public class ChatCommand extends CommandBase {
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

        boolean isToggle = message.equals("toggle");

        if (isToggle) {
            KICConfig.kicChat = !KICConfig.kicChat;
            sendToggleMessage(sender);
        } else if (KICConfig.kicChat) {
            KICWS.sendChatMessage(message, false);
        }
    }

    private void sendToggleMessage(ICommandSender sender) {
        String state = KICConfig.kicChat ? "§a§lON" : "§c§lOFF";
        sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §aToggled KIC Chat " + state));
        if (!KICConfig.kicChat && ChatHandler.currentChatMode == ChatMode.KICCHAT) {
            ChatHandler.currentChatMode = ChatMode.MC;
            sender.addChatMessage(new ChatComponentText(
                    KIC.KICPrefix + " §aSwitched to §eMinecraft Chat§a mode."
            ));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
