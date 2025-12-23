package someoneok.kic.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import someoneok.kic.events.KICEventBus;
import someoneok.kic.events.PacketEvent;
import someoneok.kic.events.ServerTickEvent;
import someoneok.kic.events.TitleEvent;
import someoneok.kic.utils.dev.KICLogger;

import static someoneok.kic.utils.dev.KICLogger.shouldLog;

@Mixin(value = NetworkManager.class, priority = 1001)
public abstract class MixinNetworkManager extends SimpleChannelInboundHandler<Packet<?>> {
    @Inject(method = "sendPacket*", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        KICEventBus.post(new PacketEvent.Send(packet));

        if (shouldLog(packet)) {
            KICLogger.info("[PacketLogger] Sent Packet: " + packet.getClass().getSimpleName());
        }
    }

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Received event = new PacketEvent.Received(packet);
        KICEventBus.post(event);

        if (shouldLog(packet)) {
            KICLogger.info("[PacketLogger] Received Packet: " + packet.getClass().getSimpleName());
        }

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        if (packet instanceof S32PacketConfirmTransaction) {
            KICEventBus.post(new ServerTickEvent((S32PacketConfirmTransaction) packet));
            return;
        }

        if (packet instanceof S45PacketTitle) {
            S45PacketTitle titlePacket = (S45PacketTitle) packet;
            S45PacketTitle.Type type = titlePacket.getType();
            IChatComponent component = titlePacket.getMessage();

            if (component != null) {
                TitleEvent.Incoming titleEvent = new TitleEvent.Incoming(type, component);
                KICEventBus.post(titleEvent);

                if (titleEvent.isCanceled()) {
                    ci.cancel();
                }
            }
        }
    }
}
