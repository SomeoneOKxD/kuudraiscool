package someoneok.kic.modules.kuudra;

import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.ServerTickEvent;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.kuudra.KuudraUtils;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.formatElapsedTimeMs;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;

public class RendDamage {
    private int kuudraLastHp = 24999;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (!KICConfig.rendDamage || !LocationUtils.inKuudra() || !ApiUtils.isVerified()) return;
        if (phase() != KuudraPhase.KILL || mc.thePlayer.posY > 30) return;

        EntityMagmaCube boss = KuudraUtils.getKuudra();
        if (boss == null) return;

        int kuudraHp = (int) boss.getHealth();
        if (kuudraHp > 25_000) return;

        int diff = kuudraLastHp - kuudraHp;
        if (diff > 1666) {
            sendMessageToPlayer(String.format(
                    "%s §fSomeone pulled for %s%s §fdamage at §a%s§f.",
                    KICPrefix,
                    getDamageColor(diff),
                    parseToShorthandNumber(diff * 9600),
                    formatElapsedTimeMs(KuudraPhase.KILL.getTime(System.currentTimeMillis()))
            ));
        }

        kuudraLastHp = kuudraHp;
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        kuudraLastHp = 24999;
    }

    private String getDamageColor(int dmg) {
        if (dmg >= 1666 && dmg <= 4166) return "§c";
        if (dmg >= 4166 && dmg <= 7291) return "§e";
        if (dmg > 7291) return "§a";
        return "§f";
    }
}
