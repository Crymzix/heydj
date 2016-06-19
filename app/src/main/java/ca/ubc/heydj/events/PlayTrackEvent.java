package ca.ubc.heydj.events;

import java.util.List;

import ca.ubc.heydj.models.Track;
import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * The AudioPlaybackService is subscribed to this
 * event in order to play a particular song in a playlist
 * <p/>
 * Created by Chris Li on 12/12/2015.
 */
public class PlayTrackEvent {

    private List<SavedTrack> userTracks;
    private int currentTrackIndex = 0;

    private Track track = null;

    public List<SavedTrack> getUserTracks() {
        return userTracks;
    }

    public void setUserTracks(List<SavedTrack> userTracks) {
        this.userTracks = userTracks;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int currentTrackIndex) {
        this.currentTrackIndex = currentTrackIndex;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
