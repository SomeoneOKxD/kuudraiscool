package someoneok.kic.utils;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import someoneok.kic.KIC;
import someoneok.kic.events.HypixelJoinEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyUtils {
    private static Set<UUID> members = new HashSet<>();
    private static Optional<UUID> leader = Optional.empty();
    private static boolean updated = false;

    private static final Pattern[] PARTY_DISBANDED_PATTERNS = {
            Pattern.compile("^.+ has disbanded the party!$"),
            Pattern.compile("^The party was disbanded because (.+)$"),
            Pattern.compile("^You left the party.$"),
            Pattern.compile("^You are not currently in a party.$"),
            Pattern.compile("^You have been kicked from the party by .+$")
    };

    private static final Pattern[] UPDATE_PATTERNS = {
            Pattern.compile("^You have joined (.+)'s party!$"),
            Pattern.compile("^The party was transferred to (.+) by .+$"),
            Pattern.compile("^(.+) has promoted (.+) to Party Leader$"),
            Pattern.compile("^(.+) joined the party.$"),
            Pattern.compile("^(.+) has been removed from the party.$"),
            Pattern.compile("^(.+) has left the party.$"),
            Pattern.compile("^(.+) was removed from your party because they disconnected.$"),
            Pattern.compile("^Kicked (.+) because they were offline.$"),
            Pattern.compile("^Party Finder > (.+) joined the .+$")
    };

    public static void onPartyInfo(ClientboundPartyInfoPacket event) {
        updated = true;
        members = event.getMembers();
        leader = event.getLeader();
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        Multithreading.schedule(this::updatePartyData, 5000, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent()
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        clearPartyData();
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();

        for (Pattern pattern : UPDATE_PATTERNS) {
            Matcher matcher = pattern.matcher(msg);
            if (matcher.matches()) {
                updatePartyData();
                return;
            }
        }

        for (Pattern pattern : PARTY_DISBANDED_PATTERNS) {
            Matcher matcher = pattern.matcher(msg);
            if (matcher.matches()) {
                clearPartyData();
                return;
            }
        }
    }

    private void updatePartyData() {
        updated = false;
        Multithreading.schedule(() -> {
            if (!updated) {
                HypixelEventApi.sendPartyPacket();
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private void clearPartyData() {
        leader = Optional.empty();
        members = new HashSet<>();
    }

    public static boolean inParty() {
        return !members.isEmpty();
    }

    public static boolean amILeader() {
        return leader.isPresent() && Objects.equals(leader.get(), KIC.mc.thePlayer.getUniqueID());
    }

    public static Set<UUID> getMembers() {
        return members;
    }
}
