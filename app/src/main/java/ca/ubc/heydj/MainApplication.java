package ca.ubc.heydj;

import android.app.Application;
import android.provider.Settings;

import ca.ubc.heydj.spotify.SpotifyAudioPlaybackService;

/**
 * Singleton class that provides access to common objects
 * and methods used in the application.
 * <p/>
 * Created by Chris Li on 12/8/2015.
 */
public class MainApplication extends Application {

    private SpotifyAudioPlaybackService mSpotifyAudioService;
    private String mSpotifyAccessToken;

    private boolean mIsBroadcasting = false;
    private boolean mIsListening = false;
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

    public boolean isListening() {
        return mIsListening;
    }

    public void setIsListening(boolean mIsListening) {
        this.mIsListening = mIsListening;
    }

    public SpotifyAudioPlaybackService getSpotifyAudioService() {
        return mSpotifyAudioService;
    }

    public void setSpotifyAudioService(SpotifyAudioPlaybackService mSpotifyAudioService) {
        this.mSpotifyAudioService = mSpotifyAudioService;
    }

    public String getSpotifyAccessToken() {
        return mSpotifyAccessToken;
    }

    public void setSpotifyAccessToken(String mSpotifyAccessToken) {
        this.mSpotifyAccessToken = mSpotifyAccessToken;
    }
}
