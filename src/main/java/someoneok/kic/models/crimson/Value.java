package someoneok.kic.models.crimson;

import someoneok.kic.models.request.Request;

public interface Value {
    boolean isFetching();

    boolean isCached();

    long getTimestamp();

    Request mapToRequest();

    void setFetching(boolean fetching);

    void setCached(boolean cached);
}
