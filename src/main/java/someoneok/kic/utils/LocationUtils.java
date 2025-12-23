package someoneok.kic.utils;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.hypixel.data.type.GameType;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.HypixelJoinEvent;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.events.LocationUpdateEvent;
import someoneok.kic.models.Island;
import someoneok.kic.models.kuudra.CrimsonFaction;
import someoneok.kic.utils.data.DataHandler;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.removeFormatting;
import static someoneok.kic.utils.StringUtils.removeUnicode;

public class LocationUtils {
    private static final Pattern AREA_PATTERN = Pattern.compile("\\s*⏣\\s+(.+)");
    private static final Pattern TAB_FACTION_PATTERN = Pattern.compile("(Mage|Barbarian) Reputation");

    public static Island currentIsland;
    public static String serverName;
    public static boolean onSkyblock = false;
    public static boolean onHypixel = false;
    public static String subArea;
    public static String lastSubArea;
    public static boolean inDungeons = false;

    private static boolean inKuudra = false;
    private static int kuudraTier = 0;

    private static final AxisAlignedBB croesusArea = new AxisAlignedBB(
            -16, 118, 30,
            -39, 145, 48
    );

    public static void handleLocationPacket(ClientboundLocationPacket packet) {
        serverName = packet.getServerName();

        if ("limbo".equalsIgnoreCase(serverName)) {
            setLocationState(Island.LIMBO);
            return;
        }

        packet.getServerType().ifPresent(serverType -> {
            if (serverType != GameType.SKYBLOCK) {
                setLocationState(Island.LOBBY);
            } else {
                currentIsland = packet.getMap()
                        .map(mapName -> Arrays.stream(Island.values())
                                .filter(island -> mapName.equals(island.getName()))
                                .findFirst()
                                .orElse(Island.LIMBO))
                        .orElse(Island.NO_CLUE);
                onSkyblock = currentIsland != Island.LIMBO;
                inDungeons = currentIsland == Island.DUNGEON;
                inKuudra = currentIsland == Island.KUUDRA;
            }
        });

        if (currentIsland == null) {
            setLocationState(Island.LIMBO);
        }
        subArea = "";
        lastSubArea = "";
        KICEventBus.post(new LocationUpdateEvent.Island(currentIsland, serverName));
        KICLogger.info("Current island: " + currentIsland);
    }

    private static void setLocationState(Island island) {
        currentIsland = island;
        onSkyblock = false;
    }

    public static boolean inKuudra() {
        return (KICConfig.forceKuudraLocation || inKuudra);
    }

    public static int kuudraTier() {
        if (KICConfig.forceKuudraTier != 0) return KICConfig.forceKuudraTier;
        return kuudraTier;
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        if (!ApiUtils.getApiKeyMessage().isEmpty()) {
            Multithreading.schedule(() -> {
                sendMessageToPlayer(ApiUtils.getApiKeyMessage());
                ApiUtils.setApiKeyMessage("");
            }, 5000, TimeUnit.MILLISECONDS);
        }
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        reset();
    }

    private int ticks = 0;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return;
        if (!onSkyblock) return;

        ticks++;
        if (ticks % 20 != 0) return;
        ticks = 0;

        List<String> scoreboardLines = getScoreboardLines();
        String subAreaTemp = removeUnicode(removeFormatting(getMatchFromLines(scoreboardLines)));

        if (inKuudra && subAreaTemp.contains("Kuudra's Hollow")) {
            if (subAreaTemp.contains("(T1)")) kuudraTier = 1;
            else if (subAreaTemp.contains("(T2)")) kuudraTier = 2;
            else if (subAreaTemp.contains("(T3)")) kuudraTier = 3;
            else if (subAreaTemp.contains("(T4)")) kuudraTier = 4;
            else if (subAreaTemp.contains("(T5)")) kuudraTier = 5;
        } else {
            kuudraTier = 0;
        }

        subArea = inCroesusArea() ? "Croesus" : subAreaTemp.replaceAll("\\s*\\([^)]*\\)", "");

        if (currentIsland == Island.CRIMSON_ISLE) {
            List<String> tabList = getTabListLines();
            if (!tabList.isEmpty()) {
                int factionIdx = findMatchingIndex(tabList);
                if (factionIdx != -1) {
                    CrimsonFaction faction = CrimsonFaction.fromString(extractFaction(tabList.get(factionIdx)));
                    if (faction != null && KIC.userData.getFaction() != faction) {
                        KIC.userData.setFaction(faction);
                        DataHandler.saveData();
                    }
                }
            }
        }

        if (!Objects.equals(lastSubArea, subArea)) {
            lastSubArea = subArea;
            KICEventBus.post(new LocationUpdateEvent.SubArea(currentIsland, serverName, subArea));
        }
    }

    private static boolean inCroesusArea() {
        if (currentIsland != Island.DUNGEON_HUB) return false;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return false;
        return croesusArea.isVecInside(player.getPositionVector());
    }

    public static List<String> getScoreboardLines() {
        if (mc.theWorld == null) return Collections.emptyList();

        final Scoreboard sb = mc.theWorld.getScoreboard();
        if (sb == null) return Collections.emptyList();

        final ScoreObjective obj = sb.getObjectiveInDisplaySlot(1);
        if (obj == null) return Collections.emptyList();

        final ArrayDeque<Score> window = new ArrayDeque<>(15);
        for (Score s : sb.getSortedScores(obj)) {
            if (s == null) continue;
            final String name = s.getPlayerName();
            if (name == null || name.startsWith("#")) continue;

            if (window.size() == 15) window.pollFirst();
            window.addLast(s);
        }

        if (window.isEmpty()) return Collections.emptyList();

        final ArrayList<String> lines = new ArrayList<>(window.size());
        for (Score s : window) {
            final String name = s.getPlayerName();
            final ScorePlayerTeam team = sb.getPlayersTeam(name);
            String line = ScorePlayerTeam.formatPlayerName(team, name);
            lines.add(removeFormatting(line));
        }

        return lines;
    }

    private String getMatchFromLines(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = AREA_PATTERN.matcher(line);
            if (matcher.find()) return matcher.group(1);
        }
        return "";
    }

    private List<String> getTabListLines() {
        if (mc.getNetHandler() == null) return new ArrayList<>();
        return mc.getNetHandler().getPlayerInfoMap().stream()
                .map(info -> info.getDisplayName() != null ? removeFormatting(info.getDisplayName().getFormattedText()) : "")
                .collect(Collectors.toList());
    }

    private int findMatchingIndex(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (LocationUtils.TAB_FACTION_PATTERN.matcher(list.get(i)).find()) {
                return i;
            }
        }
        return -1;
    }

    private String extractFaction(String line) {
        return removeFormatting(line).split(" ")[0];
    }

    private void reset() {
        currentIsland = null;
        serverName = "";
        onSkyblock = false;
        onHypixel = false;
        subArea = "";
        lastSubArea = "";
        inDungeons = false;
        inKuudra = false;
        kuudraTier = 0;
    }
}
