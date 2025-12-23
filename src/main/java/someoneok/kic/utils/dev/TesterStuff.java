package someoneok.kic.utils.dev;

import cc.polyfrost.oneconfig.utils.Multithreading;
import someoneok.kic.KIC;
import someoneok.kic.models.APIException;
import someoneok.kic.utils.NetworkUtils;

import java.util.List;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.*;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;

public class TesterStuff {
    public static boolean testerMode = false;

    public static void sendLogs(List<String> logs) {
        if (!testerMode || !(isTester() || isDev() || isBeta())) return;
        List<String> limitedLogs = logs.size() > 2500 ? logs.subList(0, 2500) : logs;

        String requestBody = KIC.GSON.toJson(limitedLogs);
        Multithreading.runAsync(() -> {
            try {
                NetworkUtils.sendPostRequest(apiHost() + (isBeta() ? "/beta" : "/tester") + "/logs", true, requestBody);
                sendMessageToPlayer(KIC.KICPrefix + " §aSuccessfully send logs to discord.");
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            }
        });
    }
}
