package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.dev.GuiLogAppender;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.dev.LogConsole;
import someoneok.kic.utils.dev.TesterStuff;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static someoneok.kic.utils.ApiUtils.*;

public class TesterCommand extends CommandBase {
    private final List<String> commands = Arrays.asList(
            "console", "sendlogs"
    );
    private static LogConsole console;

    @Override
    public String getCommandName() {
        return "kictester";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kictester";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!KICConfig.testerMode || !(isTester() || isDev() || isBeta())) return;
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §eSubcommands: console, sendlogs"));
            return;
        }
        switch (args[0]) {
            case "console":
                if (console == null) {
                    console = new LogConsole();
                } else {
                    console.show();
                }
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
    public int getRequiredPermissionLevel() {
        if (KICConfig.testerMode && (isTester() || isDev() || isBeta())) {
            return 0;
        }

        return 2;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, commands);
        }
        return Collections.emptyList();
    }
}
