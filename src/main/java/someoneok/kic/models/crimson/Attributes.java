package someoneok.kic.models.crimson;

import someoneok.kic.config.KICConfig;

import java.util.Objects;

import static someoneok.kic.utils.GeneralUtils.shortenAttribute;
import static someoneok.kic.utils.StringUtils.formatId;

public class Attributes {
    private final String name1;
    private int level1;
    private long lbPrice1;
    private long AvgPrice1;
    private final String name2;
    private int level2;
    private long lbPrice2;
    private long AvgPrice2;
    private boolean godroll;
    private long godrollLbPrice;
    private long godrollAvgPrice;

    public Attributes(String name1, int level1, String name2, int level2) {
        this.name1 = name1;
        this.level1 = level1;
        this.name2 = name2;
        this.level2 = level2;

        this.lbPrice1 = 0;
        this.AvgPrice1 = 0;
        this.lbPrice2 = 0;
        this.AvgPrice2 = 0;
        this.godroll = false;
        this.godrollLbPrice = 0;
        this.godrollAvgPrice = 0;
    }

    public String getName1() {
        return name1;
    }

    public String getFormattedName1() {
        return formatId(name1);
    }

    public int getLevel1() {
        return level1;
    }

    public String getName2() {
        return name2;
    }

    public String getFormattedName2() {
        return formatId(name2);
    }

    public int getLevel2() {
        return level2;
    }

    public boolean hasAttribute1() {
        return name1 != null;
    }

    public boolean hasAttribute2() {
        return name2 != null;
    }

    public String getAttribute1() {
        if (!hasAttribute1()) return null;
        if (KICConfig.useShortenedAttribute) {
            return shortenAttribute(name1) + " " + level1;
        } else {
            return formatId(name1) + " " + level1;
        }
    }

    public String getAttribute2() {
        if (!hasAttribute2()) return null;
        if (KICConfig.useShortenedAttribute) {
            return shortenAttribute(name2) + " " + level2;
        } else {
            return formatId(name2) + " " + level2;
        }
    }

    public long getLbPrice1() {
        return lbPrice1;
    }

    public void setLbPrice1(long lbPrice1) {
        this.lbPrice1 = lbPrice1;
    }

    public long getAvgPrice1() {
        return AvgPrice1;
    }

    public void setAvgPrice1(long avgPrice1) {
        AvgPrice1 = avgPrice1;
    }

    public long getLbPrice2() {
        return lbPrice2;
    }

    public void setLbPrice2(long lbPrice2) {
        this.lbPrice2 = lbPrice2;
    }

    public long getAvgPrice2() {
        return AvgPrice2;
    }

    public void setAvgPrice2(long avgPrice2) {
        AvgPrice2 = avgPrice2;
    }

    public boolean isGodroll() {
        return godroll;
    }

    public void setGodroll(boolean godroll) {
        this.godroll = godroll;
    }

    public long getGodrollLbPrice() {
        return godrollLbPrice;
    }

    public void setGodrollLbPrice(long godrollLbPrice) {
        this.godrollLbPrice = godrollLbPrice;
    }

    public long getGodrollAvgPrice() {
        return godrollAvgPrice;
    }

    public void setGodrollAvgPrice(long godrollAvgPrice) {
        this.godrollAvgPrice = godrollAvgPrice;
    }

    public void setLevel1(int level1) {
        this.level1 = level1;
    }

    public void setLevel2(int level2) {
        this.level2 = level2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attributes that = (Attributes) o;
        return level1 == that.level1 &&
                level2 == that.level2 &&
                lbPrice1 == that.lbPrice1 &&
                AvgPrice1 == that.AvgPrice1 &&
                lbPrice2 == that.lbPrice2 &&
                AvgPrice2 == that.AvgPrice2 &&
                godroll == that.godroll &&
                godrollLbPrice == that.godrollLbPrice &&
                godrollAvgPrice == that.godrollAvgPrice &&
                Objects.equals(name1, that.name1) &&
                Objects.equals(name2, that.name2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name1, level1, lbPrice1, AvgPrice1, name2, level2, lbPrice2, AvgPrice2, godroll, godrollLbPrice, godrollAvgPrice);
    }

    @Override
    public String toString() {
        return "Attributes{" +
                "name1='" + name1 + '\'' +
                ", level1=" + level1 +
                ", lbPrice1=" + lbPrice1 +
                ", AvgPrice1=" + AvgPrice1 +
                ", name2='" + name2 + '\'' +
                ", level2=" + level2 +
                ", lbPrice2=" + lbPrice2 +
                ", AvgPrice2=" + AvgPrice2 +
                ", godroll=" + godroll +
                ", godrollLbPrice=" + godrollLbPrice +
                ", godrollAvgPrice=" + godrollAvgPrice +
                '}';
    }
}
