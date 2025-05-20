package someoneok.kic.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import someoneok.kic.models.NEUCompatibility;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.miscfeatures.StorageManager", remap = false)
public abstract class MixinStorageManagerNEU {
    @Dynamic
    @Shadow
    private boolean shouldRenderStorageOverlayCached;

    @Dynamic
    @Inject(method = "shouldRenderStorageOverlay", at = @At("RETURN"))
    private void updateStorageSet(String containerName, CallbackInfoReturnable<Boolean> cir) {
        NEUCompatibility.setStorageMenuActive(shouldRenderStorageOverlayCached);
    }
}
