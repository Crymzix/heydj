package ca.ubc.heydj.spotify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.malinskiy.superrecyclerview.swipe.BaseSwipeAdapter;
import com.malinskiy.superrecyclerview.swipe.SwipeLayout;

import java.util.List;

import ca.ubc.heydj.MainApplication;
import ca.ubc.heydj.R;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Adapter to show the list of music tracks
 * <p/>
 * Created by Chris Li on 12/11/2015.
 */
public class TracksAdapter extends BaseSwipeAdapter<TracksAdapter.ViewHolder> implements SwipeLayout.SwipeListener{

    private static final String TAG = TracksAdapter.class.getSimpleName();

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<SavedTrack> mTracks;
    private OnItemClickListener mOnItemClickListener = null;
    private MainApplication mMain;

    public TracksAdapter(Context context, List<SavedTrack> tracks) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTracks = tracks;
        this.mMain = (MainApplication) context.getApplicationContext();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.track_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        if (mMain.isQueuing()) {
            SwipeLayout swipeLayout = viewHolder.swipeLayout;
            swipeLayout.setDragEdge(SwipeLayout.DragEdge.Left);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipeLayout.addSwipeListener(this);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Track track = mTracks.get(position).track;
        holder.trackTitle.setText(track.name);
        holder.trackArtist.setText(track.artists.get(0).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position, track);
                }
            }
        });

        if (mMain.isQueuing()) {
            holder.divider.setBackgroundResource(R.drawable.divider_orange_drawable);
        } else {
            holder.divider.setBackgroundResource(R.drawable.divider_blue_drawable);
        }

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public SavedTrack getTrack(int position) {
        return mTracks.get(position);
    }

    public List<SavedTrack> getSavedTracks() {
        return mTracks;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onStartOpen(SwipeLayout layout) {
        Log.i(TAG, "onStartOpen!");
    }

    @Override
    public void onOpen(SwipeLayout layout) {
        Log.i(TAG, "onOpen!");
    }

    @Override
    public void onStartClose(SwipeLayout layout) {
        Log.i(TAG, "onStartClose!");
    }

    @Override
    public void onClose(SwipeLayout layout) {
        Log.i(TAG, "onClose!");
    }

    @Override
    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
        Log.i(TAG, "onUpdate!");
    }

    @Override
    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
        Log.i(TAG, "onHandRelease!");
    }


    public static class ViewHolder extends BaseSwipeAdapter.BaseSwipeableViewHolder {
        TextView trackTitle;
        TextView trackArtist;
        View divider;

        public ViewHolder(View v) {
            super(v);
            trackTitle = (TextView) v.findViewById(R.id.track_title);
            trackArtist = (TextView) v.findViewById(R.id.track_artist);
            divider = v.findViewById(R.id.divider);
        }

    }

    public interface OnItemClickListener {
        void onItemClick(int position, Track selectedTrack);
    }

}
