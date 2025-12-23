package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraSplitsOptions;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.ws.KICWS;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.modules.kuudra.Kuudra.*;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class KuudraPhaseTracker {
    private static KuudraPhase currentPhase = KuudraPhase.NONE;

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!LocationUtils.inKuudra() || !ApiUtils.isVerified()) return;

        String raw = removeFormatting(event.message.getUnformattedText());
        long now = System.currentTimeMillis();
        long ticks = getTotalLagTimeTicks();

        switch (raw) {
            case "[NPC] Elle: Talk with me to begin!":
                KICLogger.info("Phase 0 (Ready)");
                currentPhase = KuudraPhase.NONE;
                return;

            case "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!":
                KICLogger.info("Phase 1 (Supplies)");
                currentPhase = KuudraPhase.SUPPLIES;
                KuudraPhase.SUPPLIES.begin(now, ticks);
                KuudraPhase.END.start(now, ticks);
                return;

            case "[NPC] Elle: OMG! Great work collecting my supplies!":
                KICLogger.info("Phase 2 (Build)");
                currentPhase = KuudraPhase.BUILD;
                KuudraPhase.BUILD.begin(now, ticks);
                return;

            case "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!":
                if (currentPhase != KuudraPhase.DPS) {
                    KICLogger.info("Phase 3 (Eaten)");
                    currentPhase = KuudraPhase.EATEN;
                    KuudraPhase.EATEN.begin(now, ticks);
                }
                return;

            case "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!":
                KICLogger.info("Phase 6 (Skip)");
                currentPhase = KuudraPhase.SKIP;
                KuudraPhase.SKIP.begin(now, ticks);
                return;
        }

        if (raw.contains("has been eaten by Kuudra!") && currentPhase == KuudraPhase.EATEN && !raw.contains("Elle")) {
            KICLogger.info("Phase 4 (Stun)");
            currentPhase = KuudraPhase.STUN;
            KuudraPhase.STUN.begin(now, ticks);
            return;
        }

        if (raw.contains("destroyed one of Kuudra's pods!") && currentPhase != KuudraPhase.DPS) {
            KICLogger.info("Phase 5 (Dps)");
            currentPhase = KuudraPhase.DPS;
            KuudraPhase.DPS.begin(now, ticks);
            return;
        }

        String trimmedUpper = raw.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        if (trimmedUpper.startsWith("KUUDRADOWN") || trimmedUpper.startsWith("DEFEAT")) {
            currentPhase = KuudraPhase.END;
            KuudraPhase.KILL.end(now, ticks);
            KuudraPhase.END.end(now, ticks);
            KuudraPhase.endMissedPhases(now);
            boolean failed = trimmedUpper.contains("DEFEAT");

            KICLogger.info("Phase 8 (End), failed? " + failed);

            long runTime = KuudraPhase.END.getTime(System.currentTimeMillis());
            KuudraProfitTracker.onRunEnded(runTime, failed);

            KICWS.sendLag(LocationUtils.kuudraTier(), getTotalLagTimeTicks(), runTime);
            if (KuudraSplitsOptions.showTotalServerLag) Multithreading.schedule(() -> sendMessageToPlayer(String.format("%s §cServer lagged for §f%.2fs §7(§f%d ticks§7)", KICPrefix, getTotalLagTimeS(), getTotalLagTimeTicks())), 500, TimeUnit.MILLISECONDS);
            if (KuudraSplitsOptions.showDetailedOverview) Multithreading.schedule(() -> KuudraSplits.sendDetailedSplits(now, ticks, getFreshTimes()), 525, TimeUnit.MILLISECONDS);
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) { currentPhase = KuudraPhase.NONE; }

    public static void updatePhaseToKill() {
        long now = System.currentTimeMillis();
        long ticks = getTotalLagTimeTicks();
        KICLogger.info("Phase 7 (Kill)");
        currentPhase = KuudraPhase.KILL;
        KuudraPhase.KILL.begin(now, ticks);
    }

    public static KuudraPhase phase() { return KICConfig.forceKuudraPhase != 0 ? KuudraPhase.fromOrdinal(KICConfig.forceKuudraPhase) : currentPhase; }
    public static int phaseOrdinal() { return KICConfig.forceKuudraPhase != 0 ? KICConfig.forceKuudraPhase : currentPhase.ordinal(); }
}
