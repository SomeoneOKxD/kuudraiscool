package someoneok.kic.models.kuudra;

public class TimedEvent {
    public final String player;
    public final long timestamp;

    public TimedEvent(String player, long timestamp) {
        this.player = player;
        this.timestamp = timestamp;
    }
}
