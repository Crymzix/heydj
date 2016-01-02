package ca.ubc.heydj;

import android.app.Application;
import android.provider.Settings;

import ca.ubc.heydj.services.BuildMusicLibraryService;
import ca.ubc.heydj.services.SpotifyAudioPlaybackService;

/**
 * Singleton class that provides access to common objects
 * and methods used in the application.
 * <p/>
 * Created by Chris Li on 12/8/2015.
 */
public class MainApplication extends Application {

    private SpotifyAudioPlaybackService mSpotifyAudioService;
    private BuildMusicLibraryService mBuildMusicLibraryService;
    private String mSpotifyAccessToken;

    private boolean mIsBroadcasting = false;
    private boolean mIsQueuing = false;

    // Generated unique id for the device
    private String mUniqueID;

    // The id of the host to queue a song to
    private String mCurrentHostId;

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

    public boolean isQueuing() {
        return mIsQueuing;
    }

    public void setIsQueuing(boolean mIsListening) {
        this.mIsQueuing = mIsListening;
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

    public String getCurrentHostId() {
        return mCurrentHostId;
    }

    public void setCurrentHostId(String mCurrentHostId) {
        this.mCurrentHostId = mCurrentHostId;
    }

    public BuildMusicLibraryService getBuildMusicLibraryService() {
        return mBuildMusicLibraryService;
    }

    public void setBuildMusicLibraryService(BuildMusicLibraryService buildMusicLibraryService) {
        this.mBuildMusicLibraryService = buildMusicLibraryService;
    }
}
