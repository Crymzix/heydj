package ca.ubc.heydj.events;

import java.util.List;

import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/12/2015.
 */
public class PlayTrackEvent {

    private List<Track> tracks;
    private List<SavedTrack> userTracks;
    private Track currentTrack;

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }

    public List<SavedTrack> getUserTracks() {
        return userTracks;
    }

    public void setUserTracks(List<SavedTrack> userTracks) {
        this.userTracks = userTracks;
    }
}
