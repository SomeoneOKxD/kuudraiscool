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
            text = "Wheel Of Fate, Burning Kuudra Core, Enrager, Tentacle Dye, Fatal Tempo and Inferno are never rerolled by default!",
            type = InfoType.INFO,
            size = 2
    )
    public static boolean ignored2;

    @Switch(
            name = "Arachno"
    )
    public static boolean arachno = false;

    @Switch(
            name = "Attack Speed"
    )
    public static boolean attackSpeed = false;

    @Switch(
            name = "Blazing"
    )
    public static boolean blazing = false;

    @Switch(
            name = "Combo"
    )
    public static boolean combo = false;

    @Switch(
            name = "Elite"
    )
    public static boolean elite = false;

    @Switch(
            name = "Ender"
    )
    public static boolean ender = false;

    @Switch(
            name = "Ignition"
    )
    public static boolean ignition = false;

    @Switch(
            name = "Life Recovery"
    )
    public static boolean lifeRecovery = false;

    @Switch(
            name = "Mana Steal"
    )
    public static boolean manaSteal = false;

    @Switch(
            name = "Midas Touch"
    )
    public static boolean midasTouch = false;

    @Switch(
            name = "Undead"
    )
    public static boolean undead = false;

    @Switch(
            name = "Warrior"
    )
    public static boolean warrior = false;

    @Switch(
            name = "Deadeye"
    )
    public static boolean deadeye = false;

    @Switch(
            name = "Arachno Resistance"
    )
    public static boolean arachnoResistance = false;

    @Switch(
            name = "Blazing Resistance"
    )
    public static boolean blazingResistance = false;

    @Switch(
            name = "Breeze"
    )
    public static boolean breeze = false;

    @Switch(
            name = "Dominance"
    )
    public static boolean dominance = true;

    @Switch(
            name = "Ender Resistance"
    )
    public static boolean enderResistance = false;

    @Switch(
            name = "Experience"
    )
    public static boolean experience = false;

    @Switch(
            name = "Fortitude"
    )
    public static boolean fortitude = false;

    @Switch(
            name = "Life Regeneration"
    )
    public static boolean lifeRegeneration = false;

    @Switch(
            name = "Lifeline"
    )
    public static boolean lifeline = true;

    @Switch(
            name = "Magic Find"
    )
    public static boolean magicFind = true;

    @Switch(
            name = "Mana Pool"
    )
    public static boolean manaPool = true;

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
    public static boolean veteran = true;

    @Switch(
            name = "Blazing Fortune"
    )
    public static boolean blazingFortune = true;

    @Switch(
            name = "Fishing Experience"
    )
    public static boolean fishingExperience = true;

    @Switch(
            name = "Infection"
    )
    public static boolean infection = false;

    @Switch(
            name = "Double Hook"
    )
    public static boolean doubleHook = true;

    @Switch(
            name = "Fisherman"
    )
    public static boolean fisherman = false;

    @Switch(
            name = "Fishing Speed"
    )
    public static boolean fishingSpeed = true;

    @Switch(
            name = "Hunter"
    )
    public static boolean hunter = false;

    @Switch(
            name = "Trophy Hunter"
    )
    public static boolean trophyHunter = true;

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
