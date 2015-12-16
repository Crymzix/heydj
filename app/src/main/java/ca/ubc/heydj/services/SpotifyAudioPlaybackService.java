package ca.ubc.heydj.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayConfig;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.heydj.MainApplication;
import ca.ubc.heydj.R;
import ca.ubc.heydj.events.AudioFeedbackEvent;
import ca.ubc.heydj.events.AudioPlaybackEvent;
import ca.ubc.heydj.events.PlayTrackEvent;
import ca.ubc.heydj.nowplaying.NowPlayingActivity;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * Service used to play audio in the background.
 * <p/>
 * Created by Chris Li on 12/11/2015.
 */
public class SpotifyAudioPlaybackService extends Service implements PlayerNotificationCallback, ConnectionStateCallback, PlayerStateCallback {

    private static final String TAG = SpotifyAudioPlaybackService.class.getSimpleName();

    public static final int mNotificationId = 1156;
    private NotificationManager mNotificationManager;

    public static final String LAUNCH_NOW_PLAYING_ACTION = "ca.ubc.heydj.player.LAUNCH_NOW_PLAYING_ACTION";
    public static final String PREVIOUS_ACTION = "ca.ubc.heydj.player.PREVIOUS_ACTION";
    public static final String PLAY_PAUSE_ACTION = "ca.ubc.heydj.player.PLAY_PAUSE_ACTION";
    public static final String NEXT_ACTION = "ca.ubc.heydj.player.NEXT_ACTION";
    public static final String STOP_SERVICE = "ca.ubc.heydj.player.STOP_SERVICE";

    private Player mPlayer;

    private boolean mFirstRun = true;
    private boolean mIsPlaying = false;

    private Context mContext;
    private MainApplication mMain;

    private Handler mHandler;
    private Runnable mRunnable;

