package ca.ubc.heydj.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ca.ubc.heydj.events.AudioPlaybackEvent;
import de.greenrobot.event.EventBus;

/**
 * Broadcast receivers for the notifications button events,
 * as this is the only way to react to those events as of now
 *
 * Created by Chris Li on 12/12/2015.
 */
public class NextBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new AudioPlaybackEvent(AudioPlaybackEvent.NEXT));
    }
}
