package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.misc.ChatMode;
import someoneok.kic.modules.misc.ChatHandler;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.ws.KICWS;

import java.util.Arrays;
import java.util.List;

import static someoneok.kic.utils.EmojiUtils.replaceEmojis;

public class ChatPlusCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "kcp";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("kchatplus", "kicchatplus");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kcp <message>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!ApiUtils.hasPremium()) {
            sender.addChatMessage(new ChatComponentText(
                    KIC.KICPrefix + " §cThis is a premium-only feature. §7Use §b/kic premium §7to learn more."
            ));
            return;
        }

        if (args.length == 0) return;

        String message = replaceEmojis(String.join(" ", args).trim());

        boolean isToggle = message.equals("toggle");

        if (isToggle) {
            KICConfig.kicPlusChat = !KICConfig.kicPlusChat;
            sendToggleMessage(sender);
        } else if (KICConfig.kicPlusChat) {
            KICWS.sendChatMessage(message, true);
        }
    }

    private void sendToggleMessage(ICommandSender sender) {
        String state = KICConfig.kicPlusChat ? "§a§lON" : "§c§lOFF";
        sender.addChatMessage(new ChatComponentText(KIC.KICPlusPrefix + " §aToggled KIC+ Chat " + state));
        if (!KICConfig.kicPlusChat && ChatHandler.currentChatMode == ChatMode.KICPLUSCHAT) {
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
