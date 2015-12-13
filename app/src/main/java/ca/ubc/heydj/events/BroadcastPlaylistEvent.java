package ca.ubc.heydj.events;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/13/2015.
 */
public class BroadcastPlaylistEvent implements Serializable {

    public List<Track> playlist;
    public String host_id;
    public int current_track_index;
    public int duration_ms;
}
