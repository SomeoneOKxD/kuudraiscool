package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.addons.AddonHandle;
import someoneok.kic.addons.AddonHelpers;
import someoneok.kic.addons.AddonRegistry;
import someoneok.kic.api.ModAddon;
import someoneok.kic.utils.FileUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;

import static someoneok.kic.addons.AddonHelpers.ADDON_DIR;

public class KICAddonsCommand extends CommandBase {
    private static final List<String> COMMANDS = new ArrayList<>(Arrays.asList(
            "help", "list","reload","open","enable","disable","unload","delete"
    ));

    @Override public String getCommandName() { return "kicaddons"; }
    @Override public String getCommandUsage(ICommandSender sender) {
        return "/kicaddons <help|list|reload|open|enable|disable|unload|delete> [id]";
    }
    @Override public int getRequiredPermissionLevel() { return 0; }
    @Override public List<String> getCommandAliases() { return Arrays.asList("kaddons", "kicadd", "kadd"); }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            help(sender);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list":
                list(sender);
                return;
            case "reload":
            case "load":
                AddonHelpers.reloadAddons();
                return;
            case "open":
            case "files":
                try {
                    if (!ADDON_DIR.exists()) ADDON_DIR.mkdirs();
                    FileUtils.openFolder(ADDON_DIR);
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §aOpened addons folder."));
                } catch (Exception e) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cFailed to open addons folder! Please open it manually."));
                    KICLogger.error("[KIC] Failed to open addons directory: " + e.getMessage());
                }
                return;
        }

        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cMissing <id>. Try §7/kicaddons list§c."));
            return;
        }
        final String id = args[1];

        if (!AddonRegistry.exists(id)) {
            sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUnknown addon ID: §f" + id + " §7(use §o/kicaddons list§7 to see available addons)"));
            return;
        }

        switch (sub) {
            case "enable":
                if (AddonRegistry.isEnabled(id)) sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §eAddon §f" + id + " §eis already enabled."));
                else if (AddonRegistry.enable(id)) sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §aEnabled addon §f" + id));
                else sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cFailed to enable addon §f" + id));
                return;
            case "disable":
                if (!AddonRegistry.isEnabled(id)) sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §eAddon §f" + id + " §eis already disabled."));
                else if (AddonRegistry.disable(id)) sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §eDisabled addon §f" + id));
                else sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cFailed to disable addon §f" + id));
                return;
            case "unload":
                if (AddonHelpers.unloadAddon(id)) sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §6Unloaded addon §f" + id));
                else sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cFailed to unload addon §f" + id));
                return;
            case "delete":
                if (AddonHelpers.deleteAddon(id)) sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cDeleted addon §f" + id + " §7(and unloaded it)"));
                else sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cFailed to delete addon §f" + id));
                return;
        }

        help(sender);
    }

    private void list(ICommandSender sender) {
        List<AddonHandle> ok = new ArrayList<>(AddonRegistry.handles());
        ok.sort(Comparator.comparing(h -> h.addon.getId().toLowerCase(Locale.ROOT)));

        if (ok.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §7Loaded addons: §8none"));
        } else {
            sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §7Loaded addons (§f" + ok.size() + "§7):"));
            for (AddonHandle h : ok) {
                ModAddon a = h.addon;
                boolean on = h.isEnabled();
                String badge = on ? "" : " §7[§cDISABLED§7]";

                sender.addChatMessage(new ChatComponentText(
                        " §8- §f§l" + a.getId() + "§r§7@" + "§f" + a.getVersion()
                                + " §7(api §f" + a.getAddonApiVersion() + "§7)" + badge
                ));
            }
        }

        List<AddonRegistry.IncompatibleAddon> bad = new ArrayList<>(AddonRegistry.incompatibles());
        bad.sort(Comparator.comparing(ia -> ia.handle.addon.getId().toLowerCase(Locale.ROOT)));

        if (!bad.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(
                    "\n" + KIC.KICPrefix + " §7Incompatible addons (§c" + bad.size() + "§7):"
            ));
            for (AddonRegistry.IncompatibleAddon ia : bad) {
                ModAddon a = ia.handle.addon;
                String requires = a.getAddonApiVersion();
                String hostApi = ia.hostApi != null ? ia.hostApi : "unknown";
                String reason  = ia.reason != null ? ia.reason : "incompatible";

                sender.addChatMessage(new ChatComponentText(
                        " §8- §c§l" + a.getId() + "§r§7@" + "§c" + a.getVersion() +
                                " §7(api §c" + requires + "§7)" +
                                " §8— §c" + reason + " §7[host §f" + hostApi + "§7]"
                ));
            }
        }
    }

    private void help(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §a§lUsage:"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicaddons list"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicaddons reload"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicaddons open §7(open the addons folder)"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicaddons enable <id>"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicaddons disable <id>"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicaddons unload <id> §7(keeps JAR on disk)"));
        sender.addChatMessage(new ChatComponentText("§8* §a/kicaddons delete <id> §7(delete JAR from disk)"));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, COMMANDS);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if ("enable".equals(sub) || "disable".equals(sub) || "unload".equals(sub) || "delete".equals(sub)) {
                List<String> ids = new ArrayList<>(AddonRegistry.ids());
                Collections.sort(ids);
                return getListOfStringsMatchingLastWord(args, ids);
            }
        }
        return Collections.emptyList();
    }
}
