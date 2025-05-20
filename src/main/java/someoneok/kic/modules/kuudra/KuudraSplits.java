package someoneok.kic.modules.kuudra;

import someoneok.kic.KIC;
import someoneok.kic.config.pages.KuudraSplitsOptions;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.TimedEvent;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.overlay.MovableOverlay;
import someoneok.kic.utils.overlay.OverlayManager;

import java.util.*;

import static someoneok.kic.models.Color.getColorCode;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
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
            KuudraPhase.OVERALL
    };
    private static final KuudraPhase[] reportPhases;

    private static String[] KuudraSplitColors = new String[0];
    private static String PaceColor;
    private static String SupplyTimesColor;
    private static String FreshTimesColor;

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
        colorMapping.put(KuudraPhase.OVERALL,  new long[]{65000, 75000});

        List<KuudraPhase> list = new ArrayList<>(Arrays.asList(SPLIT_PHASES));
        list.remove(KuudraPhase.OVERALL);
        reportPhases = list.toArray(new KuudraPhase[0]);

        updateSplitColors();
    }

    public static void updateSplitColors() {
        KuudraSplitColors = new String[]{
                getColorCode(KuudraSplitsOptions.suppliesColor),
                getColorCode(KuudraSplitsOptions.buildColor),
                getColorCode(KuudraSplitsOptions.eatenColor),
                getColorCode(KuudraSplitsOptions.stunColor),
                getColorCode(KuudraSplitsOptions.dpsColor),
                getColorCode(KuudraSplitsOptions.skipColor),
                getColorCode(KuudraSplitsOptions.killColor),
                getColorCode(KuudraSplitsOptions.overallColor)
        };

        PaceColor = getColorCode(KuudraSplitsOptions.paceColor);
        SupplyTimesColor = getColorCode(KuudraSplitsOptions.supplyTimesColor);
        FreshTimesColor = getColorCode(KuudraSplitsOptions.freshTimesColor);
    }

    public static void updateKuudraSplits(long now) {
        sbSplits.setLength(0);
        long ticks = Kuudra.getTotalLagTimeTicks();

        for (int i = 0; i < SPLIT_PHASES.length; i++) {
            KuudraPhase phase = SPLIT_PHASES[i];

            long time = phase.getTime(now);

            sbSplits.append("§r")
                    .append(KuudraSplitColors[i])
                    .append(phase.getName()).append(": ")
                    .append(getSplitColor(time, phase))
                    .append(formatElapsedTimeMs(time));

            if (KuudraSplitsOptions.showPace) {
                long pace = phase.getPace(now);
                if (pace != 0) {
                    sbSplits.append(" §r§7(§c+").append(formatElapsedTimeMs(pace)).append("§7)");
                }
            }

            if (KuudraSplitsOptions.showLag) {
                long lagTicks = phase.getLag(ticks);
                if (lagTicks != 0) {
                    sbSplits.append(" §r§7[§e");
                    if (KuudraSplitsOptions.showLagInSeconds) {
                        sbSplits.append(formatElapsedTimeMs(lagTicks * 50))
                                .append("§7]");
                    } else {
                        sbSplits.append(lagTicks)
                                .append(" ticks§7]");
                    }
                }
            }

            sbSplits.append("\n");
        }

        if (KuudraSplitsOptions.showEstimatedPace && LocationUtils.kuudraTier == 5) {
            long estimatedPace = KuudraPhase.getEstimatedPace(now);
            sbSplits.append("§r")
                    .append(PaceColor)
                    .append("Pace: ")
                    .append(getSplitColor(estimatedPace, KuudraPhase.OVERALL))
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
        if (timeMs == null || timeMs == 0 || phase == null || !colorMapping.containsKey(phase)) {
            return "§f";
        }

        long[] thresholds = colorMapping.get(phase);
        if (timeMs <= thresholds[0]) {
            return "§a";
        } else if (timeMs <= thresholds[1]) {
            return "§6";
        } else {
            return "§c";
        }
    }

    public static void resetKuudraSplitsOverlay() {
        sbSplits.setLength(0);

        for (int i = 0; i < SPLIT_PHASES.length; i++) {
            KuudraPhase phase = SPLIT_PHASES[i];
            sbSplits.append("§r")
                    .append(KuudraSplitColors[i])
                    .append(phase.getName())
                    .append(": §f0.00s\n");
        }

        sbSplits.append("§r")
                .append(PaceColor)
                .append("Pace: §f0.00s");

        String splits = sbSplits.toString().trim();
        MovableOverlay kuudraSplits = OverlayManager.getOverlay("KuudraSplits");
        kuudraSplits.updateText(splits);
        kuudraSplits.setExampleText(splits);
    }

    public static void updateSupplyTimes(TreeMap<Integer, TimedEvent> supplyTimes) {
        sbSupplyTimes.setLength(0);
        if (supplyTimes.isEmpty()) {
            sbSupplyTimes.append(SupplyTimesColor).append("§lSupply Times\n§7No placed supplies yet...");
        } else {
            int size = supplyTimes.size();

            sbSupplyTimes.append(SupplyTimesColor)
                    .append("§lSupply Times [§r")
                    .append(getRecoveredCountColor(size))
                    .append(size)
                    .append("§r§8/§a6")
                    .append(SupplyTimesColor)
                    .append("§l]\n");

            for (Map.Entry<Integer, TimedEvent> entry : supplyTimes.entrySet()) {
                TimedEvent event = entry.getValue();

                sbSupplyTimes.append(event.player)
                        .append(" §r§8(")
                        .append(entry.getKey())
                        .append("/6) §r")
                        .append(getRecoveredColor(event.timestamp))
                        .append(formatElapsedTimeMs(event.timestamp))
                        .append("\n");
            }
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
            sbFreshTimes.append(FreshTimesColor).append("§lFresh Times\n§7No freshers...");
        } else {
            int size = freshTimes.size();
            sbFreshTimes.append(FreshTimesColor)
                    .append("§lFresh Times [")
                    .append(getFreshCountColor(size))
                    .append(size)
                    .append(FreshTimesColor)
                    .append("§l]\n");

            freshTimes.forEach(event -> sbFreshTimes.append(event.player)
                    .append(" ")
                    .append(getFreshColor(event.timestamp))
                    .append(formatElapsedTimeMs(event.timestamp))
                    .append("\n"));
        }

        String result = sbFreshTimes.toString();
        if (!result.equals(lastFreshTimesText)) {
            lastFreshTimesText = result;
            OverlayManager.getOverlay("FreshTimes").updateText(result.trim());
            freshTimesOverlayModified = true;
        }
    }

    private static String getRecoveredColor(long time) {
        if (time >= 23000 && time < 26000) {
            return "§9§l";
        } else if (time >= 26000 && time < 28000) {
            return "§a§l";
        } else if (time >= 28000 && time < 29500) {
            return "§2§l";
        } else if (time >= 29500 && time < 31000) {
            return "§6§l";
        } else if (time > 31000) {
            return "§c§l";
        }
        return "§f§l";
    }

    private static String getRecoveredCountColor(int size) {
        if (size == 5) {
            return "§2";
        } else if (size == 6) {
            return "§a";
        }
        return "§c";
    }

    private static String getFreshColor(long time) {
        if (time <= 5000) {
            return "§9§l";
        } else if (time <= 7000) {
            return "§a§l";
        } else if (time <= 9000) {
            return "§6§l";
        }
        return "§c§l";
    }

    private static String getFreshCountColor(int size) {
        switch (size) {
            case 3: return "§2";
            case 4: return "§a";
            default: return "§c";
        }
    }

    public static void sendDetailedSplits(long now, List<TimedEvent> freshTimes) {
        StringBuilder message = new StringBuilder(KIC.KICPrefix)
                .append(getColorCode(KuudraSplitsOptions.splitsColor))
                .append(" Splits\n");

        for (KuudraPhase phase : reportPhases) {
            message.append(KuudraSplitsOptions.getColorForPhase(phase))
                    .append(phase.getName())
                    .append(": §f")
                    .append(formatElapsedTimeMs(phase.getTime(now)))
                    .append("\n");
        }

        message.append(getColorCode(KuudraSplitsOptions.splitsColor))
                .append("P3: §f")
                .append(formatElapsedTimeMs(KuudraPhase.getP3(now)))
                .append("\n")
                .append(getColorCode(KuudraSplitsOptions.splitsColor))
                .append("P4: §f")
                .append(formatElapsedTimeMs(KuudraPhase.getP4(now)))
                .append("\n")
                .append(getColorCode(KuudraSplitsOptions.overallColor))
                .append(KuudraPhase.OVERALL.getName())
                .append(": §f")
                .append(formatElapsedTimeMs(KuudraPhase.OVERALL.getTime(now)));

        if (KuudraSplitsOptions.showMiscInDetailed) {
            message.append("\n").append(KIC.KICPrefix)
                    .append(getColorCode(KuudraSplitsOptions.miscColor))
                    .append(" Misc.\n");

            if (freshTimes.isEmpty()) {
                message.append("§cNo fresh times.");
            } else {
                int count = 1;
                for (TimedEvent event : freshTimes) {
                    message.append("§f")
                            .append(count++)
                            .append(".")
                            .append(event.player)
                            .append("§f| §a")
                            .append(formatElapsedTimeMs(event.timestamp))
                            .append("\n");
                }
            }
        }

        sendMessageToPlayer(message.toString().trim());
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
