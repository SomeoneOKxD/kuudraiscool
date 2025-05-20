package someoneok.kic.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import someoneok.kic.events.PacketEvent;
import someoneok.kic.events.TitleEvent;
import someoneok.kic.utils.dev.KICLogger;

import static someoneok.kic.utils.dev.KICLogger.shouldLog;

@Mixin(value = NetworkManager.class, priority = 1001)
public abstract class MixinNetworkManager extends SimpleChannelInboundHandler<Packet<?>> {
    @Inject(method = "sendPacket*", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Send(packet));

        if (shouldLog(packet)) {
            KICLogger.info("[PacketLogger] Sent Packet: " + packet.getClass().getSimpleName());
        }
    }

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Received(packet));

        if (shouldLog(packet)) {
            KICLogger.info("[PacketLogger] Received Packet: " + packet.getClass().getSimpleName());
        }

        if (packet instanceof S45PacketTitle) {
            S45PacketTitle titlePacket = (S45PacketTitle) packet;
            S45PacketTitle.Type type = titlePacket.getType();
            IChatComponent component = titlePacket.getMessage();

            if (component != null) {
                TitleEvent.Incoming event = new TitleEvent.Incoming(type, component);
                MinecraftForge.EVENT_BUS.post(event);

                if (event.isCanceled()) {
                    ci.cancel();
                }
            }
        }
    }
}
