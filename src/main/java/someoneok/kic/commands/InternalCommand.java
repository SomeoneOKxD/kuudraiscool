package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class InternalCommand extends CommandBase {
    private static Consumer<Integer> action = null;

    @Override
    public String getCommandName() {
        return "intkiccmd";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args == null || args.length != 1 || action == null || isNullOrEmpty(args[0])) return;
        String arg = args[0];
        if (!isNumeric(arg)) return;
        Consumer<Integer> tempAction = action;
        action = null;
        tempAction.accept(Integer.parseInt(arg));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    public static void setAction(Consumer<Integer> newAction) {
        action = newAction;
    }
}