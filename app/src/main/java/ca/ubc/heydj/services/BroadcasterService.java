package ca.ubc.heydj.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.ubc.heydj.MainApplication;
import ca.ubc.heydj.R;
import ca.ubc.heydj.models.QueuedTrack;
import de.greenrobot.event.EventBus;

/**
 * Service class that listens for queued tracks from nearby listeners via
 * the PubNub API's publish-subscribe library. Currently a workaround for
 * the Nearby API's constraints when trying to continuously listen for
 * messages.
 *
 * <p/>
 * Created by Chris Li on 12/11/2015.
 */
public class BroadcasterService extends Service {

    private static final String TAG = BroadcasterService.class.getSimpleName();

    private Pubnub mPubNub;
    private PowerManager.WakeLock mWakeLock;

    private MainApplication mMain;
    private IncomingHandler mIncomingHandler;

    // Set of listener ids to maintain
    private Set<String> mListeners = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mPubNub = new Pubnub(getResources().getString(R.string.pubnub_publish_api_key), getResources().getString(R.string.pubnub_subscribe_api_key));
        mMain = (MainApplication) getApplication();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }

        mIncomingHandler = new IncomingHandler(this);

        try {
            mPubNub.subscribe(new String[]{mMain.getUniqueID()}, new Callback() {
                @Override
                public void connectCallback(String channel, Object message) {
                    Log.i(TAG, "CONNECT on channel:" + channel);
                }

                @Override
                public void reconnectCallback(String channel, Object message) {
                    Log.i(TAG, "RECONNECT on channel:" + channel);
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    Log.i(TAG, "DISCONNECT on channel:" + channel);
                }

                @Override
                public void successCallback(String channel, Object message, String timetoken) {
                    notifyApplication(channel + " " + message.toString());
                }

                @Override
                public void successCallback(String channel, Object message) {
                    notifyApplication(channel + " " + message.toString());
                }
            });
        } catch (PubnubException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyApplication(Object message) {

        if (mIncomingHandler == null) {
            return;
        }

        Message msg = mIncomingHandler.obtainMessage();

        try {
            String obj = (String) message;
            msg.obj = obj;
            mIncomingHandler.sendMessage(msg);
            Log.i("Received msg: ", obj);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void handleMessage(Message msg) {

        String pubNubMessage = msg.obj.toString();
        // for some odd reason, channel name (in this case, the device specific id) is appended to the string when being received
        pubNubMessage = pubNubMessage.substring(mMain.getUniqueID().length()).trim();

        try {
            QueuedTrack queuedTrack = new Gson().fromJson(pubNubMessage, QueuedTrack.class);
            if (mListeners.add(queuedTrack.listener_id)) {
                Log.i(TAG, "new listener has queued a track");
            } else {
                Log.i(TAG, "old listener, discarded");
            }

            EventBus.getDefault().post(queuedTrack);
        } catch (IllegalStateException e) {
            Log.i(TAG, "Gson error: " + e.toString());
        }
    }

    static class IncomingHandler extends Handler {
        private final WeakReference<BroadcasterService> mService;

        IncomingHandler(BroadcasterService service) {
            mService = new WeakReference<BroadcasterService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BroadcasterService service = mService.get();
            if (service != null) {
                service.handleMessage(msg);
            }
        }
    }

}
