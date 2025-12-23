package someoneok.kic.modules.kuudra;

import someoneok.kic.KIC;
import someoneok.kic.config.pages.KuudraSplitsOptions;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.TimedEvent;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.overlay.MovableOverlay;
import someoneok.kic.utils.overlay.OverlayManager;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.TreeMap;

import static someoneok.kic.models.Color.getColorCode;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.formatElapsedTimeMs;

public class KuudraSplits {
    private static final EnumMap<KuudraPhase, long[]> colorMapping = new EnumMap<>(KuudraPhase.class);
    private static final KuudraPhase[] SPLIT_PHASES = {
            KuudraPhase.SUPPLIES,
            KuudraPhase.BUILD,
            KuudraPhase.EATEN,
            KuudraPhase.STUN,
            KuudraPhase.DPS,
            KuudraPhase.SKIP,
            KuudraPhase.KILL,
            KuudraPhase.END
    };
    private static final KuudraPhase[] REPORT_PHASES = Arrays.copyOf(SPLIT_PHASES, SPLIT_PHASES.length - 1);

    private static String[] kuudraSplitColors;
    private static String paceColor;
    private static String supplyTimesColor;
    private static String freshTimesColor;

    private static String lastKuudraSplitText = "";
    private static String lastSupplyTimesText = "";
    private static String lastFreshTimesText = "";
    private static boolean splitsOverlayModified = false;
    private static boolean supplyTimesOverlayModified = false;
    private static boolean freshTimesOverlayModified = false;

    private static final StringBuilder sbSupplyTimes = new StringBuilder();
    private static final StringBuilder sbFreshTimes = new StringBuilder();
    private static final StringBuilder sbSplits = new StringBuilder();

    static {
        colorMapping.put(KuudraPhase.SUPPLIES, new long[]{29500, 32000});
        colorMapping.put(KuudraPhase.BUILD,    new long[]{16000, 19000});
        colorMapping.put(KuudraPhase.EATEN,    new long[]{5800,  6200});
        colorMapping.put(KuudraPhase.STUN,     new long[]{200,   500});
        colorMapping.put(KuudraPhase.DPS,      new long[]{5700,  6300});
        colorMapping.put(KuudraPhase.SKIP,     new long[]{4200,  5000});
        colorMapping.put(KuudraPhase.KILL,     new long[]{2200,  2500});
        colorMapping.put(KuudraPhase.END,      new long[]{65000, 75000});

        updateSplitColors();
    }

    public static void updateSplitColors() {
        kuudraSplitColors = new String[]{
                getColorCode(KuudraSplitsOptions.suppliesColor),
                getColorCode(KuudraSplitsOptions.buildColor),
                getColorCode(KuudraSplitsOptions.eatenColor),
                getColorCode(KuudraSplitsOptions.stunColor),
                getColorCode(KuudraSplitsOptions.dpsColor),
                getColorCode(KuudraSplitsOptions.skipColor),
                getColorCode(KuudraSplitsOptions.killColor),
                getColorCode(KuudraSplitsOptions.overallColor)
        };

        paceColor = getColorCode(KuudraSplitsOptions.paceColor);
        supplyTimesColor = getColorCode(KuudraSplitsOptions.supplyTimesColor);
        freshTimesColor = getColorCode(KuudraSplitsOptions.freshTimesColor);
    }

