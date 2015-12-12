package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class NearbyService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = NearbyService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private MessageListener mMessageListener;
    private Message mMessage;

    @Override
    public void onCreate() {
        super.onCreate();

        String testVar = "Hello";
        mMessage = new Message(testVar.getBytes());
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.i(TAG, "Received: " + message.toString());
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

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

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Nearby.Messages.getPermissionStatus(mGoogleApiClient).setResultCallback(
                new ErrorCheckingCallback("getPermissionStatus", new Runnable() {
                    @Override
                    public void run() {
                        publishAndSubscribe();
                    }
                })
        );

    }

    private void publishAndSubscribe() {
        Nearby.Messages.publish(mGoogleApiClient, mMessage)
                .setResultCallback(new ErrorCheckingCallback("publish()", new Runnable() {
                    @Override
                    public void run() {
                        String hi = "Yo";
                        Nearby.Messages.publish(mGoogleApiClient, new Message(hi.getBytes()));
                    }
                }));
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
                .setResultCallback(new ErrorCheckingCallback("subscribe()" ));

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

        }
    }


}
