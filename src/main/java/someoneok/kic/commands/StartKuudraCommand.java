package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import static someoneok.kic.utils.GeneralUtils.sendCommand;

public class StartKuudraCommand extends CommandBase {
    private final String alias;
    private final String instanceType;

    public StartKuudraCommand(String alias, String instanceType) {
        this.alias = alias;
        this.instanceType = instanceType;
    }

    @Override
    public String getCommandName() {
        return alias;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + alias;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        sendCommand("/joininstance " + instanceType);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
