package someoneok.kic.modules.kuudra;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.utils.LocationUtils;

import java.util.Arrays;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.modules.kuudra.KuudraPhaseTracker.phase;
import static someoneok.kic.utils.ItemUtils.getItemId;

public class TapTracker {
    private static final int MAX_PER_TICK = 4;
    private static final String TAP_ID = "TOXIC_ARROW_POISON";
    private static final String TWAP_ID = "TWILIGHT_ARROW_POISON";
    private static final int INV_LEN = 36;

    private static final int NO_BASELINE = Integer.MIN_VALUE;

    private static final byte TYPE_NONE = 0;
    private static final byte TYPE_TAP  = 1;
    private static final byte TYPE_TWAP = 2;

    private static int totalUsedTAP = 0;
    private static int totalUsedTWAP = 0;

    private int lastTotalTAP = NO_BASELINE;
    private int lastTotalTWAP = NO_BASELINE;

    private int curTAP = 0;
    private int curTWAP = 0;

    private boolean cachesDirty = false;

    private final ItemStack[] lastStacks = new ItemStack[INV_LEN];
    private final int[] lastSizes = new int[INV_LEN];
    private final byte[] targetType = new byte[INV_LEN];

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || mc.thePlayer == null) return;

        if (!LocationUtils.inKuudra()) {
            lastTotalTAP = NO_BASELINE;
            lastTotalTWAP = NO_BASELINE;
            return;
        }

        KuudraPhase currentPhase = phase();
        if (currentPhase == KuudraPhase.NONE || currentPhase == KuudraPhase.END) return;

        fastCountPerId();

        if (lastTotalTAP == NO_BASELINE || lastTotalTWAP == NO_BASELINE) {
            lastTotalTAP = curTAP;
            lastTotalTWAP = curTWAP;
            return;
        }

        int deltaTAP  = Math.max(0, lastTotalTAP  - curTAP);
        int deltaTWAP = Math.max(0, lastTotalTWAP - curTWAP);

        if (deltaTAP > 0 && deltaTAP <= MAX_PER_TICK)   totalUsedTAP  += deltaTAP;
        if (deltaTWAP > 0 && deltaTWAP <= MAX_PER_TICK) totalUsedTWAP += deltaTWAP;

        lastTotalTAP = curTAP;
        lastTotalTWAP = curTWAP;
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        totalUsedTAP = 0;
        totalUsedTWAP = 0;
        lastTotalTAP = NO_BASELINE;
        lastTotalTWAP = NO_BASELINE;

        if (cachesDirty) {
            Arrays.fill(lastStacks, null);
            Arrays.fill(lastSizes, 0);
            Arrays.fill(targetType, (byte) 0);
            cachesDirty = false;
        }
    }

    private void fastCountPerId() {
        int sumTAP = 0, sumTWAP = 0;
        ItemStack[] inv = mc.thePlayer.inventory.mainInventory;
        int len = Math.min(inv.length, INV_LEN);

        for (int i = 0; i < len; i++) {
            ItemStack cur = inv[i];

            if (cur == lastStacks[i] && (cur == null || cur.stackSize == lastSizes[i])) {
                if (cur != null) {
                    if (targetType[i] == TYPE_TAP) sumTAP  += cur.stackSize;
                    else if (targetType[i] == TYPE_TWAP) sumTWAP += cur.stackSize;
                }
                continue;
            }

            byte type = TYPE_NONE;
            int size = 0;

            if (cur != null) {
                size = cur.stackSize;
                String id = getItemId(cur);
                if (TAP_ID.equals(id)) type = TYPE_TAP;
                else if (TWAP_ID.equals(id)) type = TYPE_TWAP;
            }

            lastStacks[i] = cur;
            lastSizes[i] = size;
            targetType[i] = type;
            cachesDirty = true;

            if (type == TYPE_TAP) sumTAP += size;
            else if (type == TYPE_TWAP) sumTWAP += size;
        }

        curTAP = sumTAP;
        curTWAP = sumTWAP;
    }

    public static int getTotalUsedTAP()  { return totalUsedTAP; }
    public static int getTotalUsedTWAP() { return totalUsedTWAP; }
}
