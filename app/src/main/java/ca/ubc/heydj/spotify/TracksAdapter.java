package ca.ubc.heydj.spotify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.malinskiy.superrecyclerview.swipe.BaseSwipeAdapter;
import com.malinskiy.superrecyclerview.swipe.SwipeLayout;

import java.util.ArrayList;
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
public class TracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = TracksAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_SWIPE = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<SavedTrack> mTracks;
    private OnItemInteractionListener mOnItemInteractionListener = null;
    private MainApplication mMain;
    private List<Integer> mQueuedTracks;

    private boolean mIsPlaying = false;
    private int mCurrentSelectedIndex = -1;


    public TracksAdapter(Context context, List<SavedTrack> tracks) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTracks = tracks;
        this.mMain = (MainApplication) context.getApplicationContext();
        this.mQueuedTracks = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {

        if (mMain.isQueuing()) {
            return VIEW_TYPE_SWIPE;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {

            case VIEW_TYPE_NORMAL:
                viewHolder = new ViewHolder(mLayoutInflater.inflate(R.layout.track_list_item, parent, false));
                break;

            case VIEW_TYPE_SWIPE:
                viewHolder = new SwipeViewHolder(mLayoutInflater.inflate(R.layout.track_swipeable_list_item, parent, false));
                break;

        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final Track track = mTracks.get(position).track;

        switch (getItemViewType(position)) {

            case VIEW_TYPE_NORMAL:
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.trackTitle.setText(track.name);
                viewHolder.trackArtist.setText(track.artists.get(0).name);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemInteractionListener != null) {
                            mOnItemInteractionListener.onItemClick(position, track);
                        }
                    }
                });
                viewHolder.divider.setBackgroundResource(R.drawable.divider_blue_drawable);

                if (mCurrentSelectedIndex == position) {
                    viewHolder.playState.setVisibility(View.VISIBLE);
                    if (mIsPlaying) {
                        viewHolder.playState.setText(R.string.playing_text);
                    } else {
                        viewHolder.playState.setText(R.string.paused_text);
                    }
                } else {
                    viewHolder.playState.setVisibility(View.GONE);
                }

                break;

            case VIEW_TYPE_SWIPE:
                SwipeViewHolder swipeViewHolder = (SwipeViewHolder) holder;
                swipeViewHolder.trackTitle.setText(track.name);
                swipeViewHolder.trackArtist.setText(track.artists.get(0).name);
                swipeViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemInteractionListener != null) {
                            mOnItemInteractionListener.onItemClick(position, track);
                        }
                    }
                });
                swipeViewHolder.swipeLayout.setDragEdge(SwipeLayout.DragEdge.Left);
                swipeViewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
                swipeViewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                    @Override
                    public void onStartOpen(SwipeLayout layout) {

                    }

                    @Override
                    public void onOpen(SwipeLayout layout) {
                        if (mOnItemInteractionListener != null) {
                            mOnItemInteractionListener.onItemSwiped(position, track);
                        }
                    }

                    @Override
                    public void onStartClose(SwipeLayout layout) {

                    }

                    @Override
                    public void onClose(SwipeLayout layout) {

                    }

                    @Override
                    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                    }

                    @Override
                    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                    }
                });
                swipeViewHolder.divider.setBackgroundResource(R.drawable.divider_orange_drawable);

                if (mCurrentSelectedIndex == position) {
                    swipeViewHolder.playState.setVisibility(View.VISIBLE);
                    if (mIsPlaying) {
                        swipeViewHolder.playState.setText(R.string.playing_text);
                    } else {
                        swipeViewHolder.playState.setText(R.string.paused_text);
                    }
                } else {
                    swipeViewHolder.playState.setVisibility(View.GONE);
                }

                break;
        }
    }

    public void setTrackState(boolean isPlaying, int position) {
        mIsPlaying = isPlaying;
        mCurrentSelectedIndex = position;
        notifyDataSetChanged();
    }

    public void addQueuedTrack(int position, SavedTrack queuedTrack) {
        mTracks.add(position, queuedTrack);
        mQueuedTracks.add(position);
        notifyDataSetChanged();
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

    public void setOnItemClickListener(OnItemInteractionListener listener) {
        this.mOnItemInteractionListener = listener;
    }


    public static class SwipeViewHolder extends BaseSwipeAdapter.BaseSwipeableViewHolder {
        TextView trackTitle;
        TextView trackArtist;
        View divider;
        TextView playState;

        public SwipeViewHolder(View v) {
            super(v);
            trackTitle = (TextView) v.findViewById(R.id.track_title);
            trackArtist = (TextView) v.findViewById(R.id.track_artist);
            divider = v.findViewById(R.id.divider);
            playState = (TextView) v.findViewById(R.id.track_state);
        }
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

    public interface OnItemInteractionListener {
        void onItemClick(int position, Track selectedTrack);
        void onItemSwiped(int position, Track swipedTrack);
    }

}
