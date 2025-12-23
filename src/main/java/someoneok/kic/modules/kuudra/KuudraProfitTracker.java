package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraProfitTrackerOptions;
import someoneok.kic.models.APIException;
import someoneok.kic.models.Color;
import someoneok.kic.models.data.ProfitTrackerData;
import someoneok.kic.models.kuudra.chest.KuudraChest;
import someoneok.kic.models.kuudra.chest.KuudraKey;
import someoneok.kic.models.request.ShareTrackerRequest;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.data.DataHandler;
import someoneok.kic.utils.data.DataManager;
import someoneok.kic.utils.overlay.DualOverlay;
import someoneok.kic.utils.overlay.OverlayManager;
import someoneok.kic.utils.overlay.OverlaySegment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjLongConsumer;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.apiHost;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.*;

public class KuudraProfitTracker {
    private static boolean showConfirm = false;
    private static boolean lifetimeView = false;

    public static void changeView() {
        lifetimeView = !lifetimeView;
        showConfirm = false;
        updateTracker();
    }

    public static void onRunEnded(long runTimeMs, boolean failed) {
        ProfitTrackerData data = DataManager.getProfitTrackerData();
        data.addRun();

        boolean usedAverage = runTimeMs <= 0;
        long runTimeSec;

        if (usedAverage) runTimeSec = data.getLifetime().getAverageRunTime();
        else runTimeSec = runTimeMs / 1000L;

        if (!usedAverage && KICConfig.kuudraProfitTrackerAddRunTimeDelay) {
            runTimeSec += KICConfig.kuudraProfitTrackerTotalTimeToAdd;
        }

        data.addTime(runTimeSec);

        if (failed) data.addFailedRun();
        else if (!usedAverage) maybeUpdatePersonalBest(runTimeMs);

        int totalTap = TapTracker.getTotalUsedTAP();
        int totalTwap = TapTracker.getTotalUsedTWAP();

        if (totalTap > 0) data.addTap(totalTap);
        if (totalTwap > 0) data.addTwap(totalTwap);

        DataHandler.saveData();
        updateTracker();
    }

    private static void maybeUpdatePersonalBest(long runTimeMs) {
        if (LocationUtils.kuudraTier() != 5 || runTimeMs < 5000) return;
        Long oldPb = KIC.userData.getKuudraPersonalBest();
        if (oldPb == null) oldPb = 0L;

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

    public static void onChestReroll() {
        ProfitTrackerData data = DataManager.getProfitTrackerData();
        data.addReroll();
        DataHandler.saveData();
        updateTracker();
    }

    public static void onShardReroll() {
        ProfitTrackerData data = DataManager.getProfitTrackerData();
        data.addShardReroll();
        DataHandler.saveData();
        updateTracker();
    }

    public static void onChestBought(KuudraChest chest) {
        ProfitTrackerData data = DataManager.getProfitTrackerData();

        data.addProfit(chest.getRawTotalValue());

        int essence = chest.getEssence();
        if (essence > 0) data.addEssence(essence);

        int teeth = chest.getTeeth();
        if (teeth > 0) data.addTeeth(teeth);

        if (chest.hasValuable()) data.addValuable(chest.getValuable());

        KuudraKey key = chest.getKeyNeeded();
        if (key == null) {
            data.addFreeChest();
        } else {
            switch (key) {
                case BASIC: data.addBasicChest(); break;
                case HOT: data.addHotChest(); break;
                case BURNING: data.addBurningChest(); break;
                case FIERY: data.addFieryChest(); break;
                case INFERNAL: data.addInfernalChest(); break;
                default: data.addFreeChest(); break;
            }
        }

        DataHandler.saveData();
        updateTracker();
    }

    public static void updateKeyPrice(KuudraKey key) {
        if (key == null) return;
        ProfitTrackerData data = DataManager.getProfitTrackerData();

        long price = key.getPrice();
        if (price == 0) return;
        switch (key) {
            case BASIC: data.setBasicKeyPrice(price); break;
            case HOT: data.setHotKeyPrice(price); break;
            case BURNING: data.setBurningKeyPrice(price); break;
            case FIERY: data.setFieryKeyPrice(price); break;
            case INFERNAL: data.setInfernalKeyPrice(price); break;
        }

        DataHandler.saveData();
        updateTracker();
    }

    private static void updatePrice(long price, ObjLongConsumer<ProfitTrackerData> setter) {
        if (price <= 0) return;
        ProfitTrackerData data = DataManager.getProfitTrackerData();
        setter.accept(data, price);
        DataHandler.saveData();
        updateTracker();
    }

    public static void updateKismetPrice(long price)  { updatePrice(price, ProfitTrackerData::setKismetPrice); }
    public static void updateWofPrice(long price)     { updatePrice(price, ProfitTrackerData::setWofPrice); }
    public static void updateEssencePrice(long price) { updatePrice(price, ProfitTrackerData::setEssencePrice); }
    public static void updateTeethPrice(long price)   { updatePrice(price, ProfitTrackerData::setTeethPrice); }
    public static void updateTapPrice(long price)     { updatePrice(price, ProfitTrackerData::setTapPrice); }
    public static void updateTwapPrice(long price)    { updatePrice(price, ProfitTrackerData::setTwapPrice); }

    public static void updateTracker() {
        StringBuilder text = new StringBuilder(String.format("§7[%s§lKIC§7] %s§lProfit Tracker",
                Color.getColorCode(KuudraProfitTrackerOptions.kicColor),
                Color.getColorCode(KuudraProfitTrackerOptions.profitTrackerColor)));

        if (lifetimeView) {
            text.append(" §r§7[")
                    .append(Color.getColorCode(KuudraProfitTrackerOptions.sessionColor))
                    .append("§lLIFETIME§r§7]");
        } else if (KuudraProfitTrackerOptions.showCurrentSession) {
            text.append(" §r§7[")
                    .append(Color.getColorCode(KuudraProfitTrackerOptions.sessionColor))
                    .append("§lCURRENT§r§7]");
        }

        text.append("\n");

        ProfitTrackerData data = DataManager.getProfitTrackerData();
        ProfitTrackerData.ProfitTrackerSession session = lifetimeView ? data.getLifetime() : data.getCurrent();

        if (KuudraProfitTrackerOptions.showProfit) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.profitColor);
            text.append("\n").append(color).append("§lProfit: §r")
                    .append(color).append(parseToShorthandNumber(session.getProfit(data)));
        }

