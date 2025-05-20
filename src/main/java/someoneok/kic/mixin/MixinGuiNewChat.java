package someoneok.kic.mixin;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(value = GuiNewChat.class)
public abstract class MixinGuiNewChat extends Gui {

    @Unique
    private static final ChatLine kic$placeholderLine = new ChatLine(0, new ChatComponentText("placeholder"), 0);

    @Inject(method = "deleteChatLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ChatLine;getChatLineID()I"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void stopDeleteCrash(int id, CallbackInfo ci, Iterator<ChatLine> iterator, ChatLine chatline) {
        if (chatline == null || kic$placeholderLine == chatline) {
            iterator.remove();
        }
    }

    @ModifyVariable(method = "deleteChatLine", at = @At("STORE"))
    private ChatLine stopDeleteCrash(ChatLine chatLine) {
        return chatLine == null ? kic$placeholderLine : chatLine;
    }
}
