package someoneok.kic.models.data;

public class OverlayLocationData {
    private final int x;
    private final int y;
    private final double scale;

    public OverlayLocationData(int x, int y, double scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getScale() {
        return scale;
    }
}
