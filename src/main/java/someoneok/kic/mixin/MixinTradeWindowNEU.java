package someoneok.kic.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import someoneok.kic.models.NEUCompatibility;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.miscgui.TradeWindow", remap = false)
public class MixinTradeWindowNEU {
    @Dynamic
    @Inject(method = "tradeWindowActive", at = @At("RETURN"))
    private static void tradeWindowActive(String containerName, CallbackInfoReturnable<Boolean> cir) {
        NEUCompatibility.setTradeWindowActive(cir.getReturnValue());
    }
}
