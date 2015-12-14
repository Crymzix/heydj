package ca.ubc.heydj.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.nearby.messages.Message;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.heydj.BaseActivity;
import ca.ubc.heydj.R;
import ca.ubc.heydj.events.AudioFeedbackEvent;
import ca.ubc.heydj.events.AudioPlaybackEvent;
import ca.ubc.heydj.events.BroadcastPlaylistEvent;
import ca.ubc.heydj.events.NearbyEvent;
import ca.ubc.heydj.nowplaying.NowPlayingActivity;
import ca.ubc.heydj.services.AudioPlaybackService;
import ca.ubc.heydj.services.BroadcasterService;
import ca.ubc.heydj.spotify.SpotifyLibraryFragment;
import ca.ubc.heydj.utils.BlurTransformation;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;

public class MainActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SPOTIFY_ACCESS_TOKEN_KEY = "spotify_access_token";

    private static final int SPOTIFY_AUTH_REQUEST = 133;
    public static final int NOW_PLAYING_REQUEST = 124;


    private SharedPreferences mSharedPrefs;
    private String mSpotifyAccessToken = null;

    private FrameLayout mPlaybarContainer;
    private RelativeLayout mPlaybar;

    private int mCurrentTrackIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);

        mSharedPrefs = getSharedPreferences("ca.ubc.heydj", Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mPlaybarContainer = (FrameLayout) findViewById(R.id.play_bar_container);
        mPlaybar = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.play_bar_layout, null, false);
        mPlaybar.setOnClickListener(this);
        mPlaybar.findViewById(R.id.play_pause_toggle).setOnClickListener(this);

        createMenuItems();

        // Spotify Auth
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(getResources().getString(R.string.spotify_client_id),
                AuthenticationResponse.Type.TOKEN,
                getResources().getString(R.string.spotify_redirect_uri));
        builder.setScopes(new String[]{"user-read-private", "user-library-read", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_AUTH_REQUEST, request);
    }

    private void createMenuItems() {

        ListView menuListView = (ListView) findViewById(R.id.menu_items);
        menuListView.setOnItemClickListener(this);
        List<MenuItemsAdapter.MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItemsAdapter.MenuItem(R.drawable.ic_library, "Library"));
        menuItems.add(new MenuItemsAdapter.MenuItem(R.drawable.ic_hosts, "Nearby Hosts"));
        menuListView.setAdapter(new MenuItemsAdapter(this, menuItems));

    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, BroadcasterService.class));
        stopService(new Intent(this, AudioPlaybackService.class));
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPOTIFY_AUTH_REQUEST) {
            if (resultCode == RESULT_OK) {
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
                if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                    mSpotifyAccessToken = response.getAccessToken();

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new SpotifyLibraryFragment())
                            .commit();

                    Intent audioPlayService = new Intent(this, AudioPlaybackService.class);
                    audioPlayService.putExtra(SPOTIFY_ACCESS_TOKEN_KEY, mSpotifyAccessToken);
                    startService(audioPlayService);
                }
            } else {
                Log.e(TAG, "Authentication failed.");
            }
        }

        // Reconnect if returning from NowPlayingActivity and broadcasting was enabled.
        if (requestCode == NOW_PLAYING_REQUEST) {
            if (resultCode == RESULT_OK) {
                connect();
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {

            case 0:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new SpotifyLibraryFragment(), SpotifyLibraryFragment.TAG)
                        .commit();
                break;

            case 1:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new NearbyBroadcastersFragment(), NearbyBroadcastersFragment.TAG)
                        .commit();
                break;

            case 2:

                break;

            default:

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    public String getSpotifyAccessToken() {
        return mSpotifyAccessToken;
    }

    /**
     * Subscribe to events pertaining to Nearby APIs
     *
     * @param nearbyEvent
     */
    public void onEvent(NearbyEvent nearbyEvent) {

        switch (nearbyEvent.getType()) {

            case NearbyEvent.CONNECT:
                connect();
                break;

            case NearbyEvent.DISCONNECT:
                disconnect();
                break;

        }
    }

    /**
     * Updates activity based on events from AudioPlaybackService
     *
     * @param audioFeedbackEvent
     */
    public void onEvent(final AudioFeedbackEvent audioFeedbackEvent) {

        mCurrentTrackIndex = audioFeedbackEvent.getCurrentTrackIndex();
        ToggleButton playPauseButton = (ToggleButton) mPlaybar.findViewById(R.id.play_pause_toggle);
        if (audioFeedbackEvent.getPlayerState().playing) {
            if (mPlaybarContainer.getChildCount() == 0) {
                mPlaybarContainer.addView(mPlaybar);
            } else {
                playPauseButton.setChecked(true);
            }
        } else {
            playPauseButton.setChecked(false);
        }

        Track currentTrack = audioFeedbackEvent.getTracks().get(audioFeedbackEvent.getCurrentTrackIndex());
        ((TextView) mPlaybar.findViewById(R.id.track_title)).setText(currentTrack.name);
        ((TextView) mPlaybar.findViewById(R.id.track_artist)).setText(currentTrack.artists.get(0).name);
        Picasso.with(this)
                .load(currentTrack.album.images.get(0).url)
                .transform(new BlurTransformation(this))
                .fit()
                .centerCrop()
                .into((ImageView) mPlaybar.findViewById(R.id.track_album_image));
        mPlaybar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
                intent.putExtra(NowPlayingActivity.CURRENT_TRACK_POSITION_KEY, mCurrentTrackIndex);
                intent.putParcelableArrayListExtra(NowPlayingActivity.SAVED_TRACKS_KEY, (ArrayList<? extends Parcelable>) audioFeedbackEvent.getPlaylist());
                startActivityForResult(intent, NOW_PLAYING_REQUEST);
            }
        });

        if (mMain.isBroadcasting() && getGoogleApiClient().isConnected() && audioFeedbackEvent.getPlayerState().playing) {
            BroadcastPlaylistEvent broadcastPlaylistEvent = new BroadcastPlaylistEvent();
            broadcastPlaylistEvent.playlist = audioFeedbackEvent.getTracks();
            broadcastPlaylistEvent.position_ms = audioFeedbackEvent.getPlayerState().positionInMs;
            broadcastPlaylistEvent.current_track_index = audioFeedbackEvent.getCurrentTrackIndex();
            broadcastPlaylistEvent.host_id = mMain.getUniqueID();
            broadcastString(mGson.toJson(broadcastPlaylistEvent));
        }
    }

    @Override
    protected void onMessageFound(Message message) {
        super.onMessageFound(message);
        NearbyBroadcastersFragment nearbyBroadcastersFragment = (NearbyBroadcastersFragment) getSupportFragmentManager().findFragmentByTag(NearbyBroadcastersFragment.TAG);

        // if this fragment is currently being displayed, route the message to it
        if (nearbyBroadcastersFragment != null) {
            String jsonString = new String(message.getContent());
            nearbyBroadcastersFragment.onBroadcastPlaylistEvent(mGson.fromJson(jsonString, BroadcastPlaylistEvent.class));
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.play_pause_toggle:
                EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.PLAY_PAUSE));
                break;
        }

    }
}
