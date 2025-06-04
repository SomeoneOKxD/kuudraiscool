package someoneok.kic.models.crimson;

import someoneok.kic.config.pages.KuudraIgnoredEquipmentAttributeOptions;
import someoneok.kic.config.pages.KuudraIgnoredShardAttributeOptions;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.config.pages.KuudraSalvageAttributeOptions;
import someoneok.kic.models.request.AttributesPriceRequest;

import java.util.Set;

public class AttributeItemValue implements Value {
    private final AttributeItem item;
    private boolean cached;
    private boolean fetching;
    private long timestamp;
    private long itemPrice;

    public AttributeItemValue(AttributeItem attributeItem) {
        item = attributeItem;
        this.cached = false;
        this.fetching = false;
        this.timestamp = System.currentTimeMillis();
        this.itemPrice = 0;
    }

    public void setItemPrice(long price) {
        this.itemPrice = price;
    }

    public String getUuid() {
        return item.getUuid();
    }

    public String getName() {
        return item.getName();
    }

    public String getFullName() {
        return item.getFullName();
    }

    public String getItemId() {
        return item.getItemId();
    }

    public Attributes getAttributes() {
        return item.getAttributes();
    }

    public boolean isCached() {
        return cached;
    }

    public boolean isFetching() {
        return fetching;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public void setFetching(boolean fetching) {
        this.fetching = fetching;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getPrice(boolean lb) {
        Attributes attributes = item.getAttributes();
        if (attributes.isGodroll()) {
            return lb ? attributes.getGodrollLbPrice() : attributes.getGodrollAvgPrice();
        }

        if ("HOLLOW_WAND".equals(item.getItemId())) {
            return itemPrice;
        } else {
            long price1 = lb ? attributes.getLbPrice1() : attributes.getAvgPrice1();
            long price2 = lb ? attributes.getLbPrice2() : attributes.getAvgPrice2();

            return Math.max(price1, price2);
        }
    }

    public long getValue(BazaarItemValue essencePrice) {
        if ("HOLLOW_WAND".equals(item.getItemId())) {
            if (KuudraProfitCalculatorOptions.ignoreHollowWands) return 0;
            return itemPrice;
        }

        Attributes attributes = item.getAttributes();
        if (attributes.isGodroll()) {
            return KuudraProfitCalculatorOptions.godRollPriceType == 0 ? attributes.getGodrollLbPrice() : attributes.getGodrollAvgPrice();
        }

        if ("ATTRIBUTE_SHARD".equals(item.getItemId())) {
            long price = KuudraProfitCalculatorOptions.attributePriceType == 0 ? attributes.getLbPrice1() : attributes.getAvgPrice1();

            if (KuudraIgnoredShardAttributeOptions.ignoreShardAttributes) {
                Set<String> ignoreAttributes = KuudraIgnoredShardAttributeOptions.getEnabled();
                if (attributes.hasAttribute1() && ignoreAttributes.contains(attributes.getName1())) price = 0;
            }

            return price;
        } else {
            long essenceValue = essencePrice.getPrice(KuudraProfitCalculatorOptions.bazaarPriceType == 1);
            long salvageValue = getSalvageValue(essenceValue);

            long price1 = KuudraProfitCalculatorOptions.attributePriceType == 0 ? attributes.getLbPrice1() : attributes.getAvgPrice1();
            long price2 = KuudraProfitCalculatorOptions.attributePriceType == 0 ? attributes.getLbPrice2() : attributes.getAvgPrice2();

            if (KuudraIgnoredEquipmentAttributeOptions.ignoreEquipmentAttributes && isEquipment()) {
                Set<String> ignoreAttributes = KuudraIgnoredEquipmentAttributeOptions.getEnabled();
                if (attributes.hasAttribute1() && ignoreAttributes.contains(attributes.getName1())) price1 = 0;
                if (attributes.hasAttribute2() && ignoreAttributes.contains(attributes.getName2())) price2 = 0;
            }

            if (KuudraSalvageAttributeOptions.useSalvageValue && isArmor()) {
                Set<String> salvageAttributes = KuudraSalvageAttributeOptions.getEnabled();
                if (attributes.hasAttribute1() && salvageAttributes.contains(attributes.getName1())) price1 = salvageValue;
                if (attributes.hasAttribute2() && salvageAttributes.contains(attributes.getName2())) price2 = salvageValue;
            }

            return Math.max(price1, price2);
        }
    }

    public long getSalvageValue(long essencePrice) {
        int totalTiers = 0;
        Attributes attributes = item.getAttributes();
        if (attributes.getLevel1() > 0) totalTiers += 1 << (attributes.getLevel1() - 1);
        if (attributes.getLevel2() > 0) totalTiers += 1 << (attributes.getLevel2() - 1);
        return (10L * totalTiers) * essencePrice;
    }

    public boolean isUsingCustomValue(BazaarItemValue essencePrice) {
        Attributes attributes = item.getAttributes();

        if ("ATTRIBUTE_SHARD".equals(item.getItemId())) {
            if (!KuudraIgnoredShardAttributeOptions.ignoreShardAttributes) return false;
            Set<String> ignoreAttributes = KuudraIgnoredShardAttributeOptions.getEnabled();
            return attributes.hasAttribute1() && ignoreAttributes.contains(attributes.getName1());
        }

        if (KuudraIgnoredEquipmentAttributeOptions.ignoreEquipmentAttributes && isEquipment()) {
            Set<String> ignoreAttributes = KuudraIgnoredEquipmentAttributeOptions.getEnabled();
            return (attributes.hasAttribute1() && ignoreAttributes.contains(attributes.getName1())) ||
                    (attributes.hasAttribute2() && ignoreAttributes.contains(attributes.getName2()));
        }

        if (KuudraSalvageAttributeOptions.useSalvageValue && isArmor()) {
            long salvageValue = getSalvageValue(essencePrice.getPrice(KuudraProfitCalculatorOptions.bazaarPriceType == 1));
            long itemValue = getValue(essencePrice);
            return salvageValue == itemValue;
        }

        return false;
    }

    public AttributesPriceRequest mapToRequest() {
        Attributes attributes = item.getAttributes();
        AttributesPriceRequest request = new AttributesPriceRequest();
        request.setUuid(item.getUuid());
        request.setItemId(item.getItemId());
        request.setAttribute1(attributes.getName1());
        request.setAttributeLvl1(attributes.getLevel1());
        if (attributes.getName2() != null) {
            request.setAttribute2(attributes.getName2());
            request.setAttributeLvl2(attributes.getLevel2());
        }
        return request;
    }

    @Override
    public String toString() {
        return "AttributeItemValue{" +
                "item=" + item.toString() +
                ", cached=" + cached +
                ", fetching=" + fetching +
                ", timestamp=" + timestamp +
                '}';
    }

    private boolean isEquipment() {
        String id = item.getItemId();
        if (id == null) return false;
        return id.contains("NECKLACE") || id.contains("CLOAK") || id.contains("BELT") || id.contains("GLOVES") || id.contains("BRACELET") || id.contains("GAUNTLET");
    }

    private boolean isArmor() {
        String id = item.getItemId();
        if (id == null) return false;
        return id.contains("HELMET") || id.contains("CHESTPLATE") || id.contains("LEGGINGS") || id.contains("BOOTS");
    }
}
