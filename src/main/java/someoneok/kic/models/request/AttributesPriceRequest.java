package someoneok.kic.models.request;

public class AttributesPriceRequest implements Request {
    private String type;
    private String uuid;
    private String itemId;
    private String attribute1;
    private Integer attributeLvl1;
    private String attribute2;
    private Integer attributeLvl2;

    public AttributesPriceRequest() {
        this.type = "ATTRIBUTES";
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public void setAttributeLvl1(Integer attributeLvl1) {
        this.attributeLvl1 = attributeLvl1;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    public void setAttributeLvl2(Integer attributeLvl2) {
        this.attributeLvl2 = attributeLvl2;
    }
}