        if (KuudraProfitTrackerOptions.showRuns) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.runsColor);
            text.append("\n").append(color).append("§lRuns: §r")
                    .append(color).append(parseToShorthandNumber(session.getRuns())).append(" runs");
        }

        if (KuudraProfitTrackerOptions.showChests) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.chestsColor);
            text.append("\n").append(color).append("§lChests: §r")
                    .append(color).append(parseToShorthandNumber(session.getChests())).append(" chests");
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
                    .append(color).append(parseToShorthandNumber(session.getRerolls()))
                    .append("§7/")
                    .append(color).append(parseToShorthandNumber(session.getShardRerolls()));
            if (KuudraProfitTrackerOptions.showRerollCosts) {
                text.append(String.format(" §7(§c-%s§7)", parseToShorthandNumber(session.getRerollCost(data))));
            }
        }

        if (KuudraProfitTrackerOptions.showTime) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.timeColor);
            text.append("\n").append(color).append("§lTime: §r")
                    .append(color).append(formatElapsedTime(session.getTime(), 0, 3));
        }

        if (KuudraProfitTrackerOptions.showAverageTimePerRun) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.averageTimePerRunColor);
            text.append("\n").append(color).append("§lAverage: §r")
                    .append(color).append(formatElapsedTime(session.getAverageRunTime(), 0, 3)).append("/run");
        }

        if (KuudraProfitTrackerOptions.showRate) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.rateColor);
            text.append("\n").append(color).append("§lRate: §r")
                    .append(color).append(parseToShorthandNumber(session.getHourlyRate())).append("/hr");
        }

        if (KuudraProfitTrackerOptions.showValuables) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.valuableColor);
            text.append("\n").append(color).append("§lValuables: §r")
                    .append(color).append(parseToShorthandNumber(session.getTotalValuables()));
            if (KuudraProfitTrackerOptions.showValuablesValue) {
                text.append(String.format(" §7(§a+%s§7)", parseToShorthandNumber(session.getValuablesValue())));
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

        if (KuudraProfitTrackerOptions.showTap) {
            String color = Color.getColorCode(KuudraProfitTrackerOptions.tapColor);
            text.append("\n").append(color).append("§lTAP & TWAP: §r")
                    .append(color).append(parseToShorthandNumber(session.getTap()))
                    .append("§7/")
                    .append(color).append(parseToShorthandNumber(session.getTwap()));
            if (KuudraProfitTrackerOptions.showTapCosts) {
                text.append(String.format(" §7(§c-%s§7)", parseToShorthandNumber(session.getTapCost(data))));
            }
        }

        List<OverlaySegment> segments = new ArrayList<>();
        DualOverlay overlay = (DualOverlay) OverlayManager.getOverlay("ProfitTracker");

        segments.add(new OverlaySegment(text.toString()));
        segments.add(new OverlaySegment("\n§7-=-=-=-=-=-=-=-=-=-=-"));
        segments.add(new OverlaySegment(
                "\n§7[§aChange Session§7]",
                KuudraProfitTracker::changeView,
                true
        ));
        segments.add(new OverlaySegment(
                "\n§7[§aNew Session§7]",
                () -> newSession(false),
                true
        ));
        if (showConfirm) {
            segments.add(new OverlaySegment("\n§7§oAre you sure? "));
            segments.add(new OverlaySegment(
                    "§7[§2YES§7]",
                    () -> newSession(true),
                    true
            ));
            segments.add(new OverlaySegment(" "));
            segments.add(new OverlaySegment(
                    "§7[§4NO§7]",
                    () -> newSession(false),
                    true
            ));
        }

        overlay.setExampleText(text.toString());
        overlay.setBaseText(text.toString());
        overlay.setInteractiveSegments(segments);
    }

    public static void resetTracker() {
        DataManager.getProfitTrackerData().reset();
        DataHandler.saveData();
        updateTracker();
        sendMessageToPlayer(KIC.KICPrefix + " §aKuudra profit tracker has been reset.");
    }

    public static void newSession(boolean confirmed) {
        if (showConfirm) {
            showConfirm = false;
            if (confirmed) {
                DataManager.getProfitTrackerData().newSession();
                DataHandler.saveData();
                updateTracker();
            }
            updateTracker();
        } else {
            showConfirm = true;
            updateTracker();
        }
    }

    public static void shareTracker() {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        Multithreading.runAsync(() -> {
            ShareTrackerRequest shareTrackerRequest = new ShareTrackerRequest(lifetimeView);
            String requestBody = KIC.GSON.toJson(shareTrackerRequest);
            try {
                NetworkUtils.sendPostRequest(apiHost() + "/crimson/share-tracker", true, requestBody);
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
}
