package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class BuildMusicLibraryService extends Service {

    public static final String FINISHED_BUILDING_ACTION = "finished_building_action";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
