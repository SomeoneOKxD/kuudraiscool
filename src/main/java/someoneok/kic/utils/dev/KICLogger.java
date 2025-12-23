package someoneok.kic.utils.dev;

import net.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KICLogger {
    private static final Logger INFO_LOGGER = LogManager.getLogger("KIC.INFO");
    private static final Logger WARN_LOGGER = LogManager.getLogger("KIC.WARN");
    private static final Logger ERROR_LOGGER = LogManager.getLogger("KIC.ERROR");

    public static void info(String message) {
        if (!TesterStuff.testerMode) return;
        INFO_LOGGER.info(message);
    }

    public static void warn(String message) {
        if (!TesterStuff.testerMode) return;
        WARN_LOGGER.warn(message);
    }

    public static void error(String message) {
        if (!TesterStuff.testerMode) return;
        ERROR_LOGGER.error(message);
    }

    public static void forceInfo(String message) {
        INFO_LOGGER.info(message);
    }

    public static void forceWarn(String message) {
        WARN_LOGGER.warn(message);
    }

    public static void forceError(String message) {
        ERROR_LOGGER.error(message);
    }

    public static boolean shouldLog(Packet<?> packet) {
        return false;
        //return packet instanceof ;
    }
}
