package ca.ubc.heydj.main;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

import ca.ubc.heydj.R;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/14/2015.
 */
public class BroadcastedPlaylistActivity extends AppCompatActivity {

    public static final String BROADCASTED_TRACKS_KEY = "broadcasted_tracks_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcastedplaylist);

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

        List<Track> tracks = getIntent().getParcelableArrayListExtra(BROADCASTED_TRACKS_KEY);
        RecyclerView tracksRecyclerView = (RecyclerView) findViewById(R.id.tracks_recycler_view);
        BroadcastedPlaylistAdapter trackAdapter = new BroadcastedPlaylistAdapter(this, tracks);
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        tracksRecyclerView.setAdapter(trackAdapter);

    }
}
