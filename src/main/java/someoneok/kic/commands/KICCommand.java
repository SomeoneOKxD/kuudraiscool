package someoneok.kic.commands;

import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.modules.admin.websocket.KICAdminUI;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;
import someoneok.kic.modules.kuudra.KuudraUserInfo;
import someoneok.kic.modules.misc.EasyUpdater;
import someoneok.kic.modules.premium.Misc;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.overlay.EditHudScreen;
import someoneok.kic.utils.overlay.OverlayDataHandler;

import java.util.*;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.hasPremium;
import static someoneok.kic.utils.GeneralUtils.createHoverAndClickComponentSuggest;
import static someoneok.kic.utils.GeneralUtils.sendCommand;
import static someoneok.kic.utils.StringUtils.formatElapsedTimeMs;
import static someoneok.kic.utils.StringUtils.isValidUUIDv4RegexBased;

public class KICCommand extends CommandBase {
    private static final List<String> commands = new ArrayList<>(Arrays.asList(
            "settings", "kuudra", "apikey", "key", "verifykey", "verifyapikey",
            "resetprofittracker", "rpt", "resetpt",
            "t1", "t2", "t3", "t4", "t5", "help",
            "skpt", "sharekpt", "sharetracker", "edithuds", "kuudrapb", "resetkuudrapb"
    ));

    @Override
    public String getCommandName() {
        return "kic";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kic";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("kuudraiscool");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            KIC.config.openGui();
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "settings":
                KIC.config.openGui();
                break;

            case "kuudra":
                Multithreading.runAsync(() -> KuudraUserInfo.showKuudraInfo((args.length == 2 ? args[1].trim() : null), true));
                break;

            case "apikey":
            case "key":
                String key;
                if (args.length == 2) {
                    key = args[1].trim();
                } else {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic apikey <key>"));
                    return;
                }

                if (!isValidUUIDv4RegexBased(key)) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cInvalid API key format."));
                    return;
                }

