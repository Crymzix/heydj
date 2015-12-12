package ca.ubc.heydj.spotify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ca.ubc.heydj.R;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Adapter to show the list of music tracks
 *
 * Created by Chris Li on 12/11/2015.
 */
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder>{

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<SavedTrack> mTracks;

    public TracksAdapter(Context context, List<SavedTrack> tracks) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTracks = tracks;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.track_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Track track = mTracks.get(position).track;
        holder.trackTitle.setText(track.name);
        holder.trackArtist.setText(track.artists.get(0).name);

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public List<SavedTrack> getSavedTracks() {
        return mTracks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView trackTitle;
        TextView trackArtist;


        public ViewHolder(View v) {
            super(v);
            trackTitle = (TextView) v.findViewById(R.id.track_title);
            trackArtist = (TextView) v.findViewById(R.id.track_artist);
        }
    }

}
