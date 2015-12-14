package ca.ubc.heydj.events;

/**
 * NearbyHostService subscribes to this event.
 * <p/>
 * Created by Chris Li on 12/12/2015.
 */
public class NearbyEvent {

    public static final int CONNECT = 1;
    public static final int SUBSCRIBE = 2;
    public static final int DISCONNECT = 3;

    private int type;

    public NearbyEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
