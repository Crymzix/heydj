package ca.ubc.heydj;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.gson.Gson;

/**
 * Base activity that contains useful functionality to inherit from.
 * Currently using this class to connect to Nearby services API from
 * different activities
 * <p/>
 * Created by Chris Li on 12/13/2015.
 */
public class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    public static final int REQUEST_NEARBY_RESOLVE_ERROR = 131;
    private GoogleApiClient mGoogleApiClient;
    private MessageListener mMessageListener;
    private Message mMessage;

    private boolean mResolvingError = false;
    protected Gson mGson;
    protected MainApplication mMain;

    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMain = (MainApplication) this.getApplication();
        mGson = new Gson();
        mHandler = new Handler();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                onMessageFound(message);
            }

            @Override
            public void onLost(Message message) {
                onLost(message);
            }
        };
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(mRunnable);
        disconnect();
        super.onStop();
    }

    // This is called in response to a button tap in the Nearby permission dialog.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEARBY_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Permission granted or error resolved successfully then we proceed
                subscribe();
            } else {
                // This may mean that user had rejected to grant nearby permission.
                Log.i(TAG, "Failed to resolve error with code " + resultCode);
            }
        }
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "connection to GoogleApiClient failed");
    }


    protected void subscribe() {
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "subscribe: " + status.describeContents());
                    }
                });
    }

    protected void connect() {
        mMain.setIsBroadcasting(true);
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

    }

    protected void disconnect() {

        if (mGoogleApiClient.isConnected()) {
            if (mMessage != null) {
                Nearby.Messages.unpublish(mGoogleApiClient, mMessage)
                        .setResultCallback(new ErrorCheckingCallback("unpublish()"));
            }

            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                    .setResultCallback(new ErrorCheckingCallback("unsubscribe()"));
        }

        mMain.setIsBroadcasting(false);
        mGoogleApiClient.disconnect();
    }

    protected GoogleApiClient getGoogleApiClient (){
        return mGoogleApiClient;
    }

    /**
     * Publish the string to all subscribers. Note that we unpublish
     * the message before we publish (except on first run) in order
     * to keep the data we are publishing fresh.
     *
     * @param broadcastString
     */
    protected void broadcastString(final String broadcastString) {

        if (mMessage == null) {
            mMessage = new Message(broadcastString.getBytes());
            Nearby.Messages.publish(mGoogleApiClient, mMessage)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.i(TAG, "publishing: " + status.describeContents() + " size: " + mMessage.getContent().length);
                        }
                    });
        } else {
            Nearby.Messages.unpublish(mGoogleApiClient, mMessage)
                    .setResultCallback(new ErrorCheckingCallback("unpublish()", new Runnable() {
                        @Override
                        public void run() {
                            mMessage = new Message(broadcastString.getBytes());
                            Nearby.Messages.publish(mGoogleApiClient, mMessage)
                                    .setResultCallback(new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(Status status) {
                                            Log.i(TAG, "publishing: " + status.describeContents() + " size: " + mMessage.getContent().length);
                                        }
                                    });
                        }
                    }));

        }
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
                    if (!mResolvingError) {
                        try {
                            status.startResolutionForResult(BaseActivity.this,
                                    REQUEST_NEARBY_RESOLVE_ERROR);
                            mResolvingError = true;
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, method + " failed with exception: " + e);
                        }
                    } else {
                        // This will be encountered on initial startup because we do
                        // both publish and subscribe together.
                        Log.i(TAG, method + " failed with status: " + status
                                + " while resolving error.");
                    }
                } else {
                    Log.e(TAG, method + " failed with : " + status
                            + " resolving error: " + mResolvingError);
                }
            }
        }
    }


    protected void onMessageFound(Message message) {
        Log.i(TAG, "Message received: " + message.describeContents());
    }

    protected void onMessageLost(Message message) {
        Log.i(TAG, "Message lost: " + message.describeContents());
    }

}
