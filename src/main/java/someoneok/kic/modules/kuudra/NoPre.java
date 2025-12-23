package someoneok.kic.modules.kuudra;

import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.PickupSpot;
import someoneok.kic.models.kuudra.Supply;
import someoneok.kic.models.kuudra.SupplySpot;
import someoneok.kic.models.kuudra.SupplyStatus;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.PartyUtils;
import someoneok.kic.utils.kuudra.KuudraUtils;

import java.util.HashMap;
import java.util.Map;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.ChatUtils.sendCommand;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class NoPre {
    public static final String NO_EQUALS = "No Equals!";
    public static final String NO_SHOP = "No Shop!";
    public static final String NO_X_CANNON = "No X Cannon!";
    public static final String NO_SLASH = "No Slash!";
    public static final String NO_TRIANGLE = "No Triangle!";
    public static final String NO_X = "No X!";
    public static final String NO_SQUARE = "No Square!";

    private static PickupSpot missing = PickupSpot.NONE;
    private static SupplySpot noPreSpot;
    private PickupSpot preSpot = PickupSpot.NONE;

    private static final Map<String, PickupSpot> PHRASE_TO_SPOT = new HashMap<>();
    static {
        PHRASE_TO_SPOT.put(NO_EQUALS,   PickupSpot.EQUALS);
        PHRASE_TO_SPOT.put(NO_SHOP,     PickupSpot.SHOP);
        PHRASE_TO_SPOT.put(NO_X_CANNON, PickupSpot.X_CANNON);
        PHRASE_TO_SPOT.put(NO_SLASH,    PickupSpot.SLASH);
        PHRASE_TO_SPOT.put(NO_TRIANGLE, PickupSpot.TRIANGLE);
        PHRASE_TO_SPOT.put(NO_X,        PickupSpot.X);
        PHRASE_TO_SPOT.put(NO_SQUARE,   PickupSpot.SQUARE);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!LocationUtils.inKuudra() || !ApiUtils.isVerified()) return;

        String raw = removeFormatting(event.message.getUnformattedText());

        if (raw.equals("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!")) {
            if (mc.thePlayer == null) return;
            Vec3 playerLocation = mc.thePlayer.getPositionVector();

            if (PickupSpot.TRIANGLE.getLocation().distanceTo(playerLocation) < 15) {
                preSpot = PickupSpot.TRIANGLE;
            } else if (PickupSpot.X.getLocation().distanceTo(playerLocation) < 30) {
                preSpot = PickupSpot.X;
            } else if (PickupSpot.EQUALS.getLocation().distanceTo(playerLocation) < 15) {
                preSpot = PickupSpot.EQUALS;
            } else if (PickupSpot.SLASH.getLocation().distanceTo(playerLocation) < 15) {
                preSpot = PickupSpot.SLASH;
            } else {
                preSpot = PickupSpot.NONE;
            }

            if (KICConfig.noPre) sendMessageToPlayer(KICPrefix + (preSpot == PickupSpot.NONE ? " §cDidn't register your pre-spot because you didn't get there in time." : " §aPre-spot: " + preSpot.getDisplayText()));
            return;
        }

        if (raw.equals("[NPC] Elle: Not again!")) {
            if (!KICConfig.noPre || preSpot == PickupSpot.NONE) return;

            boolean pre = false;
            boolean second = false;
            String alert = "";

            for (Vec3 supply : KuudraUtils.getCrates()) {
                double dPre = preSpot.getLocation().distanceTo(supply);
                if (dPre < 18) pre = true;

                if (preSpot == PickupSpot.TRIANGLE) {
                    double dShop = PickupSpot.SHOP.getLocation().distanceTo(supply);
                    if (dShop < 18) second = true;
                } else if (preSpot == PickupSpot.X) {
                    double dXCannon = PickupSpot.X_CANNON.getLocation().distanceTo(supply);
                    if (dXCannon < 16) second = true;
                } else if (preSpot == PickupSpot.SLASH) {
                    double dSquare = PickupSpot.SQUARE.getLocation().distanceTo(supply);
                    if (dSquare < 20) second = true;
                }
            }

            if (second && pre) return;

            if (!pre && preSpot != PickupSpot.NONE) {
                alert = "No " + preSpot.getDisplayText() + "!";
            } else if (!second) {
                switch (preSpot) {
                    case TRIANGLE: alert = NO_SHOP; break;
                    case X: alert = NO_X_CANNON; break;
                    case SLASH: alert = NO_SQUARE; break;
                    default: return;
                }
            }

            if (!alert.isEmpty() && PartyUtils.inParty()) sendCommand("/pc " + alert);
            return;
        }

        for (Map.Entry<String, PickupSpot> e : PHRASE_TO_SPOT.entrySet()) {
            if (raw.contains(e.getKey())) {
                missing = e.getValue();
                assignMissing(missing);
                break;
            }
        }
    }

    private void assignMissing(PickupSpot missing) {
        if (missing == null) return;

        int supplyId = missing.getSupplyId();
        if (supplyId == -1) return;

        int index = supplyId - 1;
        Supply[] supplies = KuudraUtils.getSupplies();

        if (index >= 0 && index < supplies.length) {
            Supply supply = supplies[index];
            if (supply.getStatus() == SupplyStatus.NOTHING) noPreSpot = supply.getSpot();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        preSpot = PickupSpot.NONE;
        missing = PickupSpot.NONE;
    }

    public static PickupSpot getMissing() { return missing; }
    public static SupplySpot getNoPreSpot() { return noPreSpot; }
}
