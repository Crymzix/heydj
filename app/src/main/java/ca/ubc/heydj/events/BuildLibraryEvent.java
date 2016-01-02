package ca.ubc.heydj.events;

/**
 * Created by Chris Li on 1/2/2016.
 */
public class BuildLibraryEvent {

    public static final int FINISHED = 1;

    private int mType = -1;

    public BuildLibraryEvent(int type) {
        this.mType = type;
    }

    public int getType(){
        return this.mType;
    }

}
