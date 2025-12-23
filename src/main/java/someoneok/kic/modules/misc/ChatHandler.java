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

import static someoneok.kic.utils.ChatUtils.sendChatMessage;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.EmojiUtils.replaceEmojis;

@ChannelHandler.Sharable
public class ChatHandler extends ChannelOutboundHandlerAdapter {
    public static ChatMode currentChatMode = ChatMode.MC;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof C01PacketChatMessage && ApiUtils.isVerified()) {
            String message = ((C01PacketChatMessage) msg).getMessage();

            if (shouldProcess(message)) {
                switch (currentChatMode) {
                    case KICCHAT:
                        if (shouldSend(message)) KICWS.sendChatMessage(message, false);
                        return;

                    case KICPLUSCHAT:
                        if (ApiUtils.hasPremium()) {
                            if (shouldSend(message)) KICWS.sendChatMessage(message, true);
                            return;
                        } else {
                            currentChatMode = ChatMode.KICCHAT;
                            sendMessageToPlayer(KIC.KICPrefix + " §cKIC+ chat requires premium. Switching to KIC chat.");
                            break;
                        }

                    case MC:
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
        }

        ctx.write(msg, promise);
    }

    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        pipeline.addBefore("packet_handler", this.getClass().getName(), this);
    }

    private boolean shouldProcess(String message) {
        String cmd = message.trim().toLowerCase();
        if (cmd.startsWith("/")) {
            if (cmd.startsWith("/chat")) {
                if (cmd.equalsIgnoreCase("/chat kc") || cmd.equalsIgnoreCase("/chat kic")) {
                    currentChatMode = ChatMode.KICCHAT;
                    sendMessageToPlayer(KIC.KICPrefix + " §aSwitched to §bKIC Chat§a mode.");
                    return true;
                } else if (cmd.equalsIgnoreCase("/chat kcp") || cmd.equalsIgnoreCase("/chat kic+")) {
                    if (ApiUtils.hasPremium()) {
                        currentChatMode = ChatMode.KICPLUSCHAT;
                        sendMessageToPlayer(KIC.KICPlusPrefix + " §aSwitched to §dKIC+ Chat§a mode.");
                    } else {
                        currentChatMode = ChatMode.KICCHAT;
                        sendMessageToPlayer(KIC.KICPrefix + " §cKIC+ chat requires premium. Switching to KIC chat.");
                    }
                    return true;
                } else {
                    currentChatMode = ChatMode.MC;
                }
            }
            return false;
        }

        return true;
    }

    private boolean shouldSend(String message) {
        String cmd = message.trim().toLowerCase();
        return !cmd.equalsIgnoreCase("/chat kc")
                && !cmd.equalsIgnoreCase("/chat kic")
                && !cmd.equalsIgnoreCase("/chat kcp")
                && !cmd.equalsIgnoreCase("/chat kic+");
    }
}