                if (ApiUtils.isVerified() && Objects.equals(key, KICConfig.apiKey)) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §eYou're already using this API key."));
                    return;
                }

                KICConfig.apiKey = key;
                Multithreading.runAsync(() -> ApiUtils.verifyApiKey(true));
                break;

            case "verifykey":
            case "verifyapikey":
                Multithreading.runAsync(() -> ApiUtils.verifyApiKey(true));
                break;

            case "resetprofittracker":
            case "rpt":
            case "resetpt":
                KuudraProfitTracker.resetTracker();
                break;

            case "skpt":
            case "sharekpt":
            case "sharetracker":
                KuudraProfitTracker.shareTracker();
                break;

            case "admin":
                KICAdminUI.open();
                break;

            case "t1":
                sendCommand("/joininstance KUUDRA_NORMAL");
                break;

            case "t2":
                sendCommand("/joininstance KUUDRA_HOT");
                break;

            case "t3":
                sendCommand("/joininstance KUUDRA_BURNING");
                break;

            case "t4":
                sendCommand("/joininstance KUUDRA_FIERY");
                break;

            case "t5":
                sendCommand("/joininstance KUUDRA_INFERNAL");
                break;

            case "checkparty":
                Misc.checkParty();
                break;

            case "lastparty":
                String player;
                if (args.length == 2) {
                    player = args[1].trim();
                } else {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic lastparty <player>"));
                    return;
                }
                Misc.getLastParty(player);
                break;

            case "status":
                String player2;
                if (args.length == 2) {
                    player2 = args[1].trim();
                } else {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic status <player>"));
                    return;
                }
                Misc.getStatus(player2);
                break;

            case "update":
                Multithreading.runAsync(EasyUpdater::downloadAndExtractUpdate);
                break;

            case "edithuds":
                GuiUtils.displayScreen(new EditHudScreen());
                break;

            case "kuudrapb":
                sendKuudraPb(sender);
                break;

            case "resetkuudrapb":
                KIC.userData.setKuudraPersonalBest(0L);
                OverlayDataHandler.saveOverlaysData();
                break;

            case "help":
            default:
                sendKICHelpMsg(sender);
                break;
        }
    }

    private void sendKICHelpMsg(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(String.format("\n%s §a§lCommands", KIC.KICPrefix)));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic",
                "§7Opens the settings menu\n\n§7Alias: /kic settings",
                "/kic"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic help",
                "§7Shows this menu",
                "/kic help"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic apikey <key>",
                "§7Change/set your api key\n§7§o(Use your KIC api key, not Hypixel)\n\n§7Alias: /kic key",
                "/kic apikey "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic edithuds",
                "§7Edit all enabled huds.",
                "/kic edithuds"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic kuudra [player]",
                "§7Check Kuudra info for a player\n\n§7Example: /kuudra xaned\n§7Alias: /kuudra",
                "/kuudra "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/ap <attribute> [level] [attribute] [level]",
                "§7Checks the auction house for current attribute prices\n\n§7Example: /ap ll mp\n§7Aliases: /attributeprice, /kicap",
                "/ap "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/au <item> <attribute> <start level> <end level>",
                "§7Shows you the cheapest possible path to upgrade an attribute.\n\n§7Example: /ap ll mp\n§7Aliases: /attributeupgrade, /kicau",
                "/au "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/ka <attribute> [level] [attribute] [level]",
                "§7Opens a custom auction house GUI with current attribute prices\n\n§7Example: /ka ll mp\n§7Aliases: /kicaction, /kicah",
                "/ka "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic verifykey",
                "§7Verifies the status of your current api key\n\n§7Alias: /kic verifyapikey",
                "/kic verifykey"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic resetprofittracker",
                "§7Reset Kuudra profit tracker data\n\n§7Aliases: /kic rpt, /kic resetpt",
                "/kic resetprofittracker"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic sharetracker",
                "§7Share your profit tracker in the KIC discord\n\n§7Aliases: /kic skpt, /kic sharekpt",
                "/kic sharetracker"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/lf <player> <item>",
                "§7Searches a player for a specific item\n\n§7Example 1: /lf " + sender.getName() + " Hyperion\n§7Example 2: /lf " + sender.getName() + " lore: Ability: Wither Impact\n§7Aliases: /lookingfor, /lookingforitem, /lfi",
                "/lf "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kc <message>",
                "§7Sends a message in the KIC Chat/IRC/bridge\n\n§7Aliases: /kchat, /kicchat",
                "/kc "
        ));

        if (hasPremium()) {
            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§8* §a/kic checkparty §7(KIC+)",
                    "§7Checks all members in your party for KIC users",
                    "/kic checkparty"
            ));

            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§8* §a/kic lastparty <player> §7(KIC+)",
                    "§7Tells you the last dungeon party the player was in",
                    "/kic lastparty "
            ));

            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§8* §a/kic status <player> §7(KIC+)",
                    "§7Tells you if the player is currently online or not",
                    "/kic status "
            ));
        }

        for (int i = 1; i <= 5; i++) {
            String tier = "/t" + i;
            String instance = "§7Join T" + i + " Kuudra Instance\n\n§7Alias: /kic t" + i;
            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§8* §a" + tier,
                    instance,
                    tier
            ));
        }

        sender.addChatMessage(new ChatComponentText("\n§2[Hover] §7shows info, §2[Click] §7suggests command"));
    }

    private void sendKuudraPb(ICommandSender sender) {
        long pb = KIC.userData.getKuudraPersonalBest();

        String msg = pb == 0
                ? KICPrefix + " §cNo Kuudra Personal Best yet!"
                : String.format("%s §aKuudra Personal Best: §f%s", KICPrefix, formatElapsedTimeMs(pb));

        sender.addChatMessage(new ChatComponentText(msg));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, commands);
        }
        return Collections.emptyList();
    }

    public static void addPremiumCommands() {
        commands.add("checkparty");
        commands.add("lastparty");
        commands.add("status");
    }

    public static void removePremiumCommands() {
        commands.remove("checkparty");
        commands.remove("lastparty");
        commands.remove("status");
    }
}
