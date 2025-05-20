package someoneok.kic.utils;

import net.hypixel.data.region.Environment;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket;
import net.minecraftforge.common.MinecraftForge;
import someoneok.kic.events.HypixelJoinEvent;

public class HypixelEventApi {
    private static final HypixelModAPI modAPI = HypixelModAPI.getInstance();

    public HypixelEventApi() {
        modAPI.subscribeToEventPacket(ClientboundLocationPacket.class);
        modAPI.createHandler(ClientboundLocationPacket.class, LocationUtils::handleLocationPacket);
        modAPI.createHandler(ClientboundHelloPacket.class, this::handleHelloPacket);
        modAPI.createHandler(ClientboundPartyInfoPacket.class, PartyUtils::onPartyInfo);
    }

    private void handleHelloPacket(ClientboundHelloPacket packet) {
        LocationUtils.onHypixel = true;
        boolean isAlpha = packet.getEnvironment() != Environment.PRODUCTION;
        MinecraftForge.EVENT_BUS.post(new HypixelJoinEvent(isAlpha));
    }

    public static void sendPartyPacket() {
        modAPI.sendPacket(new ServerboundPartyInfoPacket());
    }
}
