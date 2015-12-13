package ca.ubc.heydj.nowplaying;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.api.Status;

import java.util.List;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.AudioFeedbackEvent;
import ca.ubc.heydj.events.AudioPlaybackEvent;
import ca.ubc.heydj.events.NearbyEvent;
import ca.ubc.heydj.services.NearbyService;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * Created by Chris Li on 12/12/2015.
 */
public class NowPlayingActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener{

    private static final String TAG = NowPlayingActivity.class.getSimpleName();

    public static final String CURRENT_TRACK_POSITION_KEY = "current_position_key";
    public static final String SAVED_TRACKS_KEY = "saved_tracks_key";

    private int mCurrentTrackIndex = 0;

    private FloatingActionButton mPlayButton;
    private FloatingActionButton mPauseButton;
    private ViewPager mTrackPager;
    private TracksPagerAdapter mTracksAdapter;

    private boolean mResolvingError = false;

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

        mPlayButton = (FloatingActionButton) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);
        mPauseButton = (FloatingActionButton) findViewById(R.id.pause_button);
        mPauseButton.setOnClickListener(this);
        FloatingActionButton nextButton = (FloatingActionButton) findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);
        FloatingActionButton previousButton = (FloatingActionButton) findViewById(R.id.previous_button);
        previousButton.setOnClickListener(this);
        ImageButton hostButton = (ImageButton) findViewById(R.id.host_button);
        hostButton.setOnClickListener(this);
        mTrackPager = (ViewPager) findViewById(R.id.track_pager);

        mCurrentTrackIndex = getIntent().getIntExtra(CURRENT_TRACK_POSITION_KEY, 0);
        List<SavedTrack> savedTracks = getIntent().getParcelableArrayListExtra(SAVED_TRACKS_KEY);

        mTracksAdapter = new TracksPagerAdapter(getSupportFragmentManager(), savedTracks);
        mTrackPager.setAdapter(mTracksAdapter);
        mTrackPager.setCurrentItem(mCurrentTrackIndex, true);
        mTrackPager.addOnPageChangeListener(this);

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
            EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PREVOUS));
        } else if (position > mCurrentTrackIndex) {
            EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.NEXT));
        }
        mCurrentTrackIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.play_button:
                EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PLAY_PAUSE));
                mPauseButton.setVisibility(View.VISIBLE);
                mPlayButton.setVisibility(View.INVISIBLE);
                break;

            case R.id.pause_button:
                EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PLAY_PAUSE));
                mPauseButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
                break;

            case R.id.next_button:

                int incrementIndex = mTrackPager.getCurrentItem();
                incrementIndex++;
                if (incrementIndex < mTracksAdapter.getCount()) {
                    mTrackPager.setCurrentItem(incrementIndex, true);
                }
                break;

            case R.id.previous_button:

                int decrementIndex = mTrackPager.getCurrentItem();
                decrementIndex--;
                if (decrementIndex >= 0 ) {
                    mTrackPager.setCurrentItem(decrementIndex, true);
                }
                break;

            case R.id.host_button:
                Intent nearbyService = new Intent(this, NearbyService.class);
                nearbyService.putExtra(NearbyService.IS_HOST_KEY, true);
                startService(nearbyService);
                break;

        }
    }

    /**
     * Updates UI based on Player state
     *
     * @param audioFeedbackEvent
     */
    public void onEvent(AudioFeedbackEvent audioFeedbackEvent) {

        if (mPlayButton != null && mPauseButton != null) {
            if (audioFeedbackEvent.getPlayerState().playing) {
                mPauseButton.setVisibility(View.VISIBLE);
                mPlayButton.setVisibility(View.INVISIBLE);
            } else {
                mPauseButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Displays the Nearby opt-in dialog when necessary.
     * Triggered by the NearbyHostService
     *
     * @param status
     */
    public void onEvent(Status status) {

        if (!mResolvingError) {
            try {
                status.startResolutionForResult(this,
                        NearbyService.REQUEST_NEARBY_RESOLVE_ERROR);
                mResolvingError = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, " failed with exception: " + e);
            }
        } else {
            Log.i(TAG, " failed with status: " + status
                    + " while resolving error.");
        }
    }

    // This is called in response to a button tap in the Nearby permission dialog.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NearbyService.REQUEST_NEARBY_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                EventBus.getDefault().post(new NearbyEvent());
            } else {
                // This may mean that user had rejected to grant nearby permission.
                Log.i(TAG, "Failed to resolve error with code " + resultCode);
            }
        }
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
    }

}
