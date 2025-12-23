package someoneok.kic.addons;

import someoneok.kic.api.ModAddon;
import someoneok.kic.utils.StringUtils;
import someoneok.kic.utils.data.DataHandler;
import someoneok.kic.utils.dev.KICLogger;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import static someoneok.kic.KIC.*;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;

public class AddonHelpers {
    public static File ADDON_DATA_DIR = new File(new File(mc.mcDataDir, "config/kuudraiscool"), "addons-data");
    public static final File ADDON_DIR = new File(mc.mcDataDir, "config/kuudraiscool/addons");
    private static final AddonLoader addonLoader = new AddonLoader();
    private static final long RELOAD_COOLDOWN_MS = 7000L;
    private static final Object RELOAD_LOCK = new Object();
    private static volatile long lastReloadAtMs = 0L;
    private static volatile boolean reloading = false;

    public static void initAddons(boolean reload) {
        try {
            addonLoader.loadAll(ADDON_DIR, ADDON_DATA_DIR, reload);
        } catch (Throwable t) {
            KICLogger.forceError("Failed to load addons: " + t.getMessage());
        }

        hydrateAddonOverlays();
        hydrateAddonConfigPages();
        AddonRegistry.logLoadedAddons();
        if (config != null) config.rebuildAddonsIndex();
    }

    public static void reloadAddons() {
        long now = System.currentTimeMillis();
        long left;

        synchronized (RELOAD_LOCK) {
            if (reloading) {
                sendMessageToPlayer(KICPrefix + " §eAdd-ons reload is already in progress.");
                return;
            }
            left = (lastReloadAtMs + RELOAD_COOLDOWN_MS) - now;
            if (left > 0) {
                sendMessageToPlayer(KICPrefix + " §eReload is on cooldown. Please wait §f" + StringUtils.formatSecs(left) + "s§e.");
                return;
            }
            reloading = true;
            lastReloadAtMs = now;
        }

        try {
            sendMessageToPlayer(KICPrefix + " §7Reloading add-ons...");
            persistAllAddonOverlays();
            persistAndDetachAddonConfigs();
            ArrayList<AddonHandle> handlesSnapshot = new ArrayList<>(AddonRegistry.handles());
            for (AddonHandle h : handlesSnapshot) {
                if (h.isCoreMixinAddon()) continue;
                h.safeDisable();
                AddonLoader.unload(h);
            }
            ArrayList<String> idsSnapshot = new ArrayList<>(AddonRegistry.ids());
            for (String id : idsSnapshot) {
                AddonHandle h = AddonRegistry.get(id);
                if (h != null && h.isCoreMixinAddon()) continue;
                AddonRegistry.remove(id);
            }
            initAddons(true);
            if (config != null) config.rebuildAddonsIndex();
            sendMessageToPlayer(KICPrefix + " §aReload complete.");
        } finally {
            reloading = false;
        }
    }

    public static boolean unloadAddon(String id) {
        AddonHandle h = AddonRegistry.get(id);
        if (h == null) return false;

        if (h.isCoreMixinAddon()) {
            sendMessageToPlayer(KICPrefix + " §cYou cannot unload core mixin addons while the game is running.");
            return false;
        }

        persistAddonOverlays(id);
        persistAndDetachAddonConfig(id);
        AddonRegistry.disable(id);
        AddonLoader.unload(h);
        AddonRegistry.remove(id);
        if (config != null) config.rebuildAddonsIndex();
        return true;
    }

    public static boolean deleteAddon(String id) {
        AddonHandle h = AddonRegistry.get(id);
        if (h == null) return false;

        if (h.isCoreMixinAddon()) {
            sendMessageToPlayer(KICPrefix + " §cYou cannot delete core mixin addons while the game is running. " +
                    "Remove the JAR from disk and restart Minecraft.");
            return false;
        }

        persistAddonOverlays(id);
        persistAndDetachAddonConfig(id);
        AddonRegistry.disable(id);
        AddonLoader.unload(h);
        AddonRegistry.remove(id);
        try { Files.deleteIfExists(h.sourceJar.toPath()); } catch (Throwable ignored) {}
        if (config != null) config.rebuildAddonsIndex();
        return true;
    }

    private static void persistAndDetachAddonConfigs() {
        for (AddonHandle h : new ArrayList<>(AddonRegistry.handles())) {
            persistAndDetachAddonConfig(h.id);
        }
    }

    private static void persistAndDetachAddonConfig(String id) {
        AddonHandle h = AddonRegistry.get(id);
        if (h == null) return;

        Class<?> pageKlass = h.addon.getConfigPageClass();
        if (pageKlass != null) {
            try {
                AddonConfigIO.saveStaticPage(h.addon.getConfigPersistenceId(), pageKlass);
            } catch (Throwable ignored) {}
        }
    }

    private static void persistAddonOverlays(String id) {
        try {
            DataHandler.saveOverlaysForAddon(id);
        } catch (Throwable ignored) {}
    }

    private static void persistAllAddonOverlays() {
        for (AddonHandle h : new ArrayList<>(AddonRegistry.handles())) {
            persistAddonOverlays(h.id);
        }
    }

    private static void hydrateAddonConfigPages() {
        for (ModAddon addon : AddonRegistry.addons()) {
            Class<?> pageKlass = addon.getConfigPageClass();
            if (pageKlass == null) continue;

            try {
                AddonConfigIO.loadStaticPage(addon.getConfigPersistenceId(), pageKlass);
            } catch (Throwable t) {
                KICLogger.warn("Could not load static page for " + addon.getId() + ": " + t.getMessage());
            }
        }
    }

    private static void hydrateAddonOverlays() {
        for (ModAddon addon : AddonRegistry.addons()) {
            try {
                DataHandler.loadOverlaysForAddon(addon.getConfigPersistenceId());
            } catch (Throwable t) {
                KICLogger.warn("Could not load overlays for " + addon.getId() + ": " + t.getMessage());
            }
        }
    }

    public static File configFileForAddon(String id) {
        return new File(ADDON_DATA_DIR, id + "/config.json");
    }

    public static File overlayFileForAddon(String id) {
        return new File(ADDON_DATA_DIR, id + "/overlay.json");
    }
}
