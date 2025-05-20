package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;

import java.util.HashSet;
import java.util.Set;

public class KuudraIgnoredEquipmentAttributeOptions {
    @Info(
            text = "Ignored Attribute on Equipment",
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
            name = "Life Recovery"
    )
    public static boolean lifeRecovery = true;

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
    public static boolean speed = true;

    @Switch(
            name = "Undead Resistance"
    )
    public static boolean undeadResistance = false;

    @Switch(
            name = "Veteran"
    )
    public static boolean veteran = false;

    public static Set<String> getEnabled() {
        Set<String> set = new HashSet<>();

        if (lifeRecovery) set.add("life_recovery");
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

        return set;
    }
}
