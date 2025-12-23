package someoneok.kic.modules.kuudra;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.RenderUtils;

import java.awt.*;

import static someoneok.kic.KIC.mc;

public class TeamHighlight {
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!KICConfig.teamHighlight || !ApiUtils.isVerified() || !LocationUtils.inKuudra() || mc.theWorld == null) return;

        Color highlightColor = KICConfig.teamHighlightColor.toJavaColor();
        float partialTicks = event.partialTicks;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (!(player instanceof EntityOtherPlayerMP)) continue;
            if (player.getTeam() != null && player.getTeam().getNameTagVisibility() == Team.EnumVisible.NEVER) continue;

            IChatComponent displayName = player.getDisplayName();
            if (displayName == null) continue;

            RenderUtils.drawEntityBox(player, highlightColor, 4, partialTicks);
            RenderUtils.renderNameTag(displayName.getFormattedText(), player.posX, player.posY + 2.5, player.posZ, 1, false);
        }
    }
}
