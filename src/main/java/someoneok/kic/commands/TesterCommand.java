package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.utils.dev.GuiLogAppender;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.dev.LogConsole;
import someoneok.kic.utils.dev.TesterStuff;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TesterCommand extends CommandBase {
    private final List<String> COMMANDS = Arrays.asList("console", "sendlogs", "debug");

    @Override public String getCommandName() { return "kictester"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/kictester"; }
    @Override public int getRequiredPermissionLevel() { return TesterStuff.testerMode ? 0 : 2; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!TesterStuff.testerMode) return;
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §eSubcommands: console, sendlogs"));
            return;
        }
        switch (args[0]) {
            case "console":
                LogConsole.showConsole();
                break;
            case "sendlogs":
                TesterStuff.sendLogs(GuiLogAppender.getCurrentLogs());
                break;
            case "test":
                KICLogger.info("Nothing here.");
                break;
            default:
                sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §eSubcommands: console, sendlogs"));
                break;
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, COMMANDS);
        return Collections.emptyList();
    }
}
