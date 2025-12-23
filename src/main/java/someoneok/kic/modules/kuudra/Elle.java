package someoneok.kic.modules.kuudra;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.ElleDialogues;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.RenderUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.kuudra.KuudraUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Map;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phaseOrdinal;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class Elle {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!ApiUtils.isVerified()) return;

        String message = event.message.getUnformattedText();
        if (!message.equals("§e[NPC] §cElle§f: Talk with me to begin!") && !LocationUtils.inKuudra()) return;

        String raw = removeFormatting(message);
        if (!raw.startsWith("[NPC] Elle")) return;

        if (KICConfig.elleShutTheFuckUp) {
            event.setCanceled(true);
            return;
        }

        final Map<String, String> map = mapFor(KICConfig.elleMessageStyle);
        if (map == null) return;

        String line = map.get(raw);

        if (line == null) {
            String compact = raw.replace("  ", " ");
            line = map.get(compact);
        }

        if (line != null) {
            event.setCanceled(true);
            sendMessageToPlayer(line);
        } else {
            KICLogger.info("[ELLE-DIALOGUE] Missing line: " + raw);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null
                || mc.theWorld == null
                || !LocationUtils.inKuudra()
                || !ApiUtils.isVerified()) return;

        KuudraPhase currentPhase = phase();
        if (currentPhase == KuudraPhase.END) return;

        if (KICConfig.elleESP && phaseOrdinal() < KuudraPhase.EATEN.ordinal()) {
            EntityArmorStand elle = KuudraUtils.getElle();
            if (elle != null) RenderUtils.drawEntityBox(elle, Color.MAGENTA, 5, event.partialTicks);
        }
    }

    private static @Nullable Map<String, String> mapFor(int idx) {
        switch (idx) {
            case 1: return ElleDialogues.ROADMAN;
            case 2: return ElleDialogues.PIRATE;
            case 3: return ElleDialogues.ZOOMER;
            case 4: return ElleDialogues.EMO;
            case 5: return ElleDialogues.SHAKESPEARE;
            default: return null;
        }
    }
}
