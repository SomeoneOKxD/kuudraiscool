package someoneok.kic.models.crimson;

import java.util.Objects;

public class AttributeItem {
    private final String uuid;
    private final String name;
    private final String itemId;
    private final Attributes attributes;

    public AttributeItem(String uuid, String name, String itemId, Attributes attributes) {
        this.uuid = uuid;
        this.name = name;
        this.itemId = itemId;
        this.attributes = attributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        String name = this.name;
        if (attributes.hasAttribute1()) {
            name += String.format(" §r§7[§b%s§7]", attributes.getAttribute1());
        }
        if (attributes.hasAttribute2()) {
            name += String.format(" §r§7[§b%s§7]", attributes.getAttribute2());
        }
        return name;
    }

    public String getItemId() {
        return itemId;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public boolean hasAttributes() {
        return attributes != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeItem that = (AttributeItem) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(itemId, that.itemId) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, itemId, attributes);
    }

    @Override
    public String toString() {
        return "AttributeItem{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", itemId='" + itemId + '\'' +
                ", attributes=" + attributes.toString() +
                '}';
    }
}
