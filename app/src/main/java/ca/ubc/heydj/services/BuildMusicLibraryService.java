package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import ca.ubc.heydj.MainApplication;
import ca.ubc.heydj.asynctasks.AsyncBuildLibraryTask;
import ca.ubc.heydj.events.BuildLibraryEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class BuildMusicLibraryService extends Service implements AsyncBuildLibraryTask.AsyncBuildLibraryListener{

    private static final String TAG = BuildMusicLibraryService.class.getSimpleName();

    public static final String FINISHED_BUILDING_ACTION = "finished_building_action";

    private Context mContext;
    private MainApplication mMain;

    @Override
    public void onCreate() {
        mContext = this.getApplicationContext();
        mMain = (MainApplication) this.getApplication();
        mMain.setBuildMusicLibraryService(this);
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
        EventBus.getDefault().post(new BuildLibraryEvent(BuildLibraryEvent.FINISHED));
        stopSelf();
        Log.i(TAG, "Finished building.");
    }

}
