package someoneok.kic.commands;

import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import someoneok.kic.utils.dev.KICLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FuckOtherMods {
    private static boolean fuckedThem = false;

    private static final List<ICommand> COMMANDS = Arrays.asList(
            new KICCommand(),
            new TesterCommand(),
            new ChatCommand(),
            new KuudraCommand(),
            new KICAuctionCommand(),
            new AttributePriceCommand(),
            new AttributeUpgradeCommand(),
            new InternalCommand(),
            new LFCommand(),
            new StartKuudraCommand("t1", "KUUDRA_NORMAL"),
            new StartKuudraCommand("t2", "KUUDRA_HOT"),
            new StartKuudraCommand("t3", "KUUDRA_BURNING"),
            new StartKuudraCommand("t4", "KUUDRA_FIERY"),
            new StartKuudraCommand("t5", "KUUDRA_INFERNAL")
    );

    public static void registerCommands() {
        COMMANDS.forEach(ClientCommandHandler.instance::registerCommand);
    }

    public static void fuckThem() {
        if (fuckedThem) return;
        Map<String, ICommand> commandMap = ClientCommandHandler.instance.getCommands();

        for (ICommand command : COMMANDS) {
            List<String> allNames = new ArrayList<>();
            allNames.add(command.getCommandName());
            allNames.addAll(command.getCommandAliases());

            for (String name : allNames) {
                ICommand existingCommand = commandMap.get(name);

                if (existingCommand != null && existingCommand.getClass() != command.getClass()) {
                    KICLogger.info("[KIC] Another mod has modified /" + name + "! Overriding it...");
                    commandMap.remove(name);
                }

                ClientCommandHandler.instance.registerCommand(command);
            }
        }
        fuckedThem = true;
    }
}
