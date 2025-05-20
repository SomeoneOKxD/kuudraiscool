package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;

import java.util.HashSet;
import java.util.Set;

public class KuudraIgnoredShardAttributeOptions {
    @Info(
            text = "Ignored Attribute on Shards",
            type = InfoType.INFO,
            size = 2
    )
    public static boolean ignored;

    @Info(
            text = "Every enabled attribute will be counted as 0 value.",
            type = InfoType.INFO,
            size = 2
    )
    public static boolean ignored2;

    @Switch(
            name = "Arachno"
    )
    public static boolean arachno = true;

    @Switch(
            name = "Attack Speed"
    )
    public static boolean attackSpeed = true;

    @Switch(
            name = "Blazing"
    )
    public static boolean blazing = true;

    @Switch(
            name = "Combo"
    )
    public static boolean combo = true;

    @Switch(
            name = "Elite"
    )
    public static boolean elite = true;

    @Switch(
            name = "Ender"
    )
    public static boolean ender = true;

    @Switch(
            name = "Ignition"
    )
    public static boolean ignition = true;

    @Switch(
            name = "Life Recovery"
    )
    public static boolean lifeRecovery = true;

    @Switch(
            name = "Mana Steal"
    )
    public static boolean manaSteal = true;

    @Switch(
            name = "Midas Touch"
    )
    public static boolean midasTouch = true;

    @Switch(
            name = "Undead"
    )
    public static boolean undead = true;

    @Switch(
            name = "Warrior"
    )
    public static boolean warrior = true;

    @Switch(
            name = "Deadeye"
    )
    public static boolean deadeye = true;

    @Switch(
            name = "Arachno Resistance"
    )
    public static boolean arachnoResistance = true;

    @Switch(
            name = "Blazing Resistance"
    )
    public static boolean blazingResistance = true;

    @Switch(
            name = "Breeze"
    )
    public static boolean breeze = false;

    @Switch(
            name = "Dominance"
    )
    public static boolean dominance = false;

    @Switch(
            name = "Ender Resistance"
    )
    public static boolean enderResistance = true;

    @Switch(
            name = "Experience"
    )
    public static boolean experience = true;

    @Switch(
            name = "Fortitude"
    )
    public static boolean fortitude = true;

    @Switch(
            name = "Life Regeneration"
    )
    public static boolean lifeRegeneration = true;

    @Switch(
            name = "Lifeline"
    )
    public static boolean lifeline = false;

    @Switch(
            name = "Magic Find"
    )
    public static boolean magicFind = false;

    @Switch(
            name = "Mana Pool"
    )
    public static boolean manaPool = false;

    @Switch(
            name = "Mana Regeneration"
    )
    public static boolean manaRegeneration = false;

    @Switch(
            name = "Vitality"
    )
    public static boolean vitality = false;

    @Switch(
            name = "Speed"
    )
    public static boolean speed = false;

    @Switch(
            name = "Undead Resistance"
    )
    public static boolean undeadResistance = false;

    @Switch(
            name = "Veteran"
    )
    public static boolean veteran = false;

    @Switch(
            name = "Blazing Fortune"
    )
    public static boolean blazingFortune = false;

    @Switch(
            name = "Fishing Experience"
    )
    public static boolean fishingExperience = false;

    @Switch(
            name = "Infection"
    )
    public static boolean infection = true;

    @Switch(
            name = "Double Hook"
    )
    public static boolean doubleHook = false;

    @Switch(
            name = "Fisherman"
    )
    public static boolean fisherman = true;

    @Switch(
            name = "Fishing Speed"
    )
    public static boolean fishingSpeed = false;

    @Switch(
            name = "Hunter"
    )
    public static boolean hunter = true;

    @Switch(
            name = "Trophy Hunter"
    )
    public static boolean trophyHunter = false;

    public static Set<String> getEnabled() {
        Set<String> set = new HashSet<>();

        if (arachno) set.add("arachno");
        if (attackSpeed) set.add("attack_speed");
        if (blazing) set.add("blazing");
        if (combo) set.add("combo");
        if (elite) set.add("elite");
        if (ender) set.add("ender");
        if (ignition) set.add("ignition");
        if (lifeRecovery) set.add("life_recovery");
        if (manaSteal) set.add("mana_steal");
        if (midasTouch) set.add("midas_touch");
        if (undead) set.add("undead");
        if (warrior) set.add("warrior");
        if (deadeye) set.add("deadeye");
        if (arachnoResistance) set.add("arachno_resistance");
        if (blazingResistance) set.add("blazing_resistance");
        if (breeze) set.add("breeze");
        if (dominance) set.add("dominance");
        if (enderResistance) set.add("ender_resistance");
        if (experience) set.add("experience");
        if (fortitude) set.add("fortitude");
        if (lifeRegeneration) set.add("life_regeneration");
        if (lifeline) set.add("lifeline");
        if (magicFind) set.add("magic_find");
        if (manaPool) set.add("mana_pool");
        if (manaRegeneration) set.add("mana_regeneration");
        if (vitality) set.add("vitality");
        if (vitality) set.add("mending");
        if (speed) set.add("speed");
        if (undeadResistance) set.add("undead_resistance");
        if (veteran) set.add("veteran");
        if (blazingFortune) set.add("blazing_fortune");
        if (fishingExperience) set.add("fishing_experience");
        if (infection) set.add("infection");
        if (doubleHook) set.add("double_hook");
        if (fisherman) set.add("fisherman");
        if (fishingSpeed) set.add("fishing_speed");
        if (hunter) set.add("hunter");
        if (trophyHunter) set.add("trophy_hunter");

        return set;
    }
}