    public static void updateKuudraSplits() {
        long now = System.currentTimeMillis();
        sbSplits.setLength(0);
        long ticks = Kuudra.getTotalLagTimeTicks();

        for (int i = 0; i < SPLIT_PHASES.length; i++) {
            KuudraPhase phase = SPLIT_PHASES[i];
            long time = phase.getTime(now);
            sbSplits.append("§r").append(kuudraSplitColors[i])
                    .append(phase.getName()).append(": ")
                    .append(getSplitColor(time, phase))
                    .append(formatElapsedTimeMs(time));

            if (KuudraSplitsOptions.showPace) {
                long pace = phase.getPace(now);
                if (pace != 0) {
                    sbSplits.append(" §r§7(§c+")
                            .append(formatElapsedTimeMs(pace)).append("§7)");
                }
            }

            if (KuudraSplitsOptions.showLag) {
                long lagTicks = phase.getLag(ticks);
                if (lagTicks != 0) {
                    sbSplits.append(getLagMessage(lagTicks));
                }
            }

            sbSplits.append("\n");
        }

        if (KuudraSplitsOptions.showEstimatedPace && LocationUtils.kuudraTier() == 5) {
            long estimatedPace = KuudraPhase.getEstimatedPace(now);
            sbSplits.append("§r").append(paceColor).append("Pace: ")
                    .append(getSplitColor(estimatedPace, KuudraPhase.END))
                    .append(formatElapsedTimeMs(estimatedPace));
        }

        String result = sbSplits.toString();
        if (!result.equals(lastKuudraSplitText)) {
            lastKuudraSplitText = result;
            OverlayManager.getOverlay("KuudraSplits").updateText(result.trim());
            splitsOverlayModified = true;
        }
    }

    private static String getSplitColor(Long timeMs, KuudraPhase phase) {
        if (timeMs == null || timeMs == 0 || phase == null || !colorMapping.containsKey(phase)) return "§f";
        long[] thresholds = colorMapping.get(phase);
        return timeMs <= thresholds[0] ? "§a" : timeMs <= thresholds[1] ? "§6" : "§c";
    }

    public static void resetKuudraSplitsOverlay() {
        sbSplits.setLength(0);

        for (int i = 0; i < SPLIT_PHASES.length; i++) {
            sbSplits.append("§r").append(kuudraSplitColors[i])
                    .append(SPLIT_PHASES[i].getName()).append(": §f0.00s\n");
        }

        sbSplits.append("§r").append(paceColor).append("Pace: §f0.00s");

        String splits = sbSplits.toString().trim();
        MovableOverlay kuudraSplits = OverlayManager.getOverlay("KuudraSplits");
        kuudraSplits.updateText(splits);
        kuudraSplits.setExampleText(splits);
    }

    public static void updateSupplyTimes(TreeMap<Integer, TimedEvent> supplyTimes) {
        sbSupplyTimes.setLength(0);
        if (supplyTimes.isEmpty()) {
            sbSupplyTimes.append(supplyTimesColor)
                    .append("§lSupply Times\n§7No placed supplies yet...");
        } else {
            int size = supplyTimes.size();

            sbSupplyTimes.append(supplyTimesColor)
                            .append("§lSupply Times [§r")
                            .append(getRecoveredCountColor(size))
                            .append(size)
                            .append("§r§8/§a6")
                            .append(supplyTimesColor)
                            .append("§l]\n");

            supplyTimes.forEach((key, event) -> sbSupplyTimes.append(event.player)
                            .append(" §r§8(").append(key).append("/6) §r")
                            .append(getRecoveredColor(event.timestamp))
                            .append(formatElapsedTimeMs(event.timestamp)).append("\n"));
        }

        String result = sbSupplyTimes.toString();
        if (!result.equals(lastSupplyTimesText)) {
            lastSupplyTimesText = result;
            OverlayManager.getOverlay("SupplyTimes").updateText(result.trim());
            supplyTimesOverlayModified = true;
        }
    }

    public static void updateFreshTimes(List<TimedEvent> freshTimes) {
        sbFreshTimes.setLength(0);

        if (freshTimes.isEmpty()) {
            sbFreshTimes.append(freshTimesColor).append("§lFresh Times\n§7No freshers...");
        } else {
            int size = freshTimes.size();
            sbFreshTimes.append(freshTimesColor)
                        .append("§lFresh Times [")
                        .append(getFreshCountColor(size))
                        .append(size)
                        .append(freshTimesColor)
                        .append("§l]\n");

            freshTimes.forEach(event -> sbFreshTimes.append(event.player)
                        .append(" ")
                        .append(getFreshColor(event.timestamp))
                        .append(formatElapsedTimeMs(event.timestamp)).append("\n"));
        }

        String result = sbFreshTimes.toString();
        if (!result.equals(lastFreshTimesText)) {
            lastFreshTimesText = result;
            OverlayManager.getOverlay("FreshTimes").updateText(result.trim());
            freshTimesOverlayModified = true;
        }
    }

