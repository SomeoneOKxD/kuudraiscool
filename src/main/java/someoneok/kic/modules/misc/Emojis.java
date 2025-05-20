package someoneok.kic.modules.misc;

import io.netty.channel.*;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import someoneok.kic.config.KICConfig;

import static someoneok.kic.utils.EmojiUtils.replaceEmojis;
import static someoneok.kic.utils.GeneralUtils.sendChatMessage;

@ChannelHandler.Sharable
public class Emojis extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof C01PacketChatMessage) {
            if (KICConfig.emojis) {
                String message = ((C01PacketChatMessage) msg).getMessage();
                String replacedMessage = replaceEmojis(message);

                if (!replacedMessage.equals(message)) {
                    sendChatMessage(replacedMessage);
                    return;
                }
            }
        }
        ctx.write(msg, promise);
    }

    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        pipeline.addBefore("packet_handler", this.getClass().getName(), this);
    }
}