    private List<String> mPlaylist;
    private String mCurrentTrackUri = null;
    private int mCurrentTrackIndex = 0;
    private SavedTrack mCurrentTrack = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mMain = (MainApplication) getApplication();
        mHandler = new Handler();
        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        EventBus.getDefault().register(this);
        mMain.setSpotifyAudioService(this);
        mFirstRun = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String spotifyAccessToken = mMain.getSpotifyAccessToken();
            if (spotifyAccessToken != null && mFirstRun) {
                setupSpotifyPlayer(spotifyAccessToken);
            }
        } else {
            Log.i(TAG, "No new intent data, service already started.");
        }

        return START_STICKY;
    }

    private void setupSpotifyPlayer(String spotifyAccessToken) {
        Config playerConfig = new Config(this, spotifyAccessToken, getResources().getString(R.string.spotify_client_id));
        Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer = player;
                mPlayer.addConnectionStateCallback(SpotifyAudioPlaybackService.this);
                mPlayer.addPlayerNotificationCallback(SpotifyAudioPlaybackService.this);
                mPlayer.getPlayerState(SpotifyAudioPlaybackService.this);
                mFirstRun = false;
                Log.i(TAG, "Spotify player initialized");

                // In the case that this service was created AFTER the event was posted
                PlayTrackEvent playTrackEvent = EventBus.getDefault().removeStickyEvent(PlayTrackEvent.class);
                if (playTrackEvent != null) {
                    playSpotifyTrack(playTrackEvent);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    public void onEvent(PlayTrackEvent playTrackEvent) {
        playSpotifyTrack(playTrackEvent);
    }

    public void onEvent(AudioPlaybackEvent audioPlaybackEvent) {

        //Update the current notification.
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        switch (audioPlaybackEvent.getEventType()) {

            case AudioPlaybackEvent.STOP:
                // Update the UI if the service was stopped via the notification close button
                AudioFeedbackEvent audioFeedbackEvent = new AudioFeedbackEvent();
                audioFeedbackEvent.setType(AudioFeedbackEvent.STOPPED);
                EventBus.getDefault().post(audioFeedbackEvent);
                mNotificationManager.cancel(mNotificationId);
                stopSelf();

                break;

            case AudioPlaybackEvent.PLAY_PAUSE:
                if (mIsPlaying) {
                    mPlayer.pause();
                    mIsPlaying = false;
                } else {
                    mPlayer.resume();
                    mIsPlaying = true;
                }

                mNotificationManager.notify(mNotificationId, buildNotification());

                break;

            case AudioPlaybackEvent.NEXT:
                mPlayer.skipToNext();
                if (mCurrentTrackIndex + 1 < mPlaylist.size()) {
                    mCurrentTrackIndex++;
                }
                mNotificationManager.notify(mNotificationId, buildNotification());

                break;

            case AudioPlaybackEvent.PREVIOUS:
                mPlayer.skipToPrevious();
                if (mCurrentTrackIndex - 1 >= 0) {
                    mCurrentTrackIndex--;
                }
                mNotificationManager.notify(mNotificationId, buildNotification());

                break;

            case AudioPlaybackEvent.SCRUB:
                mPlayer.seekToPosition(audioPlaybackEvent.getPositionInMs());
                break;
        }
    }

    /**
     * Plays a track in a given playlist
     *
     * @param playTrackEvent
     */
    private void playSpotifyTrack(final PlayTrackEvent playTrackEvent) {

        // Create list of Spotify URIs and pass it to the player
        mPlaylist = new ArrayList<>();
        for (SavedTrack savedTrack : playTrackEvent.getUserTracks()) {
            mPlaylist.add("spotify:track:" + savedTrack.track.id);
        }

        PlayConfig playConfig = PlayConfig.createFor(mPlaylist);
        playConfig.withTrackIndex(playTrackEvent.getCurrentTrackIndex());
        mCurrentTrackIndex = playTrackEvent.getCurrentTrackIndex();
        mCurrentTrack = playTrackEvent.getUserTracks().get(mCurrentTrackIndex);

        if (mPlayer != null) {

            mHandler.removeCallbacks(mRunnable);

            // Repeatedly broadcast player state to (internal) subscribers
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    mPlayer.getPlayerState(new PlayerStateCallback() {
                        @Override
                        public void onPlayerState(PlayerState playerState) {
                            AudioFeedbackEvent audioFeedbackEvent = new AudioFeedbackEvent();
                            audioFeedbackEvent.setPlayerState(playerState);
                            audioFeedbackEvent.setCurrentTrackIndex(mCurrentTrackIndex);
                            audioFeedbackEvent.setPlaylist(playTrackEvent.getUserTracks());

                            if (playerState.playing) {
                                audioFeedbackEvent.setType(AudioFeedbackEvent.PLAYING);
                                if (mCurrentTrackUri == null) {
                                    audioFeedbackEvent.setType(AudioFeedbackEvent.STARTED);
                                } else if (!mCurrentTrackUri.equals(playerState.trackUri)) {
                                    // Manually sending a track changed event
                                    if (mPlaylist.indexOf(playerState.trackUri) < mCurrentTrackIndex) {
                                        mCurrentTrackIndex--;
                                    } else if (mPlaylist.indexOf(playerState.trackUri) > mCurrentTrackIndex) {
                                        mCurrentTrackIndex++;
                                    }
                                    audioFeedbackEvent.setType(AudioFeedbackEvent.TRACK_CHANGED);
                                }
                                mCurrentTrackUri = playerState.trackUri;
                            }

                            EventBus.getDefault().post(audioFeedbackEvent);
                            mHandler.postDelayed(mRunnable, 1000);
                        }
                    });
                }
            };

            mHandler.post(mRunnable);
            mIsPlaying = true;
            mPlayer.play(playConfig);
            startForeground(mNotificationId, buildNotification());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Spotify.destroyPlayer(this);
        EventBus.getDefault().unregister(this);
        mMain.setSpotifyAudioService(null);
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }

    @Override
    public void onLoggedIn() {
        Log.i(TAG, "Spotify client logged in.");
    }

    @Override
    public void onLoggedOut() {
        Log.i(TAG, "Spotify client logged out.");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.i(TAG, "onPlaybackEvent: " + playerState.playing);
        Log.i(TAG, playerState.trackUri);
        Log.i(TAG, "position in MS:" + playerState.positionInMs);
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    @Override
    public void onPlayerState(PlayerState playerState) {
        Log.i(TAG, "onPlayerState: " + playerState.playing);
    }

    private Notification buildNotification() {

        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(mContext);
        mNotificationBuilder.setColor(Color.BLACK);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setAutoCancel(false);
        mNotificationBuilder.setSmallIcon(R.drawable.notif_icon);

        //Open up the play screen when the user taps on the notification.
        Intent launchNowPlayingIntent = new Intent();
        launchNowPlayingIntent.setAction(SpotifyAudioPlaybackService.LAUNCH_NOW_PLAYING_ACTION);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, launchNowPlayingIntent, 0);
        mNotificationBuilder.setContentIntent(launchNowPlayingPendingIntent);

        //Grab the notification layouts.
        RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);
        RemoteViews expNotificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_expanded_layout);

        //Initialize the notification layout buttons.
        Intent previousTrackIntent = new Intent();
        previousTrackIntent.setAction(SpotifyAudioPlaybackService.PREVIOUS_ACTION);
        PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, previousTrackIntent, 0);

        Intent playPauseTrackIntent = new Intent();
        playPauseTrackIntent.setAction(SpotifyAudioPlaybackService.PLAY_PAUSE_ACTION);
        PendingIntent playPauseTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, playPauseTrackIntent, 0);

        Intent nextTrackIntent = new Intent();
        nextTrackIntent.setAction(SpotifyAudioPlaybackService.NEXT_ACTION);
        PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, nextTrackIntent, 0);

        Intent stopServiceIntent = new Intent();
        stopServiceIntent.setAction(SpotifyAudioPlaybackService.STOP_SERVICE);
        PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, stopServiceIntent, 0);

        //Check if audio is playing and set the appropriate play/pause button.
        if (mIsPlaying) {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_pause_light);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_pause_light);
        } else {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_play_light);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
        }

        //Set the notification content.
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_one, mCurrentTrack.track.name);
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_two, mCurrentTrack.track.artists.get(0).name);
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_three, mCurrentTrack.track.album.name);

        notificationView.setTextViewText(R.id.notification_base_line_one, mCurrentTrack.track.name);
        notificationView.setTextViewText(R.id.notification_base_line_two, mCurrentTrack.track.artists.get(0).name);

        //We're smack dab in the middle of the queue, so keep the previous and next buttons enabled.
        expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.VISIBLE);
        expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.VISIBLE);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, previousTrackPendingIntent);

        notificationView.setViewVisibility(R.id.notification_base_previous, View.VISIBLE);
        notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);
        notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_previous, previousTrackPendingIntent);

        //Set the "Stop Service" pending intents.
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, stopServicePendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_collapse, stopServicePendingIntent);

        //Attach the shrunken layout to the notification.
        mNotificationBuilder.setContent(notificationView);

        //Build the notification object.
        Notification notification = mNotificationBuilder.build();

        //Attach the expanded layout to the notification and set its flags.
        notification.bigContentView = expNotificationView;
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE |
                Notification.FLAG_NO_CLEAR |
                Notification.FLAG_ONGOING_EVENT;

        //Set the album art.
        Picasso.with(this)
                .load(mCurrentTrack.track.album.images.get(2).url)
                .into(expNotificationView, R.id.notification_expanded_base_image, mNotificationId, notification);
        Picasso.with(this)
                .load(mCurrentTrack.track.album.images.get(2).url)
                .into(notificationView, R.id.notification_base_image, mNotificationId, notification);

        return notification;
    }

}
