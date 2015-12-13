package ca.ubc.heydj.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.BroadcastPlaylistEvent;
import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by Chris Li on 12/13/2015.
 */
public class NearbyHostsAdapter extends RecyclerView.Adapter<NearbyHostsAdapter.ViewHolder>{

    private static final String TAG = NearbyHostsAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<BroadcastPlaylistEvent> mBroadcasts;

    public NearbyHostsAdapter(Context context, List<BroadcastPlaylistEvent> broadcasts) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mBroadcasts = broadcasts;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.nearby_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        BroadcastPlaylistEvent broadcastPlaylistEvent = mBroadcasts.get(position);

        Track currentTrack = broadcastPlaylistEvent.playlist.get(broadcastPlaylistEvent.current_track_index);
        holder.trackArtist.setText(currentTrack.artists.get(0).name);
        holder.trackTitle.setText(currentTrack.name);
        Picasso.with(mContext)
                .load(currentTrack.album.images.get(0).url)
                .fit()
                .centerCrop()
                .into(holder.albumCover);
    }

    @Override
    public int getItemCount() {
        return mBroadcasts.size();
    }

    public void addItem(BroadcastPlaylistEvent broadcastPlaylistEvent) {
        mBroadcasts.add(broadcastPlaylistEvent);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView trackTitle;
        TextView trackArtist;
        ImageView albumCover;

        public ViewHolder(View v) {
            super(v);
            trackTitle = (TextView) v.findViewById(R.id.track_title);
            trackArtist = (TextView) v.findViewById(R.id.track_artist);
            albumCover = (ImageView) v.findViewById(R.id.album_image);
        }
    }

}
