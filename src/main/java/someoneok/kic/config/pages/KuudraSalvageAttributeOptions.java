package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;

import java.util.HashSet;
import java.util.Set;

public class KuudraSalvageAttributeOptions {
    private transient static final String ATTRIBUTE = "Attributes";

    @Info(
            text = "Salvage Value Attributes",
            type = InfoType.INFO,
            size = 2
    )
    public static boolean ignored;

    @Info(
            text = "Every enabled attribute will be counted as salvage value.",
            type = InfoType.INFO,
            size = 2
    )
    public static boolean ignored2;

    @Switch(
            name = "Use Salvage Value for Matching Attributes",
            size = 2
    )
    public static boolean useSalvageValue = true;

    @Switch(
            name = "Life Recovery",
            subcategory = ATTRIBUTE
    )
    public static boolean lifeRecovery = true;

    @Switch(
            name = "Arachno Resistance",
            subcategory = ATTRIBUTE
    )
    public static boolean arachnoResistance = true;

    @Switch(
            name = "Blazing Resistance",
            subcategory = ATTRIBUTE
    )
    public static boolean blazingResistance = true;

    @Switch(
            name = "Breeze",
            subcategory = ATTRIBUTE
    )
    public static boolean breeze = false;

    @Switch(
            name = "Dominance",
            subcategory = ATTRIBUTE
    )
    public static boolean dominance = false;

    @Switch(
            name = "Ender Resistance",
            subcategory = ATTRIBUTE
    )
    public static boolean enderResistance = true;

    @Switch(
            name = "Experience",
            subcategory = ATTRIBUTE
    )
    public static boolean experience = true;

    @Switch(
            name = "Fortitude",
            subcategory = ATTRIBUTE
    )
    public static boolean fortitude = true;

    @Switch(
            name = "Life Regeneration",
            subcategory = ATTRIBUTE
    )
    public static boolean lifeRegeneration = true;

    @Switch(
            name = "Lifeline",
            subcategory = ATTRIBUTE
    )
    public static boolean lifeline = false;

    @Switch(
            name = "Magic Find",
            subcategory = ATTRIBUTE
    )
    public static boolean magicFind = false;

    @Switch(
            name = "Mana Pool",
            subcategory = ATTRIBUTE
    )
    public static boolean manaPool = false;

    @Switch(
            name = "Mana Regeneration",
            subcategory = ATTRIBUTE
    )
    public static boolean manaRegeneration = false;

    @Switch(
            name = "Vitality",
            subcategory = ATTRIBUTE
    )
    public static boolean vitality = false;

    @Switch(
            name = "Speed",
            subcategory = ATTRIBUTE
    )
    public static boolean speed = true;

    @Switch(
            name = "Undead Resistance",
            subcategory = ATTRIBUTE
    )
    public static boolean undeadResistance = false;

    @Switch(
            name = "Veteran",
            subcategory = ATTRIBUTE
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
