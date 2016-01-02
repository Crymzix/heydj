package ca.ubc.heydj.nowplaying;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ca.ubc.heydj.R;
import ca.ubc.heydj.spotify.TracksAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * Created by Chris Li on 1/2/2016.
 */
public class NowPlayingListActivity extends AppCompatActivity {

    private CircleImageView mTrackImage;
    private TextView mTrackTitle;
    private TextView mTrackArtist;

    private RecyclerView mTracksRecyclerView;
    private TracksAdapter mTracksAdapter;

    private List<SavedTrack> mCurrentTracks;
    private int mCurrentTrackIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing_list);

        mCurrentTrackIndex = getIntent().getIntExtra(NowPlayingActivity.CURRENT_TRACK_POSITION_KEY, 0);
        mCurrentTracks = getIntent().getParcelableArrayListExtra(NowPlayingActivity.SAVED_TRACKS_KEY);

        mTrackImage = (CircleImageView) findViewById(R.id.album_image);
        mTrackTitle = (TextView) findViewById(R.id.track_title);
        mTrackArtist = (TextView) findViewById(R.id.track_artist);

        mTracksRecyclerView = (RecyclerView) findViewById(R.id.queue_list);
        mTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mTracksAdapter = new TracksAdapter(this, mCurrentTracks.subList(mCurrentTrackIndex + 1, mCurrentTracks.size()), true);
        mTracksRecyclerView.setAdapter(mTracksAdapter);

        SavedTrack currentTrack = mCurrentTracks.get(mCurrentTrackIndex);
        Picasso.with(this)
                .load(currentTrack.track.album.images.get(0).url)
                .fit()
                .centerCrop()
                .into(mTrackImage);

        mTrackTitle.setText(currentTrack.track.name);
        mTrackArtist.setText(currentTrack.track.artists.get(0).name);
    }
}
