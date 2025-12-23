package someoneok.kic.addons;

import net.minecraft.launchwrapper.Launch;
import someoneok.kic.api.ApiInfo;
import someoneok.kic.api.CoreMixinAddon;
import someoneok.kic.api.ModAddon;
import someoneok.kic.tweaker.AddonMixinsBootstrap;
import someoneok.kic.utils.VersionCompat;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;

public final class AddonLoader {
    private static final String SPI = "META-INF/services/someoneok.kic.api.ModAddon";

    public void loadAll(File dir, File addonDataRoot, boolean reload) {
        dir.mkdirs();

        cleanupOrphanShadows(dir);

        File runtimeDir = new File(dir, "temp");
        runtimeDir.mkdirs();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir.toPath(), "*.jar")) {
            for (Path p : ds) {
                File jar = p.toFile();

                File shadow = new File(runtimeDir, jar.getName() + ".shadow");
                copyFile(jar.toPath(), shadow.toPath());

                URLClassLoader cl = new URLClassLoader(new URL[]{ shadow.toURI().toURL() }, Launch.classLoader);
                List<String> providers = readProviders(shadow, SPI);

                if (providers.isEmpty()) {
                    String mf = readManifestValue(shadow, "Addon-Class");
                    if (mf != null && !mf.trim().isEmpty()) providers = Collections.singletonList(mf.trim());
                }
                if (providers.isEmpty()) { safeClose(cl); continue; }

                for (String fqcn : providers) {
                    try {
                        Class<?> k = Class.forName(fqcn, true, cl);
                        if (!ModAddon.class.isAssignableFrom(k)) continue;

                        ModAddon addon = (ModAddon) k.newInstance();

                        boolean coreMixin = addon instanceof CoreMixinAddon;
                        String addonId = addon.getId();

                        String hostApi = ApiInfo.ADDON_API_VERSION;
                        String requires = String.valueOf(addon.getAddonApiVersion());
                        if (!VersionCompat.isCompatible(hostApi, requires)) {
                            AddonHandle handle = new AddonHandle(addon, cl, jar, shadow, coreMixin, null);
                            AddonRegistry.registerIncompatible(
                                    handle,
                                    "Addon API mismatch",
                                    hostApi,
                                    requires
                            );
                            safeClose(cl);
                            deleteQuiet(shadow.toPath());
                            continue;
                        }

                        String bootstrapVersion = null;
                        if (coreMixin) {
                            if (!AddonMixinsBootstrap.wasMixinAddonPresentAtBootstrap(addonId)) {
                                AddonHandle handle = new AddonHandle(addon, cl, jar, shadow, true, null);
                                AddonRegistry.registerIncompatible(
                                        handle,
                                        "Core mixin addon was not installed at launch. Install it and restart Minecraft.",
                                        hostApi,
                                        requires
                                );
                                safeClose(cl);
                                deleteQuiet(shadow.toPath());
                                continue;
                            }

                            bootstrapVersion = AddonMixinsBootstrap.getBootstrapAddonVersion(addonId);
                            String runtimeVersion = addon.getVersion();

                            if (bootstrapVersion != null && runtimeVersion != null
                                    && !bootstrapVersion.equals(runtimeVersion)) {
                                AddonHandle handle = new AddonHandle(addon, cl, jar, shadow, true, bootstrapVersion);
                                AddonRegistry.registerIncompatible(
                                        handle,
                                        "Core mixin addon version mismatch. Mixins were loaded for " +
                                                bootstrapVersion + " but runtime addon is " + runtimeVersion +
                                                ". Restart Minecraft after updating the addon.",
                                        hostApi,
                                        requires
                                );
                                safeClose(cl);
                                deleteQuiet(shadow.toPath());
                                continue;
                            }
                        }

                        prepareAddonDataLayout(addonDataRoot, addonId);
                        addon.onLoad(new AddonContextImpl(addonId, addonDataRoot));

                        AddonHandle handle = new AddonHandle(addon, cl, jar, shadow, coreMixin, bootstrapVersion);
                        if (AddonRegistry.register(handle)) {
                            handle.safeEnable();
                            if (reload) sendMessageToPlayer(KICPrefix + " §a§l" + handle.id + "§r§a loaded!");
                        } else {
                            safeClose(cl);
                            deleteQuiet(shadow.toPath());
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        safeClose(cl);
                        deleteQuiet(shadow.toPath());
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void prepareAddonDataLayout(File addonDataRoot, String addonId) {
        try {
            File dir = new File(addonDataRoot, addonId);
            if (!dir.isDirectory()) dir.mkdirs();

            File cfg  = new File(dir, "config.json");
            File data = new File(dir, "data.json");
            if (!cfg.exists())  cfg.createNewFile();
            if (!data.exists()) data.createNewFile();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void unload(AddonHandle h) {
        // (Callers should have already disabled via AddonRegistry.disable(id))
        safeClose(h.loader);
        deleteQuiet(h.runtimeJar.toPath());
    }

    private static void copyFile(Path src, Path dst) throws IOException {
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    private static void safeClose(URLClassLoader cl) {
        try { cl.close(); } catch (Throwable ignored) {}
    }

    private static void deleteQuiet(Path p) {
        try { Files.deleteIfExists(p); } catch (Throwable ignored) {}
    }

    private static String readManifestValue(File jar, String key) {
        try (JarFile jf = new JarFile(jar)) {
            Manifest mf = jf.getManifest();
            return mf != null ? mf.getMainAttributes().getValue(key) : null;
        } catch (IOException ignored) { return null; }
    }

    private static List<String> readProviders(File jar, String spiPath) {
        List<String> out = new ArrayList<>();
        try (JarFile jf = new JarFile(jar)) {
            JarEntry e = jf.getJarEntry(spiPath);
            if (e == null) return out;
            try (InputStream in = jf.getInputStream(e);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) out.add(line);
                }
            }
        } catch (IOException ignored) {}
        return out;
    }

    public static void cleanupOrphanShadows(File addonsDir) {
        File runtimeDir = new File(addonsDir, "temp");
        if (!runtimeDir.isDirectory()) return;

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(runtimeDir.toPath(), "*.shadow")) {
            for (Path shadow : ds) {
                try {
                    String name = shadow.getFileName().toString();
                    String originalName = name.substring(0, name.length() - ".shadow".length());
                    File source = new File(addonsDir, originalName);

                    boolean delete = !source.exists();
                    if (!delete) {
                        try {
                            long shadowTs = Files.getLastModifiedTime(shadow).toMillis();
                            long srcTs = source.lastModified();
                            delete = shadowTs < srcTs;
                        } catch (Throwable ignored) {
                            delete = true;
                        }
                    }

                    if (delete) {
                        Files.deleteIfExists(shadow);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}