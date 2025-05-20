package someoneok.kic.models.request;

public class AttributeUpgradeRequest {
    private final String attribute;
    private final String item;
    private final Integer startLevel;
    private final Integer endLevel;

    public AttributeUpgradeRequest(String attribute, String item, int startLevel, int endLevel) {
        this.attribute = attribute;
        this.item = item;
        this.startLevel = startLevel;
        this.endLevel = endLevel;
    }
}
