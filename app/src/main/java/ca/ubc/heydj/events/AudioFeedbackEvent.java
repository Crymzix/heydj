package ca.ubc.heydj.events;

import com.spotify.sdk.android.player.PlayerState;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Subscribers to this event receive updates from
 * the AudioPlaybackService regarding to player control
 *
 * Created by Chris Li on 12/13/2015.
 */
public class AudioFeedbackEvent {

    private List<SavedTrack> playlist;
    private int currentTrackIndex = 0;
    private PlayerState playerState;

    public List<SavedTrack> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<SavedTrack> playlist) {
        this.playlist = playlist;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int currentTrackIndex) {
        this.currentTrackIndex = currentTrackIndex;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public List<Track> getTracks() {

        List<Track> tracks = new ArrayList<>();
        for (SavedTrack savedTrack : playlist) {
            tracks.add(savedTrack.track);
        }

        return tracks;
    }
}
