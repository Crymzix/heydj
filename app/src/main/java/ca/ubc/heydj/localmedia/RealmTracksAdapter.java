package ca.ubc.heydj.localmedia;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.ubc.heydj.R;
import ca.ubc.heydj.models.Track;
import io.realm.RealmResults;

/**
 * Created by Chris Li on 1/2/2016.
 */
public class RealmTracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private RealmResults<Track> mTracks;
    private OnItemClickListener mListener;

    public RealmTracksAdapter(Context context, RealmResults<Track> tracks) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTracks = tracks;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.track_normal_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Track track = mTracks.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.trackTitle.setText(track.getSongTitle());
        viewHolder.trackArtist.setText(track.getSongArtist());
        viewHolder.divider.setBackgroundResource(R.drawable.divider_blue_drawable);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(track);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mTracks.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView trackTitle;
        TextView trackArtist;
        View divider;
        TextView playState;

        public ViewHolder(View v) {
            super(v);
            trackTitle = (TextView) v.findViewById(R.id.track_title);
            trackArtist = (TextView) v.findViewById(R.id.track_artist);
            divider = v.findViewById(R.id.divider);
            playState = (TextView) v.findViewById(R.id.track_state);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Track track);
    }
}
