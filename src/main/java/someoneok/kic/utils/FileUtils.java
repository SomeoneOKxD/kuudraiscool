package someoneok.kic.utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class FileUtils {
    public static void openFolder(File dir) throws IOException {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(dir);
                return;
            } catch (Throwable ignored) {}
        }

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String target = dir.getAbsolutePath();

        ProcessBuilder pb;
        if (os.contains("win")) {
            pb = new ProcessBuilder("explorer.exe", target);
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("open", target);
        } else {
            pb = new ProcessBuilder("xdg-open", target);
        }

        pb.inheritIO();
        pb.start();
    }
}
