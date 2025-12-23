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
import someoneok.kic.config.sharing.ConfigSharing;
import someoneok.kic.config.sharing.ConfigSharingGUI;
import someoneok.kic.models.misc.ChatMode;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;
import someoneok.kic.modules.kuudra.KuudraUserInfo;
import someoneok.kic.modules.misc.ChatHandler;
import someoneok.kic.modules.misc.TrackEmptySlots;
import someoneok.kic.modules.premium.Misc;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.Updater;
import someoneok.kic.utils.data.DataHandler;
import someoneok.kic.utils.overlay.EditHudScreen;

import java.util.*;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.hasPremium;
import static someoneok.kic.utils.ChatUtils.*;
import static someoneok.kic.utils.StringUtils.formatElapsedTimeMs;
import static someoneok.kic.utils.StringUtils.isValidUUIDv4RegexBased;

public class KICCommand extends CommandBase {
    private static final List<String> COMMANDS = new ArrayList<>(Arrays.asList(
            "help",                                                     // Help menu
            "settings",                                                 // Config GUI
            "apikey", "key",                                            // Set kic api key
            "verify", "verifykey",                                      // Verify kic api key
            "resetprofittracker", "rpt", "resetpt",                     // Reset profit tracker
            "skpt", "sharekpt", "sharetracker",                         // Share profit tracker
            "kuudra",                                                   // Kuudra info
            "t1", "t2", "t3", "t4", "t5",                               // Start tier 1-5
            "sharing", "sharinghelp",                                   // Config sharing help command
            "importconfig", "shareconfig", "myconfigs", "deleteconfig", // Config sharing commands
            "configsharing",                                            // Config sharing GUI
            "edithuds",                                                 // Edit all kic huds
            "pb", "resetpb", "resetkpb",                                // Kuudra pb commands
            "chat",                                                     // Kic chat commands
            "premium",                                                  // Kic premium info
            "ecbp",                                                     // Open empty ender chest or backpack
            "checkupdates",                                             // Check for updates
            "discord"                                                   // Discord invite link
    ));
    private static final List<String> CHAT_MODES = Arrays.asList("kc", "kcp", "mc");

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

            case "discord":
                sender.addChatMessage(createHoverAndClickComponent(true, KIC.KICPrefix + " §aClick here to join the §9Discord§a!", "§7Open Discord invite:\n§9" + KIC.discordUrl, ClickEvent.Action.OPEN_URL, KIC.discordUrl));
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

            case "verify":
            case "verifykey":
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
                DataHandler.saveData();
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

            case "sharing":
            case "sharinghelp":
                sendConfigSharingHelp(sender);
                break;

            case "configsharing":
                ConfigSharingGUI.open();
                break;

