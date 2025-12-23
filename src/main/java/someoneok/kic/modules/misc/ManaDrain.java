package someoneok.kic.modules.misc;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.PartyUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.ChatUtils.sendCommand;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class ManaDrain {
    private static final Pattern MANA_DRAIN = Pattern.compile("^Used Extreme Focus! \\((\\d+) Mana\\)$");

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!KICConfig.announceManaDrain || !PartyUtils.inParty()) return;

        EntityPlayerSP player = mc.thePlayer;
        World world = mc.theWorld;
        if (world == null || player == null) return;

        String raw = removeFormatting(event.message.getUnformattedText());
        Matcher matcher = MANA_DRAIN.matcher(raw);
        if (!matcher.matches()) return;

        String manaUsed = matcher.group(1);

        long nearbyPlayers = world.getPlayers(EntityOtherPlayerMP.class, p ->
                player.getDistanceToEntity(p) <= 5 &&
                        !(p.getTeam() != null && p.getTeam().getNameTagVisibility() == Team.EnumVisible.NEVER)
        ).size();

        sendCommand(String.format("/pc Used %s mana on %d players!", manaUsed, nearbyPlayers));
    }
}
