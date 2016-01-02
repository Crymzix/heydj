package ca.ubc.heydj.nowplaying;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.heydj.BaseActivity;
import ca.ubc.heydj.R;
import ca.ubc.heydj.events.AudioFeedbackEvent;
import ca.ubc.heydj.events.AudioPlaybackEvent;
import ca.ubc.heydj.models.BroadcastedPlaylist;
import ca.ubc.heydj.events.PlayTrackEvent;
import ca.ubc.heydj.services.BroadcasterService;
import ca.ubc.heydj.services.SpotifyAudioPlaybackService;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * Activity to show currently playing track. Subscriver to AudioFeedbackEvents
 * delivered by the AudioPlaybackService.
 *
 * Created by Chris Li on 12/12/2015.
 */
public class NowPlayingActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = NowPlayingActivity.class.getSimpleName();

    public static final String CURRENT_TRACK_POSITION_KEY = "current_position_key";
    public static final String SAVED_TRACKS_KEY = "saved_tracks_key";

    private int mCurrentTrackIndex = 0;
    private List<SavedTrack> mCurrentTracks = null;

    private FloatingActionButton mPlayButton;
    private FloatingActionButton mPauseButton;
    private ViewPager mTrackPager;
    private TracksPagerAdapter mTracksAdapter;
    private ToggleButton mHostButton;
    private SeekBar mTrackBar;
    private TextView mTrackTitle;
    private TextView mTrackArtist;


    // use to make sure we publish a message right away when initiating broadcast
    private boolean mBroadcastStarted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_now_playing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTrackTitle = (TextView) toolbar.findViewById(R.id.track_title);
        mTrackArtist = (TextView) toolbar.findViewById(R.id.track_artist);

        mPlayButton = (FloatingActionButton) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);
        mPauseButton = (FloatingActionButton) findViewById(R.id.pause_button);
        mPauseButton.setOnClickListener(this);
        FloatingActionButton nextButton = (FloatingActionButton) findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);
        FloatingActionButton previousButton = (FloatingActionButton) findViewById(R.id.previous_button);
        previousButton.setOnClickListener(this);
        mHostButton = (ToggleButton) findViewById(R.id.host_button);
        mHostButton.setOnClickListener(this);
        mTrackPager = (ViewPager) findViewById(R.id.track_pager);
        mTrackBar = (SeekBar) findViewById(R.id.track_bar);
        mTrackBar.setProgress(0);
        mTrackBar.setOnSeekBarChangeListener(this);

        mCurrentTrackIndex = getIntent().getIntExtra(CURRENT_TRACK_POSITION_KEY, 0);
        mCurrentTracks = getIntent().getParcelableArrayListExtra(SAVED_TRACKS_KEY);

        mTracksAdapter = new TracksPagerAdapter(getSupportFragmentManager(), mCurrentTracks);
        mTrackPager.setAdapter(mTracksAdapter);
        mTrackPager.setCurrentItem(mCurrentTrackIndex, true);
        mTrackPager.addOnPageChangeListener(this);
        SavedTrack currentTrack = mCurrentTracks.get(mCurrentTrackIndex);
        mTrackTitle.setText(currentTrack.track.name);
        mTrackArtist.setText(currentTrack.track.artists.get(0).name);

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        if (position < mCurrentTrackIndex) {
            EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PREVIOUS));
        } else if (position > mCurrentTrackIndex) {
            EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.NEXT));
        }
        mCurrentTrackIndex = position;
        mTrackBar.setMax((int) mCurrentTracks.get(position).track.duration_ms);
        mTrackTitle.setText(mCurrentTracks.get(position).track.name);
        mTrackArtist.setText(mCurrentTracks.get(position).track.artists.get(0).name);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.play_button:

                if (mMain.getSpotifyAudioService() == null) {
                    if (mCurrentTracks != null) {
                        startService(new Intent(this, SpotifyAudioPlaybackService.class));
                        PlayTrackEvent playTrackEvent = new PlayTrackEvent();
                        playTrackEvent.setCurrentTrackIndex(mCurrentTrackIndex);
                        playTrackEvent.setUserTracks(mCurrentTracks);
                        EventBus.getDefault().postSticky(playTrackEvent);
                    }
                } else {
                    EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PLAY_PAUSE));
                    mPauseButton.setVisibility(View.VISIBLE);
                    mPlayButton.setVisibility(View.INVISIBLE);
                }

                break;

            case R.id.pause_button:

                if (mMain.getSpotifyAudioService() != null) {
                    EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PLAY_PAUSE));
                    mPauseButton.setVisibility(View.INVISIBLE);
                    mPlayButton.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.next_button:

                int incrementIndex = mTrackPager.getCurrentItem();
                incrementIndex++;
                if (incrementIndex < mTracksAdapter.getCount()) {
                    mTrackPager.setCurrentItem(incrementIndex, true);
                    mCurrentTrackIndex = incrementIndex;
                }
                break;

            case R.id.previous_button:

                int decrementIndex = mTrackPager.getCurrentItem();
                decrementIndex--;
                if (decrementIndex >= 0) {
                    mTrackPager.setCurrentItem(decrementIndex, true);
                    mCurrentTrackIndex = decrementIndex;
                }
                break;

            case R.id.host_button:
                if (((ToggleButton) v).isChecked()) {
                    mBroadcastStarted = true;
                    startService(new Intent(this, BroadcasterService.class));
                    connect();
                } else {
                    stopService(new Intent(this, BroadcasterService.class));
                    disconnect();
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if (getGoogleApiClient().isConnected() && mMain.isBroadcasting()) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }

    /**
     * Updates activity based on events from AudioPlaybackService
     *
     * @param audioFeedbackEvent
     */
    public void onEvent(AudioFeedbackEvent audioFeedbackEvent) {

        mCurrentTracks = audioFeedbackEvent.getPlaylist();
        if (mPlayButton != null && mPauseButton != null) {
            if (audioFeedbackEvent.getPlayerState().playing) {
                mPauseButton.setVisibility(View.VISIBLE);
                mPlayButton.setVisibility(View.INVISIBLE);
            } else {
                mPauseButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
            }
        }

        mTrackBar.setProgress(audioFeedbackEvent.getPlayerState().positionInMs);
        mTrackBar.setMax(audioFeedbackEvent.getPlayerState().durationInMs);

        if (audioFeedbackEvent.getPlayerState().playing) {

            BroadcastedPlaylist broadcastedPlaylist = new BroadcastedPlaylist();
            broadcastedPlaylist.playlist = audioFeedbackEvent.getTracks();
            broadcastedPlaylist.position_ms = audioFeedbackEvent.getPlayerState().positionInMs;
            broadcastedPlaylist.current_track_index = audioFeedbackEvent.getCurrentTrackIndex();
            broadcastedPlaylist.host_id = mMain.getUniqueID();

            switch (audioFeedbackEvent.getType()) {

                case AudioFeedbackEvent.STARTED:
                case AudioFeedbackEvent.TRACK_CHANGED:
                    mTrackPager.setCurrentItem(audioFeedbackEvent.getCurrentTrackIndex(), true);
                    mCurrentTrackIndex = audioFeedbackEvent.getCurrentTrackIndex();

                    if (mMain.isBroadcasting() && getGoogleApiClient().isConnected()) {
                        broadcastString(mGson.toJson(broadcastedPlaylist), null);
                    }
                    Log.i(TAG, "publish: currentTrackIndex: " + String.valueOf(broadcastedPlaylist.current_track_index));
                    break;
            }

            if (mBroadcastStarted) {
                if (mMain.isBroadcasting() && getGoogleApiClient().isConnected()) {
                    broadcastString(mGson.toJson(broadcastedPlaylist), null);
                }
                mBroadcastStarted = false;
                Log.i(TAG, "publish: currentTrackIndex: " + String.valueOf(broadcastedPlaylist.current_track_index));
            }
        }

        if (audioFeedbackEvent.getType() == AudioFeedbackEvent.TRACK_QUEUED) {
            mTracksAdapter.insertQueuedTrack(mTrackPager.getCurrentItem() + 1, audioFeedbackEvent.getQueuedTrack());
            mCurrentTracks.add(mTrackPager.getCurrentItem() + 1, audioFeedbackEvent.getQueuedTrack());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            AudioPlaybackEvent audioPlaybackEvent = new AudioPlaybackEvent(AudioPlaybackEvent.SCRUB);
            audioPlaybackEvent.setPositionInMs(progress);
            EventBus.getDefault().post(audioPlaybackEvent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private class TracksPagerAdapter extends FragmentStatePagerAdapter {

        private List<SavedTrack> mTracks;

        public TracksPagerAdapter(FragmentManager fm, List<SavedTrack> tracks) {
            super(fm);
            this.mTracks = tracks;
        }

        @Override
        public Fragment getItem(int position) {
            return TrackFragment.newInstance(mTracks.get(position).track);
        }

        @Override
        public int getCount() {
            return mTracks.size();
        }

        public void insertQueuedTrack(int position, SavedTrack savedTrack) {
            mTracks.add(position, savedTrack);
            notifyDataSetChanged();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.now_playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_queue) {
            Intent intent = new Intent(this, NowPlayingListActivity.class);
            intent.putParcelableArrayListExtra(SAVED_TRACKS_KEY, (ArrayList<? extends Parcelable>) mCurrentTracks);
            intent.putExtra(CURRENT_TRACK_POSITION_KEY, mCurrentTrackIndex);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
