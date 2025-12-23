package someoneok.kic.models.kuudra;

public class Supply {
    private final SupplySpot spot;
    private SupplyStatus status;
    private final float[] progressColor;

    public Supply(SupplySpot spot) {
        this.spot = spot;
        this.status = SupplyStatus.NOTHING;
        this.progressColor = new float[]{1, 0};
    }

    public SupplySpot getSpot() { return spot; }
    public SupplyStatus getStatus() { return status; }
    public float getRed() { return progressColor[0]; }
    public float getGreen() { return progressColor[1]; }

    public void setStatus(SupplyStatus status) { this.status = status; }
    public void setProgressColor(float[] color) {
        this.progressColor[0] = color[0];
        this.progressColor[1] = color[1];
    }

    public void reset() {
        this.status = SupplyStatus.NOTHING;
        this.progressColor[0] = 1;
        this.progressColor[1] = 0;
    }
}
