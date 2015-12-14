package ca.ubc.heydj.events;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/13/2015.
 */
public class BroadcastPlaylistEvent implements Serializable {

    public List<Track> playlist;
    public String host_id;
    public int current_track_index;
    public int position_ms;

}
