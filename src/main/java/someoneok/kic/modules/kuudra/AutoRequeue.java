package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.PartyUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ChatUtils.sendCommand;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class AutoRequeue {
    private static final Pattern DT_REQUEST = Pattern.compile(
            "^Party > (?:\\[[^]]+] )?(\\w+): [!.](dt|downtime|undt|undowntime)(?:\\s+(.*))?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Set<UUID> partyAtStart = new HashSet<>();
    private static final HashMap<String, String> dtRequests = new HashMap<>();

    private boolean downtimeMentioned = false;
    private boolean requeued = false;
    private boolean disableRequeue = false;

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!LocationUtils.inKuudra()
                || !ApiUtils.isVerified()
                || !PartyUtils.inParty()
                || !KICConfig.autoRequeue
                || requeued) return;

        String raw = removeFormatting(event.message.getUnformattedText());
        String lower = raw.toLowerCase(Locale.ROOT);

        if ("[NPC] Elle: Talk with me to begin!".equals(raw)) {
            initParty();
            return;
        }

        if (containsDtTrigger(lower)) {
            handleDTRequest(raw);
            return;
        }

        if (lower.contains("you have been re-queued!")) {
            requeued = true;
            return;
        }

        if (!PartyUtils.amILeader() || !isSamePartyAsStart()) return;

        String trimmed = lower.trim().replace(" ", "");
        if (trimmed.startsWith("kuudradown") || trimmed.startsWith("defeat")) {
            if (disableRequeue) {
                mentionDowntime();
                return;
            }
            sendCommand("/instancerequeue");
            return;
        }

        if (raw.contains("Click HERE to re-queue into Kuudra's Hollow!")) {
            if (disableRequeue) {
                mentionDowntime();
                return;
            }
            sendCommand("/instancerequeue");
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        reset();
    }

    private static boolean containsDtTrigger(String s) {
        return s.contains("!dt")
                || s.contains(".dt")
                || s.contains("!downtime")
                || s.contains(".downtime")
                || s.contains("!undt")
                || s.contains(".undt")
                || s.contains("!undowntime")
                || s.contains(".undowntime");
    }

    private void handleDTRequest(String raw) {
        Matcher matcher = DT_REQUEST.matcher(raw);
        if (!matcher.matches()) return;

        String player = matcher.group(1);
        String cmd = matcher.group(2);
        String reason = matcher.group(3);

        String cleanPlayer = (player == null || player.trim().isEmpty())
                ? "Someone"
                : player.trim();

        String cleanCmd = (cmd == null) ? "" : cmd.trim().toLowerCase(Locale.ROOT);

        boolean isUndo = cleanCmd.equals("undt") || cleanCmd.equals("undowntime");
        if (isUndo) {
            boolean existed = dtRequests.remove(cleanPlayer) != null;

            if (!existed) {
                sendMessageToPlayer(String.format(
                        "%s §6%s §chas no reminder set!",
                        KICPrefix,
                        cleanPlayer
                ));
                return;
            }

            sendMessageToPlayer(String.format(
                    "%s §aReminder removed!",
                    KICPrefix
            ));

            if (dtRequests.isEmpty()) {
                disableRequeue = false;
                downtimeMentioned = false;
            }
            return;
        }

        String cleanReason = (reason == null || reason.trim().isEmpty())
                ? "No reason given"
                : reason.trim();

        dtRequests.put(cleanPlayer, cleanReason);
        disableRequeue = true;

        sendMessageToPlayer(String.format(
                "%s §d%s §7requested DT§7: §f%s",
                KICPrefix,
                cleanPlayer,
                cleanReason
        ));
    }

    private boolean isSamePartyAsStart() {
        Set<UUID> currentParty = PartyUtils.getMembers();
        return currentParty != null
                && currentParty.size() == partyAtStart.size()
                && currentParty.containsAll(partyAtStart);
    }

    private void mentionDowntime() {
        if (downtimeMentioned) return;

        Set<String> snapshot = new HashSet<>(dtRequests.keySet());

        Multithreading.schedule(() -> sendMessageToPlayer(String.format(
                "%s §cAuto-requeue paused. DT requested by: §e%s",
                KICPrefix,
                String.join(", ", snapshot)
        )), 500, TimeUnit.MILLISECONDS);

        downtimeMentioned = true;
    }

    private void initParty() {
        partyAtStart.clear();
        Set<UUID> members = PartyUtils.getMembers();
        if (members != null) partyAtStart.addAll(members);
    }

    private void reset() {
        dtRequests.clear();
        partyAtStart.clear();
        downtimeMentioned = false;
        requeued = false;
        disableRequeue = false;
    }
}
