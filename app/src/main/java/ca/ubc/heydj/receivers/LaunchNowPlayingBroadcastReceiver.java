package ca.ubc.heydj.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ca.ubc.heydj.main.MainActivity;

/**
 * Broadcast receivers for the notifications button events,
 * as this is the only way to react to those events as of now
 * <p/>
 * Created by Chris Li on 12/12/2015.
 */
public class LaunchNowPlayingBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        /*Intent nowPlayingIntent = new Intent(DispatchActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);*/
    }
}
