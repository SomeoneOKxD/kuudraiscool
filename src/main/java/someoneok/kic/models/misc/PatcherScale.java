package someoneok.kic.models.misc;

import net.minecraftforge.fml.common.Loader;
import someoneok.kic.utils.ReflectionUtil;

import java.lang.invoke.MethodHandle;

public class PatcherScale {
    public static int getInvScale() {
        if (!Loader.isModLoaded("patcher")) return -1;
        int inventoryScale = -1;

        try {
            MethodHandle inventoryScaleHandle = ReflectionUtil.getField(Class.forName("club.sk1er.patcher.config.PatcherConfig"), "inventoryScale");
            inventoryScale = (int) inventoryScaleHandle.invoke();
        } catch (Throwable ignored) {}

        return inventoryScale;
    }
}
