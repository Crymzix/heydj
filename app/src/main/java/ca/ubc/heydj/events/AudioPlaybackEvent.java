package ca.ubc.heydj.events;

/**
 * Created by Chris Li on 12/12/2015.
 */
public class AudioPlaybackEvent {

    public static final int PLAY_PAUSE = 1;
    public static final int NEXT = 2;
    public static final int PREVOUS = 3;
    public static final int STOP = 4;
    public static final int START = 5;

    private int eventType = 0;


    public AudioPlaybackEvent(int eventType) {
        setEventType(eventType);
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
