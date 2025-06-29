package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;

import java.util.HashSet;
import java.util.Set;

public class KuudraRerollFailsafeOptions {
    @Info(
            text = "Auto Reroll Failsafes",
            type = InfoType.INFO,
            size = 2
    )
    public static boolean ignored;

    @Info(
            text = "Ananke Feather, Wheel Of Fate, Burning Kuudra Core, Tormentor, Hellstorm Wand, Tentacle Dye, Fatal Tempo and Inferno\nare never rerolled by default!",
            type = InfoType.INFO,
            size = 2
    )
    public static boolean ignored2;

    @Switch(
            name = "Bezal Shard"
    )
    public static boolean bezal = false;

    @Switch(
            name = "Magma Slug Shard"
    )
    public static boolean magmaSlug = false;

    @Switch(
            name = "Kada Knight Shard"
    )
    public static boolean kadaKnight = false;

    @Switch(
            name = "Wither Spectre Shard"
    )
    public static boolean witherSpectre = false;

    @Switch(
            name = "Matcho Shard"
    )
    public static boolean matcho = false;

    @Switch(
            name = "Lava Flame Shard"
    )
    public static boolean lavaFlame = false;

    @Switch(
            name = "Fire Eel Shard"
    )
    public static boolean fireEel = false;

    @Switch(
            name = "Flare Shard"
    )
    public static boolean flare = false;

    @Switch(
            name = "Barbarian Duke X Shard"
    )
    public static boolean barbarianDukeX = false;

    @Switch(
            name = "Hellwisp Shard"
    )
    public static boolean hellwisp = false;

    @Switch(
            name = "XYZ Shard"
    )
    public static boolean xyz = false;

    @Switch(
            name = "Taurus Shard"
    )
    public static boolean taurus = false;

    @Switch(
            name = "Lord Jawbus Shard"
    )
    public static boolean lordJawbus = false;

    @Switch(
            name = "Cinderbat Shard"
    )
    public static boolean cinderbat = false;

    @Switch(
            name = "Daemon Shard"
    )
    public static boolean daemon = false;

    @Switch(
            name = "Moltenfish Shard"
    )
    public static boolean moltenfish = false;

    @Switch(
            name = "Ananke Shard"
    )
    public static boolean ananke = false;

    public static Set<String> getEnabled() {
        Set<String> set = new HashSet<>();

        if (bezal) set.add("SHARD_BEZAL");
        if (magmaSlug) set.add("SHARD_MAGMA_SLUG");
        if (kadaKnight) set.add("SHARD_KADA_KNIGHT");
        if (witherSpectre) set.add("SHARD_WITHER_SPECTRE");
        if (matcho) set.add("SHARD_MATCHO");
        if (lavaFlame) set.add("SHARD_LAVA_FLAME");
        if (fireEel) set.add("SHARD_FIRE_EEL");
        if (flare) set.add("SHARD_FLARE");
        if (barbarianDukeX) set.add("SHARD_BARBARIAN_DUKE_X");
        if (hellwisp) set.add("SHARD_HELLWISP");
        if (xyz) set.add("SHARD_XYZ");
        if (taurus) set.add("SHARD_TAURUS");
        if (lordJawbus) set.add("SHARD_LORD_JAWBUS");
        if (cinderbat) set.add("SHARD_CINDERBAT");
        if (daemon) set.add("SHARD_DAEMON");
        if (moltenfish) set.add("SHARD_MOLTENFISH");
        if (ananke) set.add("SHARD_ANANKE");

        return set;
    }
}
