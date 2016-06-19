package ca.ubc.heydj.events;

import com.spotify.sdk.android.player.PlayerState;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Subscribers to this event receive updates from
 * the AudioPlaybackService regarding to player control
 * <p/>
 * Created by Chris Li on 12/13/2015.
 */
public class AudioFeedbackEvent {

    public static final int STARTED = 1;
    public static final int PLAYING = 2;
    public static final int TRACK_CHANGED = 3;
    public static final int STOPPED = 4;
    public static final int TRACK_QUEUED = 5;

    private List<SavedTrack> playlist;
    private int currentTrackIndex = 0;
    private PlayerState playerState;
    private int type;
    private SavedTrack queuedTrack;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SavedTrack getQueuedTrack() {
        return queuedTrack;
    }

    public void setQueuedTrack(SavedTrack queuedTrack) {
        this.queuedTrack = queuedTrack;
    }
}