            case "importconfig":
                if (args.length != 2) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic importconfig <configId>"));
                    return;
                }
                String configId = args[1].trim();
                if (configId.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic importconfig <configId>"));
                    return;
                }

                ConfigSharing.importConfig(configId);
                break;

            case "shareconfig":
                ConfigSharing.shareConfig(null);
                break;

            case "myconfigs":
                ConfigSharing.myConfigs();
                break;

            case "deleteconfig":
                if (args.length != 2) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic deleteconfig <configId>"));
                    return;
                }
                String configId2 = args[1].trim();
                if (configId2.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText(KIC.KICPrefix + " §cUsage: /kic deleteconfig <configId>"));
                    return;
                }

                ConfigSharing.deleteConfig(configId2);
                break;

            case "help":
            default:
                sendKICHelpMsg(sender);
                break;
        }
    }

    private void sendKICHelpMsg(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(String.format("\n%s §a§lCommands", KIC.KICPrefix)));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic",
                "§7Opens the settings menu\n\n§7Alias: /kic settings",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic help",
                "§7Shows this menu",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic help"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic apikey <key>",
                "§7Change/set your api key\n§7§o(Use your KIC api key, not Hypixel)\n\n§7Alias: /kic key",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic apikey "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic verify",
                "§7Verifies the status of your current api key\n\n§7Alias: /kic verifykey",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic verify"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic discord",
                "§7Join the KIC discord server.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic discord"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic edithuds",
                "§7Edit all enabled huds.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic edithuds"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic chat kc/kcp/mc",
                "§7Switches the chat mode to KIC Chat/KIC+ Chat/Minecraft (Same as /chat party)",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic chat "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic kuudra [player]",
                "§7Check Kuudra info for a player\n\n§7Example: /kic kuudra xaned\n§7Alias: /kic kuudra",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic kuudra "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic resetprofittracker",
                "§7Reset Kuudra profit tracker data\n\n§7Aliases: /kic rpt, /kic resetpt",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic resetprofittracker"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic sharetracker",
                "§7Share your profit tracker in the KIC discord\n\n§7Aliases: /kic skpt, /kic sharekpt",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic sharetracker"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic pb",
                "§7Show your current kuudra T5 personal best",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic pb"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic resetpb",
                "§7Resets your kuudra T5 personal best\n\n§7Alias: /kic resetkpb",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic resetpb"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic ecbp",
                "§7Opens an ender chest page or backpack with empty slots",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic ecbp"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic sharinghelp",
                "§7Help menu for config sharing.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic sharinghelp"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic checkupdates",
                "§7Checks whether a new version of the mod is available.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic checkupdates"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic premium",
                "§7Info about the premium features in KIC.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic premium"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/lf <player> <item>",
                "§7Searches a player for a specific item\n\n§7Example 1: /lf " + sender.getName() + " Hyperion\n§7Example 2: /lf " + sender.getName() + " lore: Ability: Wither Impact\n§7Aliases: /lookingfor, /lookingforitem, /lfi",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/lf "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kc <message>",
                "§7Sends a message in the KIC Chat/IRC/bridge\n\n" +
                        "§7Aliases: /kchat, /kicchat\n\n" +
                        "§6Subcommands:\n" +
                        "§e  - /kc toggle §7→ Toggles KIC Chat ON/OFF",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kc "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kicdoublepearl help",
                "§7Help menu for custom double pearls.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kicdoublepearl help"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kicaddons help",
                "§7Help menu for kic addons.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kicaddons help"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/t1 - /t2 - /t3 - /t4 - /t5",
                "§7Join T1, T2, T3, T4 or T5 Kuudra Instance\n\n§7Alias: /kic t1, /kic t2, /kic t3, /kic t4, /kic t5",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/t"
        ));

        String lastMsg = "\n§2[Hover] §7shows info, §2[Click] §7suggests command";
        if (hasPremium()) {
            lastMsg += ", §6* §7KIC+ command";
            sender.addChatMessage(createHoverAndClickComponent(
                    true,
                    "§6* §a/kcp <message>",
                    "§7Sends a message in the KIC+ Chat/IRC/bridge\n\n" +
                            "§7Aliases: /kchatplus, /kicchatplus\n\n" +
                            "§6Subcommands:\n" +
                            "§e  - /kcp toggle §7→ Toggles KIC+ Chat ON/OFF",
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/kcp "
            ));

            sender.addChatMessage(createHoverAndClickComponent(
                    true,
                    "§6* §a/kic checkparty",
                    "§7Checks all members in your party for KIC users",
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/kic checkparty"
            ));

            sender.addChatMessage(createHoverAndClickComponent(
                    true,
                    "§6* §a/kic status <player>",
                    "§7Tells you if the player is currently online or not",
                    ClickEvent.Action.SUGGEST_COMMAND,
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

    private void sendConfigSharingHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(String.format("\n%s §a§lConfig Sharing Commands", KIC.KICPrefix)));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic configsharing",
                "§7Opens the §aconfig sharing GUI§7.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic configsharing"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic importconfig <id>",
                "§7Imports a shared config using the provided ID.\n\n§7Example: §f/kic importconfig tobodefade",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic importconfig "
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic shareconfig",
                "§7Shares your current config with others.\n§7Returns a unique ID you can send to friends.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic shareconfig"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic myconfigs",
                "§7Lists all configs you've shared.",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic myconfigs"
        ));

        sender.addChatMessage(createHoverAndClickComponent(
                true,
                "§8* §a/kic deleteconfig <id>",
                "§7Deletes one of your shared configs by ID.\n\n§7Example: §f/kic deleteconfig tobodefade",
                ClickEvent.Action.SUGGEST_COMMAND,
                "/kic deleteconfig "
        ));

        sender.addChatMessage(new ChatComponentText("\n§2[Hover] §7shows info, §2[Click] §7suggests command"));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, COMMANDS);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("chat")) {
            return getListOfStringsMatchingLastWord(args, CHAT_MODES);
        }

        return Collections.emptyList();
    }

    public static void addPremiumCommands() {
        COMMANDS.add("checkparty");
        COMMANDS.add("status");
    }

    public static void removePremiumCommands() {
        COMMANDS.remove("checkparty");
        COMMANDS.remove("status");
    }
}
