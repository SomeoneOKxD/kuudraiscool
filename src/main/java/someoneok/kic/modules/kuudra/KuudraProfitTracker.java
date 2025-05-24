package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraProfitTrackerOptions;
import someoneok.kic.models.APIException;
import someoneok.kic.models.Color;
import someoneok.kic.models.kuudra.KuudraChest;
import someoneok.kic.models.kuudra.KuudraKey;
import someoneok.kic.models.overlay.ProfitTrackerData;
import someoneok.kic.models.request.ShareTrackerRequest;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.overlay.*;

import java.util.ArrayList;
import java.util.List;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.*;
import someoneok.kic.utils.LocationUtils;

public class KuudraProfitTracker {
    private static final int MAX_TIER = 5;
    private static boolean showConfirm = false;
    private static boolean lifetimeView = false;
    private static int selectedTier = 0;

    public static void changeView() {
        lifetimeView = !lifetimeView;
        showConfirm = false;
        updateTracker();
    }

    public static void onRunEnded(long runTimeMs, boolean failed) {
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();
        int tier = LocationUtils.kuudraTier;

        data.addRun(tier);

        long runTime = runTimeMs / 1000;
        if (KICConfig.kuudraProfitTrackerAddRunTimeDelay) {
            runTime += KICConfig.kuudraProfitTrackerTotalTimeToAdd;
        }

        data.addTime(runTime, tier);

        if (failed) {
            data.addFailedRun(tier);
        } else {
            maybeUpdatePersonalBest(runTimeMs);
        }

        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    private static void maybeUpdatePersonalBest(long runTimeMs) {
        long oldPb = KIC.userData.getKuudraPersonalBest();

        if (oldPb == 0 || runTimeMs < oldPb) {
            KIC.userData.setKuudraPersonalBest(runTimeMs);

            String msg = String.format(
                    "%s §aNew Personal Best! §f%s §7→ §a%s",
                    KICPrefix,
                    formatElapsedTimeMs(oldPb),
                    formatElapsedTimeMs(runTimeMs)
            );

            if (oldPb != 0) sendMessageToPlayer(msg);
        }
    }

    public static void onRunEnded(boolean failed) {
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();
        int tier = LocationUtils.kuudraTier;

        data.addRun(tier);
        long averageTime = data.getLifetime().getAverageRunTime();

        if (KICConfig.kuudraProfitTrackerAddRunTimeDelay) {
            averageTime += KICConfig.kuudraProfitTrackerTotalTimeToAdd;
        }

        data.addTime(averageTime, tier);

        if (failed) data.addFailedRun(tier);

        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    public static void onReroll() {
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();
        int tier = LocationUtils.kuudraTier;
        data.addReroll(tier);
        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    public static void onChestBought(KuudraChest chest, boolean rerolled) {
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();
        int tier = LocationUtils.kuudraTier;

        long totalProfit = chest.getTotalValue(rerolled);
        data.addProfit(totalProfit, tier);

        int essence = chest.getEssence();
        if (essence > 0) data.addEssence(essence, tier);

        int teeth = chest.getTeeth();
        if (teeth > 0) data.addTeeth(teeth, tier);

        if (chest.hasGodRoll()) data.addGodRoll(chest.getGodRoll(), tier);

        KuudraKey key = chest.getKeyNeeded();
        if (key == null) {
            data.addFreeChest(tier);
        } else {
            data.addPaidChest(tier);
            switch (key) {
                case BASIC: data.addBasicChest(tier); break;
                case HOT: data.addHotChest(tier); break;
                case BURNING: data.addBurningChest(tier); break;
                case FIERY: data.addFieryChest(tier); break;
                case INFERNAL: data.addInfernalChest(tier); break;
            }
        }

        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    public static void updateKeyPrice(KuudraKey key) {
        if (key == null) return;
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();

        long price = key.getPrice();
        if (price == 0) return;
        switch (key) {
            case BASIC:
                data.setBasicKeyPrice(price);
                break;
            case HOT:
                data.setHotKeyPrice(price);
                break;
            case BURNING:
                data.setBurningKeyPrice(price);
                break;
            case FIERY:
                data.setFieryKeyPrice(price);
                break;
            case INFERNAL:
                data.setInfernalKeyPrice(price);
                break;
        }

        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    public static void updateKismetPrice(long price) {
        if (price == 0) return;
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();

        data.setKismetPrice(price);

        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    public static void updateEssencePrice(long price) {
        if (price == 0) return;
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();

        data.setEssencePrice(price);

        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    public static void updateTeethPrice(long price) {
        if (price == 0) return;
        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();

        data.setTeethPrice(price);

        OverlayDataHandler.saveOverlaysData();
        updateTracker();
    }

    public static void updateTracker() {
        StringBuilder text = new StringBuilder(String.format("§7[%s§lKIC§7] %s§lProfit Tracker",
                Color.getColorCode(KuudraProfitTrackerOptions.kicColor),
                Color.getColorCode(KuudraProfitTrackerOptions.profitTrackerColor)));

        if (lifetimeView) {
            text.append(" §r§7[")
                    .append(Color.getColorCode(KuudraProfitTrackerOptions.sessionColor))
                    .append("§lLIFETIME");
            if (selectedTier != 0) {
                text.append(" (T").append(selectedTier).append(")");
            }
            text.append("§r§7]");
        } else if (KuudraProfitTrackerOptions.showCurrentSession) {
            text.append(" §r§7[")
                    .append(Color.getColorCode(KuudraProfitTrackerOptions.sessionColor))
                    .append("§lCURRENT");
            if (selectedTier != 0) {
                text.append(" (T").append(selectedTier).append(")");
            }
            text.append("§r§7]");
        }

        text.append("\n");

        ProfitTrackerData data = OverlayDataManager.getProfitTrackerData();
        ProfitTrackerData.ProfitTrackerSession session;
        if (lifetimeView) {
            if (selectedTier != 0) {
                session = data.getLifetimeTierSessions().get(selectedTier);
                if (session == null) session = data.getLifetime(); // fallback
            } else {
                session = data.getLifetime();
            }
        } else {
            if (selectedTier != 0) {
                session = data.getTierSessions().get(selectedTier);
                if (session == null) session = data.getCurrent(); // fallback
            } else {
                session = data.getCurrent();
            }
        }
        if (KuudraProfitTrackerOptions.showProfit) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.profitColor);
            text.append("\n").append(color).append("§lProfit: §r")
                    .append(color).append(parseToShorthandNumber(session.getProfit()));
        }

        if (KuudraProfitTrackerOptions.showRuns) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.runsColor);
            text.append("\n").append(color).append("§lRuns: §r")
                    .append(color).append(session.getRuns()).append(" runs");
        }

        if (KuudraProfitTrackerOptions.showChests) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.chestsColor);
            text.append("\n").append(color).append("§lChests: §r")
                    .append(color).append(session.getChests()).append(" chests");
            if (KuudraProfitTrackerOptions.showKeyCosts) {
                text.append(String.format(" §7(§c-%s§7)", parseToShorthandNumber(session.getChestsCost(data))));
            }
        }

        if (KuudraProfitTrackerOptions.showAverageProfitPerChest) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.averagePerChestColor);
            text.append("\n").append(color).append("§lAverage: §r")
                    .append(color).append(parseToShorthandNumber(session.getAverage())).append("/chest");
        }

