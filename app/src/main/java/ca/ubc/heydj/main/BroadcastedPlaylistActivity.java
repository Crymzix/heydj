package ca.ubc.heydj.main;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import ca.ubc.heydj.MainApplication;
import ca.ubc.heydj.R;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/14/2015.
 */
public class BroadcastedPlaylistActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    public static final String BROADCASTED_TRACKS_KEY = "broadcasted_tracks_key";
    public static final String BROADCASTER_ID_KEY = "broadcaster_id";

    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    private BroadcastedPlaylistAdapter mBroadcastedPlaylistAdapter;

    private String mHostId;
    private MainApplication mMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcastedplaylist);
        mMain = (MainApplication) getApplication();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Switch queuingSwitch = (Switch) mToolbar.findViewById(R.id.queue_switch);

        if (mMain.isQueuing()) {
            mToolbar.setBackgroundResource(R.drawable.toolbar_orange_gradient);
            queuingSwitch.setChecked(true);
        } else {
            mToolbar.setBackgroundResource(R.drawable.toolbar_blue_gradient);
            queuingSwitch.setChecked(false);
        }

        queuingSwitch.setOnCheckedChangeListener(this);
        List<Track> tracks = getIntent().getParcelableArrayListExtra(BROADCASTED_TRACKS_KEY);
        mHostId = getIntent().getStringExtra(BROADCASTER_ID_KEY);

        RecyclerView tracksRecyclerView = (RecyclerView) findViewById(R.id.tracks_recycler_view);
        mBroadcastedPlaylistAdapter = new BroadcastedPlaylistAdapter(this, tracks);
        mBroadcastedPlaylistAdapter.setIsQueuing(mMain.isQueuing());
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        tracksRecyclerView.setAdapter(mBroadcastedPlaylistAdapter);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Drawable backgrounds[] = new Drawable[2];

        if (isChecked) {
            mMain.setIsQueuing(true);
            mMain.setCurrentHostId(mHostId);
            mToolbarTitle.setText(R.string.queuing_title);
            mBroadcastedPlaylistAdapter.setIsQueuing(true);
            backgrounds[0] = ContextCompat.getDrawable(this, R.drawable.toolbar_blue_gradient);
            backgrounds[1] = ContextCompat.getDrawable(this, R.drawable.toolbar_orange_gradient);
        } else {
            mMain.setIsQueuing(false);
            mMain.setCurrentHostId(mHostId);
            mToolbarTitle.setText(R.string.local_playlist_title);
            mBroadcastedPlaylistAdapter.setIsQueuing(false);
            backgrounds[0] = ContextCompat.getDrawable(this, R.drawable.toolbar_orange_gradient);
            backgrounds[1] = ContextCompat.getDrawable(this, R.drawable.toolbar_blue_gradient);
        }

        TransitionDrawable transitionDrawable = new TransitionDrawable(backgrounds);
        mToolbar.setBackground(transitionDrawable);
        transitionDrawable.startTransition(1000);
    }

    @Override
    public void onBackPressed() {
        if (mMain.isQueuing()) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}
