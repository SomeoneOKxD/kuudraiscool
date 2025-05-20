package someoneok.kic.utils.dev;

import net.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import someoneok.kic.config.KICConfig;

import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

public class KICLogger {
    private static final Logger INFO_LOGGER = LogManager.getLogger("KIC.INFO");
    private static final Logger WARN_LOGGER = LogManager.getLogger("KIC.WARN");
    private static final Logger ERROR_LOGGER = LogManager.getLogger("KIC.ERROR");

    public static void info(String message) {
        if (!KICConfig.testerMode) return;
        INFO_LOGGER.info(message);
        if (KICConfig.testerModeLogInChat) sendMessageToPlayer("§a[KIC-INFO] " + message);
    }

    public static void warn(String message) {
        if (!KICConfig.testerMode) return;
        WARN_LOGGER.warn(message);
        if (KICConfig.testerModeLogInChat) sendMessageToPlayer("§e[KIC-WARN] " + message);
    }

    public static void error(String message) {
        if (!KICConfig.testerMode) return;
        ERROR_LOGGER.error(message);
        if (KICConfig.testerModeLogInChat) sendMessageToPlayer("§c[KIC-ERROR] " + message);
    }

    public static boolean shouldLog(Packet<?> packet) {
        return false;
        //return packet instanceof ;
    }
}
