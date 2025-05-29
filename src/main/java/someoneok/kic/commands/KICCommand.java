package someoneok.kic.commands;

import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.misc.ChatMode;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;
import someoneok.kic.modules.kuudra.KuudraUserInfo;
import someoneok.kic.modules.misc.ChatHandler;
import someoneok.kic.modules.misc.TrackEmptySlots;
import someoneok.kic.modules.premium.Misc;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.Updater;
import someoneok.kic.utils.overlay.EditHudScreen;
import someoneok.kic.utils.overlay.OverlayDataHandler;

import java.util.*;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.hasPremium;
import static someoneok.kic.utils.GeneralUtils.*;
import static someoneok.kic.utils.StringUtils.formatElapsedTimeMs;
import static someoneok.kic.utils.StringUtils.isValidUUIDv4RegexBased;

public class KICCommand extends CommandBase {
    private static final List<String> commands = new ArrayList<>(Arrays.asList(
            "help", "settings", "apikey", "key",
            "verifykey", "verifyapikey",
            "resetprofittracker", "rpt", "resetpt",
            "kuudra", "t1", "t2", "t3", "t4", "t5",
            "skpt", "sharekpt", "sharetracker", "edithuds",
            "pb", "resetpb", "resetkpb", "chat", "premium",
            "ecbp", "checkupdates"
    ));
    private static final List<String> chatModes = Arrays.asList("kc", "kcp", "mc");

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

            case "checkupdates":
                Updater.checkForUpdates(true);
                break;

            case "update":
                Updater.performUpdate();
                break;

            case "edithuds":
                GuiUtils.displayScreen(new EditHudScreen());
                break;

            case "pb":
                sendKuudraPb(sender);
                break;

            case "resetpb":
            case "resetkpb":
                KIC.userData.setKuudraPersonalBest(0L);
                OverlayDataHandler.saveOverlaysData();
                break;

            case "chat":
                if (args.length != 2) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic chat kc/kcp/mc"));
                    break;
                }

                String chatArg = args[1].trim().toLowerCase();

                switch (chatArg) {
                    case "kc":
                        ChatHandler.currentChatMode = ChatMode.KICCHAT;
                        sender.addChatMessage(new ChatComponentText(
                                KIC.KICPrefix + " §aSwitched to §bKIC Chat§a mode."
                        ));
                        break;

                    case "kcp":
                        if (ApiUtils.hasPremium()) {
                            ChatHandler.currentChatMode = ChatMode.KICPLUSCHAT;
                            sender.addChatMessage(new ChatComponentText(
                                    KIC.KICPlusPrefix + " §aSwitched to §dKIC+ Chat§a mode."
                            ));
                        } else {
                            sender.addChatMessage(new ChatComponentText(
                                    KIC.KICPrefix + " §cThis is a premium-only feature. §7Use §b/kic premium §7to learn more."
                            ));
                        }
                        break;

                    case "mc":
                        ChatHandler.currentChatMode = ChatMode.MC;
                        sender.addChatMessage(new ChatComponentText(
                                KIC.KICPrefix + " §aSwitched to §eMinecraft Chat§a mode."
                        ));
                        break;

                    default:
                        sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic chat kc/kcp/mc"));
                        break;
                }
                break;

            case "premium":
                ChatComponentText base = new ChatComponentText(KIC.KICPrefix + " §aPremium/KIC+ features are exclusive to Patreon supporters.\n");
                ChatComponentText link = new ChatComponentText("§9https://www.patreon.com/kuudraiscool");
                link.getChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/kuudraiscool"))
                        .setUnderlined(true);

                base.appendText("§7Join via §b/premium §7in the Discord or visit: ");
                base.appendSibling(link);

                sender.addChatMessage(base);
                break;

            case "ecbp":
                TrackEmptySlots.openEmptyEcOrBp();
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
                "§8* §a/kic verifykey",
                "§7Verifies the status of your current api key\n\n§7Alias: /kic verifyapikey",
                "/kic verifykey"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic edithuds",
                "§7Edit all enabled huds.",
                "/kic edithuds"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic chat kc/kcp/mc",
                "§7Switches the chat mode to KIC Chat/KIC+ Chat/Minecraft (Same as /chat party)",
                "/kic chat "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic kuudra [player]",
                "§7Check Kuudra info for a player\n\n§7Example: /kuudra xaned\n§7Alias: /kuudra",
                "/kuudra "
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
                "§8* §a/kic pb",
                "§7Show your current kuudra T5 personal best",
                "/kic pb"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic resetpb",
                "§7Resets your kuudra T5 personal best\n\n§7Alias: /kic resetkpb",
                "/kic resetpb"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic ecbp",
                "§7Opens an ender chest page or backpack with empty slots",
                "/kic ecbp"
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kic checkupdates",
                "§7Checks whether a new version of the mod is available.",
                "/kic checkupdates"
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
                "§8* §a/lf <player> <item>",
                "§7Searches a player for a specific item\n\n§7Example 1: /lf " + sender.getName() + " Hyperion\n§7Example 2: /lf " + sender.getName() + " lore: Ability: Wither Impact\n§7Aliases: /lookingfor, /lookingforitem, /lfi",
                "/lf "
        ));

        sender.addChatMessage(createHoverAndClickComponentSuggest(
                true,
                "§8* §a/kc <message>",
                "§7Sends a message in the KIC Chat/IRC/bridge\n\n" +
                        "§7Aliases: /kchat, /kicchat\n\n" +
                        "§6Subcommands:\n" +
                        "§e  - /kc toggle §7→ Toggles KIC Chat ON/OFF",
                "/kc "
        ));

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

        String lastMsg = "\n§2[Hover] §7shows info, §2[Click] §7suggests command";
        if (hasPremium()) {
            lastMsg += ", §6* §7KIC+ command";
            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§6* §a/kcp <message>",
                    "§7Sends a message in the KIC+ Chat/IRC/bridge\n\n" +
                            "§7Aliases: /kchatplus, /kicchatplus\n\n" +
                            "§6Subcommands:\n" +
                            "§e  - /kcp toggle §7→ Toggles KIC+ Chat ON/OFF",
                    "/kcp "
            ));

            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§6* §a/kic checkparty",
                    "§7Checks all members in your party for KIC users",
                    "/kic checkparty"
            ));

            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§6* §a/kic lastparty <player>",
                    "§7Tells you the last dungeon party the player was in",
                    "/kic lastparty "
            ));

            sender.addChatMessage(createHoverAndClickComponentSuggest(
                    true,
                    "§6* §a/kic status <player>",
                    "§7Tells you if the player is currently online or not",
                    "/kic status "
            ));
        }

        sender.addChatMessage(new ChatComponentText(lastMsg));
    }

    private void sendKuudraPb(ICommandSender sender) {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        Long pb = KIC.userData.getKuudraPersonalBest();

        String msg = pb == null || pb == 0
                ? KICPrefix + " §cNo Kuudra T5 Personal Best yet!"
                : String.format("%s §aKuudra T5 Personal Best: §f%s", KICPrefix, formatElapsedTimeMs(pb));

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

        if (args.length == 2 && args[0].equalsIgnoreCase("chat")) {
            return getListOfStringsMatchingLastWord(args, chatModes);
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
