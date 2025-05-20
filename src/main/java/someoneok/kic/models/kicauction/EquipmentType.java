package someoneok.kic.models.kicauction;

import java.util.*;

import static someoneok.kic.utils.StringUtils.formatId;

public enum EquipmentType {
    // Necklaces
    MOLTEN_NECKLACE(EquipmentCategory.NECKLACES),
    DELIRIUM_NECKLACE(EquipmentCategory.NECKLACES),
    LAVA_SHELL_NECKLACE(EquipmentCategory.NECKLACES),
    MAGMA_NECKLACE(EquipmentCategory.NECKLACES),
    VANQUISHED_MAGMA_NECKLACE(EquipmentCategory.NECKLACES),
    THUNDERBOLT_NECKLACE(EquipmentCategory.NECKLACES),

    // Cloaks
    MOLTEN_CLOAK(EquipmentCategory.CLOAKS),
    SCOURGE_CLOAK(EquipmentCategory.CLOAKS),
    GHAST_CLOAK(EquipmentCategory.CLOAKS),
    VANQUISHED_GHAST_CLOAK(EquipmentCategory.CLOAKS),

    // Belts
    MOLTEN_BELT(EquipmentCategory.BELTS),
    IMPLOSION_BELT(EquipmentCategory.BELTS),
    SCOVILLE_BELT(EquipmentCategory.BELTS),
    BLAZE_BELT(EquipmentCategory.BELTS),
    VANQUISHED_BLAZE_BELT(EquipmentCategory.BELTS),

    // Bracelets & Gauntlets
    MOLTEN_BRACELET(EquipmentCategory.BRACELETS),
    GAUNTLET_OF_CONTAGION(EquipmentCategory.BRACELETS),
    FLAMING_FIST(EquipmentCategory.BRACELETS),
    GLOWSTONE_GAUNTLET(EquipmentCategory.BRACELETS),
    VANQUISHED_GLOWSTONE_GAUNTLET(EquipmentCategory.BRACELETS),
    MAGMA_LORD_GAUNTLET(EquipmentCategory.BRACELETS);

    private final String displayText;
    private final EquipmentCategory category;

    EquipmentType(EquipmentCategory category) {
        this.category = category;
        this.displayText = formatId(this.name());
    }

    public String getDisplayText() {
        return displayText;
    }

    public static Set<EquipmentType> getTypes(EquipmentCategory category) {
        EnumSet<EquipmentType> types = EnumSet.noneOf(EquipmentType.class);
        for (EquipmentType type : EquipmentType.values()) {
            if (type.category == category) {
                types.add(type);
            }
        }
        return types;
    }

    public static EquipmentType getNext(EquipmentCategory category, EquipmentType type) {
        List<EquipmentType> types = new ArrayList<>(getTypes(category));
        if (types.isEmpty()) return null;

        int index = types.indexOf(type);
        return types.get((index + 1) % types.size());
    }

    public static EquipmentType getPrevious(EquipmentCategory category, EquipmentType type) {
        List<EquipmentType> types = new ArrayList<>(getTypes(category));
        if (types.isEmpty()) return null;

        int index = (types.indexOf(type) - 1 + types.size()) % types.size();
        return types.get(index);
    }

    public static Optional<EquipmentType> getFromId(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        try {
            return Optional.of(EquipmentType.valueOf(id.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
