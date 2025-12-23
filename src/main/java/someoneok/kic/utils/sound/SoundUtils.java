package someoneok.kic.utils.sound;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import someoneok.kic.mixin.AccessorSoundHandler;

import java.util.*;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;

public class SoundUtils {
    private static final Map<String, ResourceLocation> SOUND_CACHE = new HashMap<>();
    private static List<String> ALL_SOUNDS_CACHE = null;

    public static SoundRegistry getSoundRegistry() {
        SoundHandler handler = mc.getSoundHandler();
        return ((AccessorSoundHandler) handler).getSndRegistry();
    }

    public static List<String> getAllSounds() {
        if (ALL_SOUNDS_CACHE != null) return ALL_SOUNDS_CACHE;
        SoundRegistry registry = getSoundRegistry();
        List<String> list = new ArrayList<>();
        for (ResourceLocation rl : registry.getKeys()) list.add(stripNamespace(rl.toString()));
        ALL_SOUNDS_CACHE = Collections.unmodifiableList(list);
        return ALL_SOUNDS_CACHE;
    }

    public static List<String> searchSounds(String query) {
        if (isNullOrEmpty(query)) return Collections.emptyList();
        String lower = query.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String name : getAllSounds()) if (name.toLowerCase(Locale.ROOT).contains(lower)) matches.add(name);
        return matches;
    }

    public static boolean validSound(String soundName) {
        if (isNullOrEmpty(soundName)) return false;
        SoundHandler handler = mc.getSoundHandler();
        if (handler == null) return false;
        ResourceLocation rl = rl(soundName);
        return handler.getSound(rl) != null;
    }

    private static ResourceLocation rl(String s) {
        return SOUND_CACHE.computeIfAbsent(stripNamespace(s.trim()), ResourceLocation::new);
    }

    public static void playSound(String sound, float volume, float pitch, boolean distanceDelay) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        playSound(player.posX, player.posY, player.posZ, sound, volume, pitch, distanceDelay);
    }

    public static void playSound(double x, double y, double z, String sound, float volume, float pitch, boolean distanceDelay) {
        WorldClient world = mc.theWorld;
        if (world == null) return;
        try {
            world.playSound(x, y, z, sound, volume, pitch, distanceDelay);
        } catch (Exception ignored) {}
    }

    public static void clearCache() {
        ALL_SOUNDS_CACHE = null;
        SOUND_CACHE.clear();
    }

    private static String stripNamespace(String s) {
        int idx = s.indexOf(':');
        return (idx >= 0 ? s.substring(idx + 1) : s);
    }
}
