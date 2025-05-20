package someoneok.kic.models.request;

public class AuctionDataRequest {
    private String attribute1;
    private Integer attributeLvl1;
    private String attribute2;
    private Integer attributeLvl2;

    public AuctionDataRequest() {}

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

    public String getAttribute1() {
        return attribute1;
    }

    public Integer getAttributeLvl1() {
        return attributeLvl1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public Integer getAttributeLvl2() {
        return attributeLvl2;
    }

    @Override
    public String toString() {
        return "AuctionRequest{" +
                "attribute1='" + attribute1 + '\'' +
                ", attributeLvl1=" + attributeLvl1 +
                ", attribute2='" + attribute2 + '\'' +
                ", attributeLvl2=" + attributeLvl2 +
                '}';
    }
}
