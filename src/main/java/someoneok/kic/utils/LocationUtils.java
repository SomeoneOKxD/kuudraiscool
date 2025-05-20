package someoneok.kic.utils;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.hypixel.data.type.GameType;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import someoneok.kic.KIC;
import someoneok.kic.commands.FuckOtherMods;
import someoneok.kic.events.HypixelJoinEvent;
import someoneok.kic.models.Island;
import someoneok.kic.models.kuudra.CrimsonFaction;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.OverlayDataHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.removeFormatting;
import static someoneok.kic.utils.StringUtils.removeUnicode;

public class LocationUtils {
    private static final Pattern AREA_PATTERN = Pattern.compile(" ⏣ (.+)");
    private static final Pattern TAB_FACTION_PATTERN = Pattern.compile("(Mage|Barbarian) Reputation");

    public static Island currentIsland;
    public static String serverName;
    public static boolean onSkyblock = false;
    public static boolean onHypixel = false;
    public static String subArea;
    public static boolean inDungeons = false;
    public static boolean inKuudra = false;
    public static int kuudraTier = 0;

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
        KICLogger.info("Current island: " + currentIsland);
    }

    private static void setLocationState(Island island) {
        currentIsland = island;
        onSkyblock = false;
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinEvent event) {
        FuckOtherMods.fuckThem();
        if (!ApiUtils.getApiKeyMessage().isEmpty()) {
            Multithreading.schedule(() -> {
                sendMessageToPlayer(ApiUtils.getApiKeyMessage());
                ApiUtils.setApiKeyMessage("");
            }, 5000, TimeUnit.MILLISECONDS);
        }
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        onHypixel = false;
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

        subArea = subAreaTemp.replaceAll(" \\(.+\\)", "");

        if (currentIsland == Island.CRIMSON_ISLE) {
            List<String> tabList = getTabListLines();
            if (tabList.isEmpty()) return;

            int factionIdx = findMatchingIndex(tabList);

            if (factionIdx != -1) {
                CrimsonFaction faction = CrimsonFaction.fromString(extractFaction(tabList.get(factionIdx)));

                if (KIC.userData.getFaction() != faction) {
                    KIC.userData.setFaction(faction);
                    OverlayDataHandler.saveOverlaysData();
                }
            }
        }
    }

    private List<String> getScoreboardLines() {
        List<String> lines = new ArrayList<>();
        if (mc.theWorld == null) return lines;

        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return lines;

        List<Score> scores = scoreboard.getSortedScores(objective).stream()
                .filter(score -> score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#"))
                .collect(Collectors.toList());

        if (scores.size() > 15) {
            scores = scores.subList(scores.size() - 15, scores.size());
        }

        scores.forEach(score -> {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            String line = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
            lines.add(removeFormatting(line));
        });

        return lines;
    }

    private String getMatchFromLines(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = AREA_PATTERN.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
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
}
