package ca.ubc.heydj.nowplaying;

import android.content.Intent;
import android.graphics.Color;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.spotify.sdk.android.player.PlayerState;

import java.util.List;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.AudioPlaybackEvent;
import ca.ubc.heydj.services.NearbyHostService;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * Created by Chris Li on 12/12/2015.
 */
public class NowPlayingActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener{

    public static final String CURRENT_TRACK_POSITION_KEY = "current_position_key";
    public static final String SAVED_TRACKS_KEY = "saved_tracks_key";

    private int mCurrentTrackIndex = 0;

    private FloatingActionButton mPlayButton;
    private FloatingActionButton mPauseButton;

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
        ViewPager trackPager = (ViewPager) findViewById(R.id.track_pager);

        mCurrentTrackIndex = getIntent().getIntExtra(CURRENT_TRACK_POSITION_KEY, 0);
        List<SavedTrack> savedTracks = getIntent().getParcelableArrayListExtra(SAVED_TRACKS_KEY);

        trackPager.setAdapter(new TracksPagerAdapter(getSupportFragmentManager(), savedTracks));
        trackPager.setCurrentItem(mCurrentTrackIndex, true);
        trackPager.addOnPageChangeListener(this);

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
        } else {
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
                EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.NEXT));
                break;

            case R.id.previous_button:
                EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PREVOUS));
                break;

            case R.id.host_button:
                //startService(new Intent(getApplicationContext(), NearbyHostService.class));
                //EventBus.getDefault().postSticky();
                break;

        }
    }

    // Update UI based on player state
    public void onEvent(PlayerState playerState) {

        if (mPlayButton != null && mPauseButton != null) {
            if (playerState.playing) {
                mPauseButton.setVisibility(View.VISIBLE);
                mPlayButton.setVisibility(View.INVISIBLE);
            } else {
                mPauseButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
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
