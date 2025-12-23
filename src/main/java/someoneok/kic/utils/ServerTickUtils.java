package someoneok.kic.utils;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.events.ServerTickEvent;

public class ServerTickUtils {
    private static long tick = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) { tick++; }

    public static long getServerTime() { return tick * 50; }
    public static long getServerTick() { return tick; }
}
