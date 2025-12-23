package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import someoneok.kic.KIC;
import someoneok.kic.models.request.LFRequest;
import someoneok.kic.modules.misc.LF;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;

public class LFCommand extends CommandBase {
    Pattern pattern = Pattern.compile("(\\S+)\\s+(?:(?i)lore:\\s*)?(.*)");

    @Override
    public String getCommandName() {
        return "lf";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("lookingfor", "lookingforitem", "lfi");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/lf <player> <query>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sendMessageToPlayer(KIC.KICPrefix + " §cInvalid search.\n §7- §a/lf <player> <query>\n §7- §a/lf <player> lore:<query>");
            return;
        }

        String message = String.join(" ", args).trim();
        String username = null;
        boolean lore = false;
        String search = null;

        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            username = matcher.group(1);
            lore = message.toLowerCase().contains("lore:");
            search = matcher.group(2).trim();
        }

        if (username == null) {
            sendMessageToPlayer(KIC.KICPrefix + " §cInvalid search.\n §7- §a/lf <player> <query>\n §7- §a/lf <player> lore:<query>");
            return;
        }
        LF.show(new LFRequest(username, search, lore));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