    public static String getRecoveredColor(long time) {
        if (time >= 23000 && time < 26000) return "§9§l";
        if (time >= 26000 && time < 28000) return "§a§l";
        if (time >= 28000 && time < 29500) return "§2§l";
        if (time >= 29500 && time < 31000) return "§6§l";
        if (time > 31000) return "§c§l";
        return "§f§l";
    }

    private static String getRecoveredCountColor(int size) {
        return size == 5 ? "§2" : size == 6 ? "§a" : "§c";
    }

    private static String getFreshColor(long time) {
        return time <= 5000 ? "§9§l" : time <= 7000 ? "§a§l" : time <= 9000 ? "§6§l" : "§c§l";
    }

    private static String getFreshCountColor(int size) {
        return size == 3 ? "§2" : size == 4 ? "§a" : "§c";
    }

    public static void sendDetailedSplits(long now, long ticks, List<TimedEvent> freshTimes) {
        StringBuilder message = new StringBuilder(KIC.KICPrefix)
                .append(getColorCode(KuudraSplitsOptions.splitsColor))
                .append(" Splits\n");

        for (KuudraPhase phase : REPORT_PHASES) {
            long time = phase.getTime(now);
            message.append("§r")
                    .append(KuudraSplitsOptions.getColorForPhase(phase))
                    .append(phase.getName()).append(": ")
                    .append(getSplitColor(time, phase))
                    .append(formatElapsedTimeMs(time))
                    .append(getLagMessage(phase.getLag(ticks))).append("\n");
        }

        message.append(getColorCode(KuudraSplitsOptions.splitsColor))
                .append("P3: §f")
                .append(formatElapsedTimeMs(KuudraPhase.getP3(now)))
                .append(getLagMessage(KuudraPhase.getP3Lag(ticks))).append("\n")
                .append(getColorCode(KuudraSplitsOptions.splitsColor))
                .append("P4: §f")
                .append(formatElapsedTimeMs(KuudraPhase.getP4(now)))
                .append(getLagMessage(KuudraPhase.getP4Lag(ticks))).append("\n");

        long overallTime = KuudraPhase.END.getTime(now);
        message.append(getColorCode(KuudraSplitsOptions.overallColor))
                .append(KuudraPhase.END.getName()).append(": ")
                .append(getSplitColor(overallTime, KuudraPhase.END))
                .append(formatElapsedTimeMs(overallTime))
                .append(getLagMessage(KuudraPhase.END.getLag(ticks)));

        if (KuudraSplitsOptions.showMiscInDetailed) {
            message.append("\n\n").append(KIC.KICPrefix)
                    .append(getColorCode(KuudraSplitsOptions.miscColor))
                    .append(" Misc.\n");

            if (freshTimes.isEmpty()) {
                message.append("§cNo fresh times.");
            } else {
                int count = 1;
                for (TimedEvent event : freshTimes) {
                    message.append("§f§l").append(count++).append(". ")
                            .append(event.player).append(" §f| ")
                            .append(getFreshColor(event.timestamp))
                            .append(formatElapsedTimeMs(event.timestamp)).append("\n");
                }
            }
        }

        sendMessageToPlayer(message.toString().trim());
    }

    private static String getLagMessage(long ticks) {
        if (ticks == 0) return "";
        String timePart = KuudraSplitsOptions.showLagInSeconds
                ? formatElapsedTimeMs(ticks * 50)
                : ticks + " ticks";
        return " §r§7[§e" + timePart + "§7]";
    }

    public static void reset() {
        if (splitsOverlayModified) {
            resetKuudraSplitsOverlay();
            splitsOverlayModified = false;
        }
        if (supplyTimesOverlayModified) {
            OverlayManager.getOverlay("SupplyTimes").updateText("");
            supplyTimesOverlayModified = false;
        }
        if (freshTimesOverlayModified) {
            OverlayManager.getOverlay("FreshTimes").updateText("");
            freshTimesOverlayModified = false;
        }

        lastKuudraSplitText = "";
        lastSupplyTimesText = "";
        lastFreshTimesText = "";
    }
}
