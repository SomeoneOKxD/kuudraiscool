package someoneok.kic.utils.mouse;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.models.misc.SensitivityState;

public class LockMouseLook {
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        unlockMouse();
    }

    public static void unlockMouse() {
        if (!SensitivityState.LOCKED.isActive()) return;
        MouseSensitivityManager.setState(SensitivityState.UNCHANGED);
    }

    public static void lockMouse() {
        if (SensitivityState.LOCKED.isActive()) return;
        MouseSensitivityManager.setState(SensitivityState.LOCKED);
    }

    public static void toggleLock() {
        if (SensitivityState.LOCKED.isActive()) {
            unlockMouse();
        } else {
            lockMouse();
        }
    }

    public static boolean isLocked() {
        return SensitivityState.LOCKED.isActive();
    }
}