        if (KuudraProfitTrackerOptions.showRerolls) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.rerollsColor);
            text.append("\n").append(color).append("§lRerolls: §r")
                    .append(color).append(session.getRerolls());
            if (KuudraProfitTrackerOptions.showRerollCosts) {
                text.append(String.format(" §7(§c-%s§7)", parseToShorthandNumber(session.getRerollCost(data))));
            }
        }

        if (KuudraProfitTrackerOptions.showTime) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.timeColor);
            text.append("\n").append(color).append("§lTime: §r")
                    .append(color).append(formatElapsedTime(session.getTime(), 0, 4));
        }

        if (KuudraProfitTrackerOptions.showAverageTimePerRun) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.averageTimePerRunColor);
            text.append("\n").append(color).append("§lAverage: §r")
                    .append(color).append(formatElapsedTime(session.getAverageRunTime(), 0, 4)).append("/run");
        }

        if (KuudraProfitTrackerOptions.showRate) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.rateColor);
            text.append("\n").append(color).append("§lRate: §r")
                    .append(color).append(parseToShorthandNumber(session.getHourlyRate())).append("/hr");
        }

        if (KuudraProfitTrackerOptions.showGodRolls) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.godRollColor);
            text.append("\n").append(color).append("§lGod Rolls: §r")
                    .append(color).append(parseToShorthandNumber(session.getTotalGodRolls()));
            if (KuudraProfitTrackerOptions.showGodRollValue) {
                text.append(String.format(" §7(§a+%s§7)", parseToShorthandNumber(session.getGodRollValue())));
            }
        }

        if (KuudraProfitTrackerOptions.showEssence) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.essenceColor);
            text.append("\n").append(color).append("§lEssence: §r")
                    .append(color).append(parseToShorthandNumber(session.getEssence()));
            if (KuudraProfitTrackerOptions.showEssenceValue) {
                text.append(String.format(" §7(§a+%s§7)", parseToShorthandNumber(session.getTotalEssenceValue(data))));
            }
        }

        List<OverlaySegment> segments = new ArrayList<>();
        DualOverlay overlay = (DualOverlay) OverlayManager.getOverlay("ProfitTracker");

        segments.add(new OverlaySegment(text.toString()));
        segments.add(new OverlaySegment("\n§7-=-=-=-=-=-=-=-=-=-=-"));
        segments.add(new OverlaySegment("\n§7[§aChange Session§7]", KuudraProfitTracker::changeView, true));
        segments.add(new OverlaySegment("\n§7[§aNew Session§7]", () -> newSession(false), true));
        segments.add(new OverlaySegment("\n§7[§aSwitch Tier§7]", KuudraProfitTracker::cycleTier, true));

        if (showConfirm) {
            segments.add(new OverlaySegment("\n§7§oAre you sure? "));
            segments.add(new OverlaySegment("§7[§2YES§7]", () -> newSession(true), true));
            segments.add(new OverlaySegment(" "));
            segments.add(new OverlaySegment("§7[§4NO§7]", () -> newSession(false), true));
        }

        overlay.setExampleText(text.toString());
        overlay.setBaseText(text.toString());
        overlay.setInteractiveSegments(segments);
    }

    public static void resetTracker() {
        OverlayDataManager.getProfitTrackerData().reset();
        OverlayDataHandler.saveOverlaysData();
        updateTracker();
        sendMessageToPlayer(KIC.KICPrefix + " §aKuudra profit tracker has been reset.");
    }

    public static void newSession(boolean confirmed) {
        if (showConfirm) {
            showConfirm = false;
            if (confirmed) {
                if (lifetimeView) {
                    OverlayDataManager.getProfitTrackerData().getLifetime().reset();
                    OverlayDataManager.getProfitTrackerData().getLifetimeTierSessions().values().forEach(ProfitTrackerData.ProfitTrackerSession::reset);
                } else {
                    OverlayDataManager.getProfitTrackerData().newSession();
                }
                OverlayDataHandler.saveOverlaysData();
                updateTracker();
            }
            updateTracker();
        } else {
            showConfirm = true;
            updateTracker();
        }
    }

    public static void shareTracker() {
        Multithreading.runAsync(() -> {
            ShareTrackerRequest shareTrackerRequest = new ShareTrackerRequest(lifetimeView);
            String requestBody = KIC.GSON.toJson(shareTrackerRequest);
            try {
                NetworkUtils.sendPostRequest("https://api.sm0kez.com/crimson/share-tracker", true, requestBody);
            } catch (APIException e) {
                if (e.getStatus() == 429) {
                    sendMessageToPlayer(String.format("%s §cYou can only use this command every 10 minutes!", KICPrefix));
                } else {
                    sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                }
                return;
            }
            sendMessageToPlayer(String.format("%s §aShared your profit tracker in the KIC discord!", KICPrefix));
        });
    }

    private static void cycleTier() {
        selectedTier = (selectedTier + 1) % (MAX_TIER + 1);
        updateTracker();
    }
}
