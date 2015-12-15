package ca.ubc.heydj.models;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Object mainly user by the Gson library to serialize and deserialize objects quickly
 *
 * Created by Chris Li on 12/13/2015.
 */
public class BroadcastedPlaylist implements Serializable {

    public List<Track> playlist;
    public String host_id;
    public int current_track_index;
    public int position_ms;

}
