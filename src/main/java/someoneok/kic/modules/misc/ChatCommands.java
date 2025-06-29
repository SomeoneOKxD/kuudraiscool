package someoneok.kic.modules.misc;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonObject;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.PartyUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.utils.GeneralUtils.sendCommand;
import static someoneok.kic.utils.StringUtils.*;

public class ChatCommands {
    private static final Set<String> VALID_COMMANDS = new HashSet<>();

    private static final Pattern PARTY_REGEX = Pattern.compile("Party > (?:\\[[^]]+] )?([\\w\\s<3]+): (.+)");
    private static final Pattern DM_REGEX = Pattern.compile("From (?:\\[[^]]+] )?([\\w\\s<3]+): (.+)");
    private static final Pattern COMMAND_REGEX = Pattern.compile("\\.(runs|stats|rtca|cata|kick|kic)\\b(?:\\s+(\\w+))?");

    private static final long TOTAL_XP = 569809640;
    private static final long SEL_CLASS_XP = 360000;
    private static final long UNSEL_CLASS_XP = 90000;
    private static final int AVG_RUN_TIME_MINUTES = 7;

    static {
        VALID_COMMANDS.add("runs");
        VALID_COMMANDS.add("stats");
        VALID_COMMANDS.add("cata");
        VALID_COMMANDS.add("kick");
        VALID_COMMANDS.add("rtca");
        VALID_COMMANDS.add("kic");
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!ApiUtils.isVerified() || (!KICConfig.partyCommands && !KICConfig.dmCommands)) return;
        String message = removeUnicode(removeFormatting(event.message.getUnformattedText()));

        if (!message.startsWith("Party") && !message.startsWith("From")) return;

        Matcher matcher;
        String sender, chatMessage, messageType;

        if ((matcher = PARTY_REGEX.matcher(message)).matches()) {
            if (!KICConfig.partyCommands) return;
            sender = matcher.group(1);
            chatMessage = matcher.group(2);
            messageType = "Party";
        } else if ((matcher = DM_REGEX.matcher(message)).matches()) {
            if (!KICConfig.dmCommands) return;
            sender = matcher.group(1);
            chatMessage = matcher.group(2);
            messageType = "DM";
        } else {
            return;
        }

