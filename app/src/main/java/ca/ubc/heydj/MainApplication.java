package ca.ubc.heydj;

import android.app.Application;
import android.provider.Settings;

/**
 * Singleton class that provides access to common objects
 * and methods used in the application.
 * <p/>
 * Created by Chris Li on 12/8/2015.
 */
public class MainApplication extends Application {

    private boolean mIsBroadcasting = false;
    private String mUniqueID;

    @Override
    public void onCreate() {
        super.onCreate();
        mUniqueID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public boolean isBroadcasting() {
        return mIsBroadcasting;
    }

    public void setIsBroadcasting(boolean mIsBroadcasting) {
        this.mIsBroadcasting = mIsBroadcasting;
    }

    public String getUniqueID() {
        return mUniqueID;
    }
}
