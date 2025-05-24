package someoneok.kic.mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.scoreboard.Team;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.ApiUtils;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {
    @Inject(method = "preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V", at = @At("HEAD"))
    public void onPreRenderCallback(AbstractClientPlayer entity, float partialTickTime, CallbackInfo ci) {
        if (!KICConfig.playerSizeToggle || KICConfig.playerSizingMethod != 1 || !ApiUtils.isVerified()) return;

        Team team = entity.getTeam();
        if (team != null) {
            String teamName = team.getRegisteredName();
            if (teamName != null && teamName.toLowerCase().startsWith("fkt")) return;
            if (KICConfig.playerSizeIgnoreNPC && team.getNameTagVisibility() == Team.EnumVisible.NEVER) return;
        }

        GL11.glScaled(KICConfig.playerSize, KICConfig.playerSize, KICConfig.playerSize);
    }
}
