package ca.ubc.heydj.events;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/12/2015.
 */
public class BroadcastPlaylistEvent {

    private List<Track> tracks;


    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
}