        processCommand(sender, chatMessage, messageType);
    }

    private void processCommand(String sender, String chatMessage, String messageType) {
        Matcher commandMatcher = COMMAND_REGEX.matcher(chatMessage);
        if (!commandMatcher.find()) return;

        String command = commandMatcher.group(1).toLowerCase();
        String targetPlayer = commandMatcher.group(2);

        if ("DM".equals(messageType) && "kick".equals(command)) return;
        if (!VALID_COMMANDS.contains(command)) return;

        KICLogger.info("[" + messageType + "] " + sender + " used command: ." + command +
                (targetPlayer != null ? " on " + targetPlayer : ""));

        String cmd = "Party".equals(messageType) ? "/pc" : "/tell " + sender;
        String player = (targetPlayer != null) ? targetPlayer : sender;

        switch (command) {
            case "kick":
                if (!KICConfig.partyCommandKick || !PartyUtils.amILeader()) return;
                sendCommand("/party kick " + (targetPlayer != null ? targetPlayer : "51fe055890e7463383a8feac3a7d3708"));
                break;
            case "kic":
                sendKicDiscord(cmd);
                break;
            default:
                if (isCommandDisabled(command, messageType)) return;
                getData(cmd, player, command);
                break;
        }
    }

    private void sendKicDiscord(String cmd) {
        sendCommand(cmd + " KIC > " + KIC.discordUrl);
    }

    private boolean isCommandDisabled(String command, String messageType) {
        switch (command) {
            case "runs":
                return (!"Party".equals(messageType) || !KICConfig.partyCommandRuns) &&
                        (!"DM".equals(messageType) || !KICConfig.dmCommandRuns);
            case "stats":
                return (!"Party".equals(messageType) || !KICConfig.partyCommandStats) &&
                        (!"DM".equals(messageType) || !KICConfig.dmCommandStats);
            case "cata":
                return (!"Party".equals(messageType) || !KICConfig.partyCommandCata) &&
                        (!"DM".equals(messageType) || !KICConfig.dmCommandCata);
            case "rtca":
                return (!"Party".equals(messageType) || !KICConfig.partyCommandRtca) &&
                        (!"DM".equals(messageType) || !KICConfig.dmCommandRtca);
            default:
                return true;
        }
    }

    private void getData(String cmd, String player, String command) {
        if (!ApiUtils.isVerified()) return;
        String formattedPlayer = formatPlayerName(player);
        if (formattedPlayer == null) return;

        Multithreading.runAsync(() -> {
            try {
                JsonObject playerInfo = JsonUtils.parseString(NetworkUtils.sendGetRequest(
                        "https://api.sm0kez.com/hypixel/info/" + formattedPlayer + "?type=STATS", true)).getAsJsonObject();
                if (playerInfo == null || !playerInfo.isJsonObject()) return;

                JsonObject info = playerInfo.getAsJsonObject("playerInfo");
                JsonObject stats = playerInfo.getAsJsonObject("stats");
                JsonObject dungeonXp = playerInfo.getAsJsonObject("dungeonXp");

                String user = info.get("username").getAsString();

                int infernalComps = stats.get("infernal_runs").getAsInt();
                int totalComps = stats.get("basic_runs").getAsInt() +
                        stats.get("hot_runs").getAsInt() +
                        stats.get("burning_runs").getAsInt() +
                        stats.get("fiery_runs").getAsInt() +
                        infernalComps;

                double cataLevel = stats.get("cata_level").getAsDouble();
                int magicalPower = stats.get("magical_power").getAsInt();
                int lifeline = playerInfo.get("lifeline").getAsInt();
                int dominance = playerInfo.get("dominance").getAsInt();
                int manaPool = playerInfo.get("manaPool").getAsInt();

                String output = generateCommandOutput(command, user, totalComps, infernalComps, dominance, lifeline, manaPool, magicalPower, cataLevel, dungeonXp);
                if (output != null) {
                    sendCommand(cmd + output);
                }
            } catch (Exception ignored) {}
        });
    }

    private String generateCommandOutput(String command, String user, int totalComps, int infernalComps, int dominance, int lifeline, int manaPool, int magicalPower, double cataLevel, JsonObject dungeonXp) {
        switch (command) {
            case "runs":
                return String.format(" %s > %d runs (%d)", user, infernalComps, totalComps);
            case "stats":
                return String.format(" %s > %s: %d | Mana Pool: %d | Magical Power: %d | Runs: %d | Cata: %.2f",
                        user, dominance > lifeline ? "Dominance" : "Lifeline",
                        Math.max(dominance, lifeline), manaPool, magicalPower, totalComps, cataLevel);
            case "cata":
                return String.format(" %s > Cata: %.2f", user, cataLevel);
            case "rtca":
                return generateRtcaOutput(user, dungeonXp);
            default:
                return null;
        }
    }

    private String generateRtcaOutput(String user, JsonObject dungeonXp) {
        StringBuilder output = new StringBuilder(" ").append(user).append(" > ");

        Map<String, Long> classXP = calculateClassXP(dungeonXp);

        Map<String, Integer> runs = calculateRuns(classXP);

        int totalHours = Math.round((runs.values().stream().mapToInt(Integer::intValue).sum() * AVG_RUN_TIME_MINUTES) / 60f);

        output.append("H: ").append(runs.get("healer"))
                .append(" - M: ").append(runs.get("mage"))
                .append(" - B: ").append(runs.get("berserk"))
                .append(" - A: ").append(runs.get("archer"))
                .append(" - T: ").append(runs.get("tank"))
                .append(" (").append(totalHours).append("h)")
                .append(" (").append(AVG_RUN_TIME_MINUTES).append("min/run)");

        return output.toString();
    }

    private Map<String, Long> calculateClassXP(JsonObject dungeonXp) {
        Map<String, Long> classXP = new HashMap<>();
        classXP.put("healer", TOTAL_XP - dungeonXp.get("healerXp").getAsLong());
        classXP.put("mage", TOTAL_XP - dungeonXp.get("mageXp").getAsLong());
        classXP.put("berserk", TOTAL_XP - dungeonXp.get("berserkXp").getAsLong());
        classXP.put("archer", TOTAL_XP - dungeonXp.get("archerXp").getAsLong());
        classXP.put("tank", TOTAL_XP - dungeonXp.get("tankXp").getAsLong());
        return classXP;
    }

    private Map<String, Integer> calculateRuns(Map<String, Long> classXP) {
        Map<String, Integer> runs = new HashMap<>();
        runs.put("healer", 0);
        runs.put("mage", 0);
        runs.put("berserk", 0);
        runs.put("archer", 0);
        runs.put("tank", 0);

        while (classXP.values().stream().anyMatch(xp -> xp > 0)) {
            String highestXPClass = classXP.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (highestXPClass == null) break;

            classXP.replaceAll((className, xp) -> xp - (className.equals(highestXPClass) ? SEL_CLASS_XP : UNSEL_CLASS_XP));

            runs.put(highestXPClass, runs.get(highestXPClass) + 1);
        }
        return runs;
    }
}
