package someoneok.kic.utils;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

import java.lang.invoke.MethodHandle;
import java.util.*;

public class ForgeCommandUtils {
    private static final MethodHandle MH_GET_COMMAND_SET;

    static {
        MH_GET_COMMAND_SET = ReflectionUtil.getField(
                CommandHandler.class,
                "commandSet", "field_71561_b", "field_71563_b"
        );
    }

    @SuppressWarnings("unchecked")
    public static Set<ICommand> commandSet() {
        try {
            return (Set<ICommand>) MH_GET_COMMAND_SET.invoke(ClientCommandHandler.instance);
        } catch (Throwable t) {
            return Collections.emptySet();
        }
    }

    public static boolean removeCommand(ICommand target) {
        if (target == null) return false;

        Map<String, ICommand> map = ClientCommandHandler.instance.getCommands();
        boolean changed = false;

        Iterator<Map.Entry<String, ICommand>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ICommand> e = it.next();
            if (e.getValue() == target) {
                it.remove();
                changed = true;
            }
        }

        map.remove(target.getCommandName());
        List<String> aliases = target.getCommandAliases();
        if (aliases != null) for (String a : aliases) map.remove(a);

        changed |= commandSet().remove(target);

        return changed;
    }

    public static Optional<ICommand> removeByName(String nameOrAlias) {
        if (nameOrAlias == null || nameOrAlias.isEmpty()) return Optional.empty();
        Map<String, ICommand> map = ClientCommandHandler.instance.getCommands();
        ICommand existing = map.remove(nameOrAlias);
        if (existing != null) {
            map.remove(existing.getCommandName());
            List<String> aliases = existing.getCommandAliases();
            if (aliases != null) for (String a : aliases) map.remove(a);
            commandSet().remove(existing);
            return Optional.of(existing);
        }
        return Optional.empty();
    }

    public static void registerCommand(ICommand command) {
        ClientCommandHandler.instance.registerCommand(command);
    }

    public static void registerCommands(List<ICommand> commands) {
        commands.forEach(ForgeCommandUtils::registerCommand);
    }
}
