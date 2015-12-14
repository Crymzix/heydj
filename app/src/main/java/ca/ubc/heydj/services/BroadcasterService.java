package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service class that takes care of broadcasting the playlist
 * and receiving changes to the playlist via the PubNub API.
 * <p/>
 * Created by Chris Li on 12/11/2015.
 */
public class BroadcasterService extends Service {

    private static final String TAG = BroadcasterService.class.getSimpleName();
    public static final String IS_HOST_KEY = "is_host_key";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);


        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
