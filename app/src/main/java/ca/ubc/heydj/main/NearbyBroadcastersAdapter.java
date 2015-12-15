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
import ca.ubc.heydj.models.BroadcastedPlaylist;
import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by Chris Li on 12/13/2015.
 */
public class NearbyBroadcastersAdapter extends RecyclerView.Adapter<NearbyBroadcastersAdapter.ViewHolder> {

    private static final String TAG = NearbyBroadcastersAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<BroadcastedPlaylist> mBroadcasts;
    private OnItemClickListener mOnItemClickListener;

    public NearbyBroadcastersAdapter(Context context, List<BroadcastedPlaylist> broadcasts) {
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
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final BroadcastedPlaylist broadcastedPlaylist = mBroadcasts.get(position);

        Track currentTrack = broadcastedPlaylist.playlist.get(broadcastedPlaylist.current_track_index);
        holder.trackArtist.setText(currentTrack.artists.get(0).name);
        holder.trackTitle.setText(currentTrack.name);
        Picasso.with(mContext)
                .load(currentTrack.album.images.get(0).url)
                .fit()
                .centerCrop()
                .into(holder.albumCover);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position, broadcastedPlaylist.playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBroadcasts.size();
    }

    public void addItem(BroadcastedPlaylist broadcastedPlaylist) {
        mBroadcasts.add(broadcastedPlaylist);
        notifyDataSetChanged();
    }

    public void updateItem(int position, BroadcastedPlaylist broadcastedPlaylist) {
        mBroadcasts.set(position, broadcastedPlaylist);
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, List<Track> tracks);
    }

}
