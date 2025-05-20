package someoneok.kic.commands;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import someoneok.kic.modules.kuudra.KuudraUserInfo;

public class KuudraCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "kuudra";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kuudra <username>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Multithreading.runAsync(() -> KuudraUserInfo.showKuudraInfo(args.length == 0 ? null : args[0].trim(), true));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
