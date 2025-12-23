package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.utils.sound.SoundUtils;

import java.util.*;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ChatUtils.createHoverAndClickComponent;

public class KICSoundsCommand extends CommandBase {
    private static final List<String> COMMANDS = new ArrayList<>(Arrays.asList(
            "help", "search", "searchsounds", "play", "playsound"
    ));

    @Override public String getCommandName() { return "kicsounds"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/kicsounds <help|search|play> ..."; }
    @Override public int getRequiredPermissionLevel() { return 0; }
    @Override public List<String> getCommandAliases() { return Arrays.asList("ksounds", "ksound", "kicsound"); }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            help(sender);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if ("search".equals(sub) || "searchsounds".equals(sub)) {
            handleSearch(sender, args);
            return;
        }

        if ("play".equals(sub) || "playsound".equals(sub)) {
            handlePlay(sender, args);
            return;
        }

        help(sender);
    }

    private void handleSearch(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(
                    KICPrefix + " §cUsage: §f/kicsounds search <query>"
            ));
            sender.addChatMessage(new ChatComponentText(
                    KICPrefix + " §7You must provide a non-empty search query."
            ));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) sb.append(' ');
            sb.append(args[i]);
        }
        String query = sb.toString().trim();

        if (query.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(
                    KIC.KICPrefix + " §cSearch query cannot be empty."
            ));
            return;
        }

        List<String> matches = SoundUtils.searchSounds(query);
        if (matches.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(
                    KIC.KICPrefix + " §eNo sounds found matching §f\"" + query + "§f\"§e."
            ));
            return;
        }

        int total = matches.size();
        int maxToShow = 25;
        sender.addChatMessage(new ChatComponentText(
                KIC.KICPrefix + " §aFound §f" + total + "§a sound(s) matching §f\"" + query + "§f\"§a:"
        ));

        int shown = 0;
        for (String s : matches) {
            if (shown >= maxToShow) break;
            sender.addChatMessage(createHoverAndClickComponent(
                    true,
                    " §8- §f" + s,
                    "§aClick to play §f" + s,
                    ClickEvent.Action.RUN_COMMAND,
                    "/kicsounds play " + s)
            );
            shown++;
        }

        if (total > maxToShow) {
            sender.addChatMessage(new ChatComponentText(
                    KIC.KICPrefix + " §7Showing first §f" + maxToShow + "§7 of §f" + total + "§7 results."
            ));
        }
    }

    private void handlePlay(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(
                    KIC.KICPrefix + " §cUsage: §f/kicsounds play <soundName> [volume] [pitch]"
            ));
            return;
        }

        String soundName = args[1];

        float volume = 1.0F;
        float pitch = 1.0F;

        if (args.length >= 3) {
            try {
                volume = Float.parseFloat(args[2]);
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(
                        KIC.KICPrefix + " §cInvalid volume: §f" + args[2] + " §7(using default §f1.0§7)"
                ));
            }
        }

        if (args.length >= 4) {
            try {
                pitch = Float.parseFloat(args[3]);
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(
                        KIC.KICPrefix + " §cInvalid pitch: §f" + args[3] + " §7(using default §f1.0§7)"
                ));
            }
        }

        if (!SoundUtils.validSound(soundName)) {
            sender.addChatMessage(new ChatComponentText(
                    KIC.KICPrefix + " §cUnknown or invalid sound: §f" + soundName
            ));

            List<String> suggestions = SoundUtils.searchSounds(soundName);
            if (!suggestions.isEmpty()) {
                int maxSuggestions = 5;
                sender.addChatMessage(new ChatComponentText(
                        KIC.KICPrefix + " §7Did you mean:"
                ));
                int count = 0;
                for (String s : suggestions) {
                    if (count++ >= maxSuggestions) break;
                    sender.addChatMessage(new ChatComponentText(" §8- §f" + s));
                }
            }
            return;
        }

        SoundUtils.playSound(soundName, volume, pitch, false);

        sender.addChatMessage(new ChatComponentText(
                KIC.KICPrefix + " §aPlaying sound §f" + soundName +
                        " §7[vol §f" + volume + "§7, pitch §f" + pitch + "§7]"
        ));
    }

    private void help(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §a§lSounds command usage:"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicsounds search <query>"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicsounds play <soundName> §7[volume] [pitch]"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicsounds help"));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, COMMANDS);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);

            if ("play".equals(sub) || "playsound".equals(sub)) {
                try {
                    List<String> sounds = SoundUtils.getAllSounds();
                    return getListOfStringsMatchingLastWord(args, sounds);
                } catch (Exception ignored) {}
            }
        }

        return Collections.emptyList();
    }
}
