package someoneok.kic.modules.misc;

import io.netty.channel.*;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.misc.ChatMode;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.ws.KICWS;

import static someoneok.kic.utils.EmojiUtils.replaceEmojis;
import static someoneok.kic.utils.GeneralUtils.sendChatMessage;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

@ChannelHandler.Sharable
public class ChatHandler extends ChannelOutboundHandlerAdapter {
    public static ChatMode currentChatMode = ChatMode.HYPIXEL;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof C01PacketChatMessage) {
            String message = ((C01PacketChatMessage) msg).getMessage();

            switch (currentChatMode) {
                case KICCHAT:
                    KICWS.sendChatMessage(message, false);
                    return;

                case KICPLUSCHAT:
                    if (ApiUtils.hasPremium()) {
                        KICWS.sendChatMessage(message, true);
                        return;
                    } else {
                        currentChatMode = ChatMode.HYPIXEL;
                        sendMessageToPlayer(KIC.KICPrefix + " §cKIC+ chat requires premium. Switching to Hypixel chat.");
                        break;
                    }

                case HYPIXEL:
                default:
                    if (KICConfig.emojis) {
                        String replaced = replaceEmojis(message);
                        if (!replaced.equals(message)) {
                            sendChatMessage(replaced);
                            return;
                        }
                    }
                    break;
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
