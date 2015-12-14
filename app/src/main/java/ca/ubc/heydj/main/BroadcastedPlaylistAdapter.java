package ca.ubc.heydj.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.BroadcastPlaylistEvent;
import de.hdodenhof.circleimageview.CircleImageView;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/13/2015.
 */
public class BroadcastedPlaylistAdapter extends RecyclerView.Adapter<BroadcastedPlaylistAdapter.ViewHolder> {

    private static final String TAG = BroadcastedPlaylistAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Track> mTracks;

    public BroadcastedPlaylistAdapter(Context context, List<Track> tracks) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTracks = tracks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.broadcastplaylist_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Track track = mTracks.get(position);

        holder.trackArtist.setText(track.artists.get(0).name);
        holder.trackTitle.setText(track.name);
        Picasso.with(mContext)
                .load(track.album.images.get(0).url)
                .fit()
                .centerCrop()
                .into(holder.albumCover);

    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView trackTitle;
        TextView trackArtist;
        CircleImageView albumCover;

        public ViewHolder(View itemView) {
            super(itemView);
            trackTitle = (TextView) itemView.findViewById(R.id.track_title);
            trackArtist = (TextView) itemView.findViewById(R.id.track_artist);
            albumCover = (CircleImageView) itemView.findViewById(R.id.album_image);
        }
    }
}
