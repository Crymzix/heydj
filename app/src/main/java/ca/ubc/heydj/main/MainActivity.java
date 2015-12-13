package ca.ubc.heydj.main;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.api.Status;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.NearbyEvent;
import ca.ubc.heydj.services.NearbyHostService;
import ca.ubc.heydj.spotify.SpotifyLibraryFragment;
import ca.ubc.heydj.services.AudioPlaybackService;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SPOTIFY_ACCESS_TOKEN_KEY = "spotify_access_token";

    private static final int SPOTIFY_REQUEST_CODE = 1331;

    private SharedPreferences mSharedPrefs;
    private String mSpotifyAccessToken = null;

    private boolean mResolvingError = false;

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

        createMenuItems();

        // Spotify Auth
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(getResources().getString(R.string.spotify_client_id),
                AuthenticationResponse.Type.TOKEN,
                getResources().getString(R.string.spotify_redirect_uri));
        builder.setScopes(new String[]{"user-read-private", "user-library-read", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);
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
        stopService(new Intent(this, NearbyHostService.class));
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

        if (resultCode == RESULT_OK && requestCode == SPOTIFY_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mSpotifyAccessToken = response.getAccessToken();
                Intent audioPlayService = new Intent(this, AudioPlaybackService.class);
                audioPlayService.putExtra(SPOTIFY_ACCESS_TOKEN_KEY, mSpotifyAccessToken);
                startService(audioPlayService);
            }
        }

        if (resultCode == RESULT_OK && requestCode == NearbyHostService.REQUEST_NEARBY_RESOLVE_ERROR) {
            mResolvingError = false;
            EventBus.getDefault().post(new NearbyEvent());
        } else {
            // This may mean that user had rejected to grant nearby permission.
            Log.i(TAG, "Failed to resolve error with code " + resultCode);
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
                        NearbyHostService.REQUEST_NEARBY_RESOLVE_ERROR);
                mResolvingError = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, " failed with exception: " + e);
            }
        } else {
            Log.i(TAG, " failed with status: " + status
                    + " while resolving error.");
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {

            case 0:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new SpotifyLibraryFragment())
                        .commit();
                break;

            case 1:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new NearbyHostsFragment())
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

}
