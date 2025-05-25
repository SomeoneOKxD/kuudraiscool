package someoneok.kic.models.kuudra;

public enum KuudraPhase {
    SUPPLIES("Supplies", 26500),
    BUILD("Build", 14500),
    EATEN("Eaten", 0),
    STUN("Stun", 0),
    DPS("DPS", 5600),
    SKIP("Skip", 4500),
    KILL("Kuudra", 2000),
    OVERALL("Overall", 53100);

    KuudraPhase(String name, long pace) {
        this.name = name;
        this.pace = pace;
    }

    private final String name;
    private final long pace;

    private long startTimestamp = -1;
    private long endTimestamp = -1;

    private long startTicks = -1;
    private long endTicks = -1;

    public String getName() {
        return name;
    }

    public void start(long now, long ticks) {
        this.startTimestamp = now;
        this.startTicks = ticks;
    }

    public void end(long now, long ticks) {
        if (this.startTimestamp == -1) {
            this.startTimestamp = now;
            this.startTicks = ticks;
        }
        if (this.endTimestamp == -1) {
            this.endTimestamp = now;
            this.endTicks = ticks;
        }
    }

    public boolean hasStarted() {
        return startTimestamp != -1;
    }

    public boolean hasEnded() {
        return endTimestamp != -1;
    }

    public long getTime(long now) {
        if (startTimestamp == -1) return 0;
        if (endTimestamp != -1) return endTimestamp - startTimestamp;
        return (now - startTimestamp);
    }

    public long getPace(long now) {
        if (startTimestamp == -1) return 0;

        long actualTime = (endTimestamp != -1) ? endTimestamp - startTimestamp : now - startTimestamp;
        return actualTime <= pace ? 0 : actualTime - pace;
    }

    public long getLag(long ticks) {
        if (startTicks == -1) return 0;
        if (endTicks != -1) return Math.abs(endTicks - startTicks);
        return Math.abs(ticks - startTicks);
    }

    public static void reset() {
        for (KuudraPhase phase : KuudraPhase.values()) {
            phase.startTimestamp = -1;
            phase.endTimestamp = -1;

            phase.startTicks = -1;
            phase.endTicks = -1;
        }
    }

    public static void endMissedPhases() {
        long now = System.currentTimeMillis();
        for (KuudraPhase phase : values()) {
            if (!phase.hasEnded()) phase.end(now, 0);
        }
    }

    public static long getEstimatedPace(long now) {
        long duration = 0;

        for (KuudraPhase phase : values()) {
            if (phase == KuudraPhase.OVERALL) continue;

            if (phase.hasEnded()) {
                duration += phase.endTimestamp - phase.startTimestamp;
            } else {
                if (!phase.hasStarted()) {
                    duration += phase.pace;
                    continue;
                }
                long elapsed = now - phase.startTimestamp;
                duration += Math.max(elapsed, phase.pace);
            }
        }

        return duration;
    }

    public static long getP3(long now) {
        return EATEN.getTime(now) + STUN.getTime(now) + DPS.getTime(now);
    }

    public static long getP4(long now) {
        return SKIP.getTime(now) + KILL.getTime(now);
    }

    public static long getP3Lag(long ticks) {
        return EATEN.getLag(ticks) + STUN.getLag(ticks) + DPS.getLag(ticks);
    }

    public static long getP4Lag(long ticks) {
        return SKIP.getLag(ticks) + KILL.getLag(ticks);
    }
}
