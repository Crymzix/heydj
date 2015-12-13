package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import ca.ubc.heydj.events.AudioFeedbackEvent;
import ca.ubc.heydj.events.BroadcastPlaylistEvent;
import ca.ubc.heydj.events.NearbyEvent;
import de.greenrobot.event.EventBus;

/**
 * Service class that takes care of broadcasting the playlist
 * and receiving changes to the playlist.
 *
 * Created by Chris Li on 12/11/2015.
 */
public class NearbyService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = NearbyService.class.getSimpleName();
    public static final String IS_HOST_KEY = "is_host_key";
    public static final int REQUEST_NEARBY_RESOLVE_ERROR = 131;

    private GoogleApiClient mGoogleApiClient;
    private MessageListener mMessageListener;
    private Message mMessage;
    private Gson mGson;

    private boolean mIsHost = false;
    private String mUniqueHostId;
    private Set<String> mHostIds;
    private Set<String> mSubscriberIds;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mGson = new Gson();
        mHostIds = new HashSet<>();
        mSubscriberIds = new HashSet<>();

        mUniqueHostId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.i(TAG, "Received: " + message.describeContents());
                String jsonMessage = new String (message.getContent());
                BroadcastPlaylistEvent receivedBroadcastPlaylist = mGson.fromJson(jsonMessage, BroadcastPlaylistEvent.class);
                if (!mHostIds.contains(receivedBroadcastPlaylist.host_id)) {
                    mHostIds.add(receivedBroadcastPlaylist.host_id);
                    EventBus.getDefault().post(receivedBroadcastPlaylist);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            mIsHost = intent.getBooleanExtra(IS_HOST_KEY, false);
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {

        if (mGoogleApiClient.isConnected()) {
            // Clean up when the service is destroyed
            if (mIsHost) {
                Nearby.Messages.unpublish(mGoogleApiClient, mMessage)
                        .setResultCallback(new ErrorCheckingCallback("unpublish()"));
            }
            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                    .setResultCallback(new ErrorCheckingCallback("unsubscribe()"));
        }

        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleClient connected");
        Nearby.Messages.getPermissionStatus(mGoogleApiClient).setResultCallback(
                new ErrorCheckingCallback("getPermissionStatus", new Runnable() {
                    @Override
                    public void run() {
                        subscribe();
                    }
                })
        );

    }

    private void publishPlaylist(String broadcastString) {

        mMessage = new Message(broadcastString.getBytes());
        Nearby.Messages.publish(mGoogleApiClient, mMessage)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "publish: " + status.describeContents());
                    }
                });
    }

    public void subscribe() {
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "subscribe: " + status.describeContents());
                    }
                });
    }


    public void onEvent(NearbyEvent nearbyEvent) {
        subscribe();
    }

    /**
     * Broadcast the playlist to all nearby devices
     *
     * @param audioFeedbackEvent
     */
    public void onEvent(AudioFeedbackEvent audioFeedbackEvent) {

        if (audioFeedbackEvent.getPlayerState().playing && mIsHost) {

            BroadcastPlaylistEvent broadcastPlaylistEvent = new BroadcastPlaylistEvent();
            broadcastPlaylistEvent.current_track_index = audioFeedbackEvent.getCurrentTrackIndex();
            broadcastPlaylistEvent.host_id = mUniqueHostId;
            broadcastPlaylistEvent.duration_ms = audioFeedbackEvent.getPlayerState().positionInMs;
            broadcastPlaylistEvent.playlist = audioFeedbackEvent.getTracks();

            String broadcastJSONString = mGson.toJson(broadcastPlaylistEvent);
            publishPlaylist(broadcastJSONString);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /**
     * A simple ResultCallback that logs when errors occur.
     * It also triggers the Nearby opt-in dialog when necessary.
     */
    private class ErrorCheckingCallback implements ResultCallback<Status> {
        private final String method;
        private final Runnable runOnSuccess;

        private ErrorCheckingCallback(String method) {
            this(method, null);
        }

        private ErrorCheckingCallback(String method, @Nullable Runnable runOnSuccess) {
            this.method = method;
            this.runOnSuccess = runOnSuccess;
        }

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, method + " succeeded.");
                if (runOnSuccess != null) {
                    runOnSuccess.run();
                }
            } else {
                // Currently, the only resolvable error is that the device is not opted
                // in to Nearby. Starting the resolution displays an opt-in dialog.
                if (status.hasResolution()) {
                    EventBus.getDefault().post(status);
                } else {
                    Log.e(TAG, method + " failed with : " + status);
                }
            }
        }
    }


}
