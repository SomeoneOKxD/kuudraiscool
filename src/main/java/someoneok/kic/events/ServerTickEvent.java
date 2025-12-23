package someoneok.kic.events;

import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ServerTickEvent extends Event {
    private final S32PacketConfirmTransaction packet;

    public ServerTickEvent(S32PacketConfirmTransaction packet) {
        this.packet = packet;
    }

    public S32PacketConfirmTransaction getPacket() {
        return packet;
    }
}
