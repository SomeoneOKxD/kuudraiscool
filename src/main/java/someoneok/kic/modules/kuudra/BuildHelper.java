package someoneok.kic.modules.kuudra;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.ServerTickEvent;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.models.kuudra.Supply;
import someoneok.kic.models.kuudra.SupplyStatus;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.RenderUtils;
import someoneok.kic.utils.kuudra.KuudraUtils;
import someoneok.kic.utils.overlay.OverlayManager;

import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phaseOrdinal;

public class BuildHelper {
    private static boolean started = false;
    private static boolean modified = false;
    private static int timer = 6150;
    private static int progress = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (!KICConfig.buildTimer || !started || !LocationUtils.inKuudra() || !ApiUtils.isVerified()) return;
        if (phaseOrdinal() > KuudraPhase.BUILD.ordinal()) return;

        if (timer > 0) timer -= 50;

        String timeText = (timer <= 0)
                ? String.format("%s%d%%", getBuildProgressColor(progress), progress)
                : String.format("§c%dms", timer);

        OverlayManager.getOverlay("BuildTimer").updateText("§6Build: " + timeText);
        modified = true;
    }

    private static String getBuildProgressColor(int percent) {
        if (percent >= 85) return "§a";
        if (percent >= 50) return "§e";
        if (percent >= 25) return "§c";
        return "§4";
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!LocationUtils.inKuudra() || !ApiUtils.isVerified()) return;
        if (!KICConfig.supplyWaypointsProgress || phase() != KuudraPhase.BUILD) return;

        for (Supply supply : KuudraUtils.getSupplies()) {
            if (supply.getStatus() != SupplyStatus.COMPLETED) {
                RenderUtils.drawBeaconBeam(
                        supply.getSpot().getLocation(),
                        supply.getRed(),
                        supply.getGreen(),
                        0,
                        1,
                        150,
                        false,
                        event.partialTicks
                );
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        started = false;
        timer = 6150;
        progress = 0;
        if (modified) {
            OverlayManager.getOverlay("BuildTimer").updateText("");
            modified = false;
        }
    }

    public static void start() { started = true; }
    public static boolean isStarted() { return started; }
    public static void setProgress(int newProgress) { progress = newProgress; }
}
