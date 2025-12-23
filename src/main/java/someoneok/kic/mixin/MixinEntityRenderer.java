package someoneok.kic.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import someoneok.kic.events.hooks.MouseSensitivityHook;

@Mixin(value = EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @ModifyVariable(
            method = "updateCameraAndRender",
            at = @At("STORE"),
            ordinal = 1
    )
    private float modifySensitivity(float value) {
        return MouseSensitivityHook.INSTANCE.remapSensitivity(value);
    }
}
