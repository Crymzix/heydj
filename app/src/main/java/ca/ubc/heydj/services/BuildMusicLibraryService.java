package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import ca.ubc.heydj.asynctasks.AsyncBuildLibraryTask;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class BuildMusicLibraryService extends Service implements AsyncBuildLibraryTask.AsyncBuildLibraryListener{

    private static final String TAG = BuildMusicLibraryService.class.getSimpleName();

    public static final String FINISHED_BUILDING_ACTION = "finished_building_action";

    private Context mContext;

    @Override
    public void onCreate() {
        mContext = this.getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext);
        task.setListener(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onFinished() {
        stopSelf();
        Log.i(TAG, "Finished building.");
    }
}
