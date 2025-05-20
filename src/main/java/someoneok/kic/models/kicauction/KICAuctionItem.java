package someoneok.kic.models.kicauction;

import java.util.List;

import static someoneok.kic.utils.StringUtils.*;

public class KICAuctionItem {
    private final String uuid;
    private final String itemId;
    private final long price;
    private final Extra extra;
    private final String attribute1;
    private final Integer attributeLvl1;
    private final String attribute2;
    private final Integer attributeLvl2;

    public KICAuctionItem(String uuid, String itemId, long price, String attribute1, Integer attributeLvl1, String attribute2, Integer attributeLvl2, List<String> lore, String modifier, Integer upgrade_level) {
        this.uuid = uuid;
        this.itemId = itemId;
        this.price = price;
        this.attribute1 = attribute1;
        this.attributeLvl1 = attributeLvl1;
        this.attribute2 = attribute2;
        this.attributeLvl2 = attributeLvl2;
        this.extra = new Extra(lore, modifier, upgrade_level);
    }

    public KICAuctionItem(String uuid, String itemId, long price, String attribute1, Integer attributeLvl1, String attribute2, Integer attributeLvl2, List<String> lore) {
        this.uuid = uuid;
        this.itemId = itemId;
        this.price = price;
        this.attribute1 = attribute1;
        this.attributeLvl1 = attributeLvl1;
        this.attribute2 = attribute2;
        this.attributeLvl2 = attributeLvl2;
        this.extra = new Extra(lore);
    }

    public KICAuctionItem(String uuid, String itemId, long price, String attribute1, Integer attributeLvl1, String attribute2, Integer attributeLvl2) {
        this.uuid = uuid;
        this.itemId = itemId;
        this.price = price;
        this.attribute1 = attribute1;
        this.attributeLvl1 = attributeLvl1;
        this.attribute2 = attribute2;
        this.attributeLvl2 = attributeLvl2;
        this.extra = null;
    }

    public String getUuid() {
        return uuid;
    }

    public String getItemId() {
        return itemId;
    }

    public long getPrice() {
        return price;
    }

    public List<String> getLore() {
        return extra.getLore();
    }

    public String getModifier() {
        return extra.getModifier();
    }

    public Integer getUpgrade_level() {
        return extra.getUpgrade_level();
    }

    public boolean hasAttribute1() {
        return !isNullOrEmpty(attribute1) && attributeLvl1 != null && attributeLvl1 != 0;
    }

    public boolean hasAttribute2() {
        return !isNullOrEmpty(attribute2) && attributeLvl2 != null && attributeLvl2 != 0;
    }

    public String getAttribute1(boolean m1) {
        String formattedAttribute = formatId(attribute1);

        if (m1) {
            return formattedAttribute + " " + attributeLvl1;
        } else {
            return formattedAttribute + " " + toRoman(attributeLvl1);
        }
    }

    public String getAttribute2(boolean m1) {
        String formattedAttribute = formatId(attribute2);

        if (m1) {
            return formattedAttribute + " " + attributeLvl2;
        } else {
            return formattedAttribute + " " + toRoman(attributeLvl2);
        }
    }

    public Integer getAttributeLvl1() {
        return attributeLvl1;
    }

    public Integer getAttributeLvl2() {
        return attributeLvl2;
    }

    private static class Extra {
        private final List<String> lore;
        private final String modifier;
        private final Integer upgrade_level;

        public Extra(List<String> lore, String modifier, Integer upgrade_level) {
            this.lore = lore;
            this.modifier = modifier;
            this.upgrade_level = upgrade_level;
        }

        public Extra(List<String> lore) {
            this.lore = lore;
            this.modifier = null;
            this.upgrade_level = null;
        }

        public List<String> getLore() {
            return lore;
        }

        public String getModifier() {
            return modifier;
        }

        public Integer getUpgrade_level() {
            return upgrade_level;
        }
    }
}
