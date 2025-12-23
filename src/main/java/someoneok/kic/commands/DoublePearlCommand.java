package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import someoneok.kic.models.kuudra.PickupSpot;
import someoneok.kic.models.kuudra.pearls.DoublePearl;
import someoneok.kic.models.kuudra.pearls.DoublePearlDefaults;
import someoneok.kic.utils.kuudra.DoublePearlRegistry;

import java.util.*;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ChatUtils.createHoverAndClickComponent;

public class DoublePearlCommand extends CommandBase {
    private static final List<String> COMMANDS = new ArrayList<>(Arrays.asList(
            "help", "list", "add", "remove", "disable", "enable",
            "resetDefaults", "addDefaults", "reload", "save", "info"
    ));

    @Override
    public String getCommandName() { return "doublepearl"; }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("kicdoublepearl", "dp", "kicdp");
    }

    @Override
    public String getCommandUsage(ICommandSender s) {
        return "/doublepearl <help|list|add|remove|disable|enable|resetDefaults|addDefaults|reload|save|info>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) { sendDoublePearlHelpMsg(sender); return; }
        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "list":          doList(sender, args); break;
            case "add":           doAdd(sender, args); break;
            case "remove":        doRemove(sender, args); break;
            case "disable":       doDisable(sender, args); break;
            case "enable":        doEnable(sender, args); break;
            case "resetdefaults": DoublePearlRegistry.resetToDefaults(); send(sender, "§aDefaults restored."); break;
            case "adddefaults":   DoublePearlRegistry.addAllDefaults(); send(sender, "§aDefaults added (kept existing)."); break;
            case "reload":        DoublePearlRegistry.load(); send(sender, "§aDouble pearls reloaded."); break;
            case "save":          DoublePearlRegistry.save(); send(sender, "§aDouble pearls saved."); break;
            case "info":          doInfo(sender, args); break;
            default:              sendDoublePearlHelpMsg(sender);
        }
    }

    private void doList(ICommandSender s, String[] args) throws CommandException {
        if (args.length >= 2) {
            PickupSpot pre = parsePre(args[1]);
            List<DoublePearl> list = DoublePearlRegistry.getRoutesFrom(pre);

            if (list.isEmpty()) {
                send(s, "§7No routes found from §6" + pre.name() + "§7.");
                return;
            }

            send(s, String.format("§aRoutes from §6%s§a §7(§f%d§7)", pre.name(), list.size()));
            for (DoublePearl dp : list) {
                s.addChatMessage(new ChatComponentText(formatRouteLine(dp, false)));
            }
        } else {
            Collection<DoublePearl> all = DoublePearlRegistry.getAllActive();
            if (all.isEmpty()) {
                send(s, "§7No active double pearls found.");
                return;
            }

            send(s, String.format("§aActive DoublePearls §7(§f%d total§7)", all.size()));
            for (DoublePearl dp : all) {
                s.addChatMessage(new ChatComponentText(formatRouteLine(dp, true)));
            }
        }
    }

    private void doAdd(ICommandSender s, String[] args) throws CommandException {
        if (args.length < 4) throw new WrongUsageException("/doublepearl add <id> <pre> <drop> [x y z]");

        String id = args[1];
        PickupSpot pre = parsePre(args[2]);
        PickupSpot drop = parsePre(args[3]);

        final double x, y, z;
        if (args.length >= 7) {
            x = parseDouble(args[4], -300, 300);
            y = parseDouble(args[5], 0, 300);
            z = parseDouble(args[6], -300, 300);
        } else {
            if (!(s instanceof EntityPlayer)) {
                throw new CommandException("No coordinates specified and sender is not a player.");
            }
            EntityPlayer p = (EntityPlayer) s;
            x = p.posX; y = p.posY; z = p.posZ;
        }

        DoublePearlRegistry.upsertCustom(id, new Vec3(x, y, z), pre, drop);
        send(s, "§aAdded/updated route:");
        s.addChatMessage(new ChatComponentText(String.format("§8 - §3%s §7:: §b%s §7→ §e%s §7@ §8(§7%.1f§8, §7%.1f§8, §7%.1f§8) §8[§a+custom§8]",
                id, pre.name(), drop.name(), x, y, z)));
    }

    private void doRemove(ICommandSender s, String[] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException("/doublepearl remove <id>");
        String id = args[1];
        boolean ok = DoublePearlRegistry.removeCustom(id);
        send(s, ok ? "§aRemoved custom route §3" + id + "§a." : "§7No custom route with id §3" + id + "§7.");
    }

    private void doDisable(ICommandSender s, String[] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException("/doublepearl disable <defaultId>");
        boolean changed = DoublePearlRegistry.disableDefault(args[1]);
        send(s, changed ? "§aDisabled default §3" + args[1] + "§a." : "§7Unknown default id or already disabled.");
    }

    private void doEnable(ICommandSender s, String[] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException("/doublepearl enable <defaultId>");
        boolean changed = DoublePearlRegistry.enableDefault(args[1]);
        send(s, changed ? "§aEnabled default §3" + args[1] + "§a." : "§7Unknown default id or already enabled.");
    }

    private void doInfo(ICommandSender s, String[] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException("/doublepearl info <id>");
        String id = args[1];
        DoublePearl hit = null;
        for (DoublePearl dp : DoublePearlRegistry.getAllActive()) if (dp.getId().equals(id)) { hit = dp; break; }
        if (hit == null) { send(s, "§7Not active: §3" + id); return; }

        send(s, "§aRoute info:");
        Vec3 v = hit.getLocation();
        s.addChatMessage(new ChatComponentText(String.format("§8 - §3ID: §b%s", hit.getId())));
        s.addChatMessage(new ChatComponentText(String.format("§8 - §3Path: §b%s §7→ §e%s", hit.getPre().name(), hit.getDrop().name())));
        s.addChatMessage(new ChatComponentText(String.format("§8 - §3Loc: §8(§7%.2f§8, §7%.2f§8, §7%.2f§8)", v.xCoord, v.yCoord, v.zCoord)));
        s.addChatMessage(new ChatComponentText(String.format("§8 - §3Type: %s", hit.isDefault() ? "§7default" : "§a+custom")));
    }

    private void sendDoublePearlHelpMsg(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(String.format("\n%s §a§lDoublePearl Commands", KICPrefix)));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl list [PRE]",
                "§7List all active routes, or only those starting from a specific §oSupplyPickUpSpot§r\n\n§7Example: §e/doublepearl list SLASH",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/doublepearl list "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl add <id> <pre> <drop> [x y z]",
                "§7Add or update a custom double pearl route.\n§7If no coordinates are provided, your current player position is used.\n\n§7Example: §e/doublepearl add MY_SPOT SLASH SHOP",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/doublepearl add "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl remove <id>",
                "§7Remove a custom route by its ID.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/doublepearl remove "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl disable <defaultId>",
                "§7Temporarily disable a built-in default route.\n§7You can re-enable it anytime.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/doublepearl disable "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl enable <defaultId>",
                "§7Re-enable a previously disabled default route.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/doublepearl enable "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl resetDefaults",
                "§7Reset to only default routes.\n§7This clears all custom routes and re-enables all defaults.",
                ClickEvent.Action.RUN_COMMAND,
                "/doublepearl resetDefaults"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl addDefaults",
                "§7Add back any missing default routes.\n§7Keeps all your custom ones intact.",
                ClickEvent.Action.RUN_COMMAND,
                "/doublepearl addDefaults"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl reload",
                "§7Reload double_pearls.json from disk.",
                ClickEvent.Action.RUN_COMMAND,
                "/doublepearl reload"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl save",
                "§7Write current double pearl data to disk.",
                ClickEvent.Action.RUN_COMMAND,
                "/doublepearl save"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/doublepearl info <id>",
                "§7Show detailed information about a specific route by ID.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/doublepearl info "
        ));
    }

    private static String formatRouteLine(DoublePearl dp, boolean includeId) {
        Vec3 v = dp.getLocation();
        String tag = dp.isDefault() ? "§8[§7default§8]" : "§8[§a+custom§8]";
        if (includeId) {
            return String.format("§8 - §3%s §7:: §b%s §7→ §e%s §7@ §8(§7%.1f§8, §7%.1f§8, §7%.1f§8) %s",
                    dp.getId(), dp.getPre().name(), dp.getDrop().name(), v.xCoord, v.yCoord, v.zCoord, tag);
        } else {
            return String.format("§8 - §b%s §7→ §e%s §7@ §8(§7%.1f§8, §7%.1f§8, §7%.1f§8) %s",
                    dp.getPre().name(), dp.getDrop().name(), v.xCoord, v.yCoord, v.zCoord, tag);
        }
    }

    private static void send(ICommandSender s, String msg) {
        s.addChatMessage(new ChatComponentText(KICPrefix + " " + msg));
    }

    private static PickupSpot parsePre(String name) throws WrongUsageException {
        try {
            return PickupSpot.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new WrongUsageException("Unknown spot '" + name + "'. Use: " + Arrays.toString(PickupSpot.values()));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender s, String[] args, BlockPos pos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, COMMANDS);
        if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "list":
                    return getListOfStringsMatchingLastWord(args, enumNames());
                case "remove":
                    return getListOfStringsMatchingLastWord(args, customIds()); // only customs
                case "disable":
                case "enable":
                    return getListOfStringsMatchingLastWord(args, defaultIds()); // only defaults
                case "info":
                    return getListOfStringsMatchingLastWord(args, activeIds()); // any active
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("add"))
            return getListOfStringsMatchingLastWord(args, enumNames());
        if (args.length == 4 && args[0].equalsIgnoreCase("add"))
            return getListOfStringsMatchingLastWord(args, enumNames());
        return Collections.emptyList();
    }

    private static String[] enumNames() {
        PickupSpot[] v = PickupSpot.values();
        String[] out = new String[v.length];
        for (int i=0;i<v.length;i++) out[i] = v[i].name();
        return out;
    }

    private static List<String> defaultIds() {
        return new ArrayList<>(DoublePearlDefaults.DEFAULTS.keySet());
    }

    private List<String> activeIds() {
        List<String> out = new ArrayList<>();
        for (DoublePearl dp : DoublePearlRegistry.getAllActive()) out.add(dp.getId());
        return out;
    }

    private List<String> customIds() {
        Set<String> def = DoublePearlDefaults.DEFAULTS.keySet();
        List<String> out = new ArrayList<>();
        for (DoublePearl dp : DoublePearlRegistry.getAllActive()) if (!def.contains(dp.getId())) out.add(dp.getId());
        return out;
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }
}
