package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import java.util.Random;
import java.util.UUID;

import ca.ubc.heydj.events.NearbyEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class NearbyHostService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = NearbyHostService.class.getSimpleName();
    public static final int REQUEST_NEARBY_RESOLVE_ERROR = 131;

    private GoogleApiClient mGoogleApiClient;
    private MessageListener mMessageListener;
    private Message mMessage;

    private Handler mHandler;
    private Runnable mRunnable;

    private String uniqueHostId;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mHandler = new Handler();

        uniqueHostId = UUID.randomUUID().toString();

        String testVar = " Hello";
        mMessage = new Message(testVar.getBytes());
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String stringMsg = new String(message.getContent());
                Log.i(TAG, "Received: " + stringMsg);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(TAG, "Started");

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {

        if (mGoogleApiClient.isConnected()) {
            // Clean up when the service is destroyed
            Nearby.Messages.unpublish(mGoogleApiClient, mMessage)
                    .setResultCallback(new ErrorCheckingCallback("unpublish()"));
            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                    .setResultCallback(new ErrorCheckingCallback("unsubscribe()"));
        }

        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacks(mRunnable);
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
                        publishPlaylist();
                    }
                })
        );

    }

    private void publishPlaylist() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                String testVar = " Hello";
                mMessage = new Message(testVar.getBytes());
                Nearby.Messages.publish(mGoogleApiClient, mMessage)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                Log.i(TAG, "publish: " + status.describeContents());
                                mHandler.postDelayed(mRunnable, 1000);
                            }
                        });
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "subscribe: " + status.describeContents());
                    }
                });
    }


    public void onEvent(NearbyEvent nearbyEvent) {
        publishPlaylist();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /**
     * A simple ResultCallback that logs when errors occur.
     * It also displays the Nearby opt-in dialog when necessary.
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
