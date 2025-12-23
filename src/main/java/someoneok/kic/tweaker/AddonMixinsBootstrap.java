package someoneok.kic.tweaker;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AddonMixinsBootstrap {
    private static final String MF_ADDON_ID     = "Addon-Id";
    private static final String MF_MIXIN_CONFIG = "Addon-MixinConfigs"; // comma-separated
    private static final String MF_ADDON_VERSION  = "Addon-Version";

    private static final Map<String, String> BOOTSTRAP_MIXIN_ADDONS =
            new LinkedHashMap<>();

    public static void run(Path addonDir) {
        try { MixinBootstrap.init(); } catch (Throwable ignored) {}

        List<Path> jars = listAddonJars(addonDir);
        LaunchClassLoader lcl = Launch.classLoader;

        for (Path jar : jars) {
            try (JarFile jf = new JarFile(jar.toFile())) {
                Manifest mf = jf.getManifest();
                if (mf == null) continue;

                Attributes at = mf.getMainAttributes();
                String cfgCsv = value(at, MF_MIXIN_CONFIG);
                if (cfgCsv == null || cfgCsv.trim().isEmpty()) continue;

                URL url = jar.toUri().toURL();
                lcl.addURL(url);

                String addonId = Optional.ofNullable(value(at, MF_ADDON_ID))
                        .orElse(jar.getFileName().toString());
                String version = at.getValue(MF_ADDON_VERSION);

                List<String> configs = new ArrayList<>();
                for (String piece : cfgCsv.split(",")) {
                    String cfg = piece.trim();
                    if (cfg.isEmpty()) continue;
                    Mixins.addConfiguration(cfg);
                    configs.add(cfg);
                }
                BOOTSTRAP_MIXIN_ADDONS.put(addonId, version);
                System.out.println("[KIC-AddonMixins] Registered " + configs + " from " + addonId);
            } catch (Throwable t) {
                System.err.println("[KIC-AddonMixins] Failed processing " + jar + ": " + t);
            }
        }
    }

    private static List<Path> listAddonJars(Path dir) {
        List<Path> out = new ArrayList<>();
        try {
            Files.createDirectories(dir);
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.jar")) {
                for (Path p : ds) out.add(p.toAbsolutePath().normalize());
            }
        } catch (IOException e) {
            System.err.println("[KIC-AddonMixins] Could not scan addon dir: " + dir + " : " + e);
        }
        return out;
    }

    public static boolean wasMixinAddonPresentAtBootstrap(String addonId) {
        return BOOTSTRAP_MIXIN_ADDONS.containsKey(addonId);
    }

    public static String getBootstrapAddonVersion(String addonId) {
        return BOOTSTRAP_MIXIN_ADDONS.get(addonId);
    }

    private static String value(Attributes at, String key) { return at.getValue(key); }
    private AddonMixinsBootstrap() {}
}
