package someoneok.kic.modules.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import someoneok.kic.config.KICConfig;

@SideOnly(Side.CLIENT)
public class PlayerSize {
    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!KICConfig.playerSizeToggle || KICConfig.playerSizingMethod != 0) return;

        EntityPlayer player = event.entityPlayer;

        if (player.getTeam() != null &&
                player.getTeam().getRegisteredName() != null &&
                player.getTeam().getRegisteredName().toLowerCase().startsWith("fkt")) {
            return;
        }

        boolean isNPC = player.getTeam() != null &&
                player.getTeam().getNameTagVisibility() == Team.EnumVisible.NEVER;

        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y, event.z);

        float scale;
        if (isNPC && KICConfig.playerSizeIgnoreNPC) {
            scale = 1;
        } else {
            scale = KICConfig.playerSize;
        }

        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-event.x, -event.y, -event.z);
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (!KICConfig.playerSizeToggle || KICConfig.playerSizingMethod != 0) return;

        EntityPlayer player = event.entityPlayer;

        if (player.getTeam() != null &&
                player.getTeam().getRegisteredName() != null &&
                player.getTeam().getRegisteredName().toLowerCase().startsWith("fkt")) {
            return;
        }

        GlStateManager.popMatrix();
    }
}
