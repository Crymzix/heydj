package ca.ubc.heydj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ca.ubc.heydj.main.MainActivity;
import ca.ubc.heydj.services.BuildMusicLibraryService;

/**
 * NOTE: Not attached at the moment
 * <p/>
 * Created by Chris Li on 12/11/2015.
 */
public class LauncherActivity extends AppCompatActivity {

    // key to check if the user has built their library
    private static final String START_SCAN_PREF = "start_scan";

    private BuildLibraryReceiver mBuildLibraryReceiver;
    private SharedPreferences mSharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = getSharedPreferences("ca.ubc.heydj", Context.MODE_PRIVATE);
        if (!mSharedPrefs.getBoolean(START_SCAN_PREF, false)) {
            startService(new Intent(this, BuildMusicLibraryService.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mSharedPrefs.getBoolean(START_SCAN_PREF, false)) {
            if (mBuildLibraryReceiver == null) {
                mBuildLibraryReceiver = new BuildLibraryReceiver();
            }
            IntentFilter intentFilter = new IntentFilter(BuildMusicLibraryService.FINISHED_BUILDING_ACTION);
            registerReceiver(mBuildLibraryReceiver, intentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mBuildLibraryReceiver != null) {
            unregisterReceiver(mBuildLibraryReceiver);
        }
    }

    private class BuildLibraryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //if (intent.getAction().equals(RefreshTask.REFRESH_DATA_INTENT)) {

            //}
            mSharedPrefs.edit()
                    .putBoolean(START_SCAN_PREF, true)
                    .apply();
            startActivity(new Intent(context, MainActivity.class));
        }
    }
}
