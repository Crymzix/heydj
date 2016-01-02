package ca.ubc.heydj.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.gson.Gson;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.malinskiy.superrecyclerview.swipe.SwipeItemManagerInterface;
import com.pubnub.api.PubnubError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.ubc.heydj.BaseActivity;
import ca.ubc.heydj.MainApplication;
import ca.ubc.heydj.R;
import ca.ubc.heydj.events.AudioFeedbackEvent;
import ca.ubc.heydj.events.NearbyEvent;
import ca.ubc.heydj.events.PlayTrackEvent;
import ca.ubc.heydj.main.MainActivity;
import ca.ubc.heydj.models.QueuedTrack;
import ca.ubc.heydj.nowplaying.NowPlayingActivity;
import ca.ubc.heydj.services.SpotifyAudioPlaybackService;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class SpotifyLibraryFragment extends Fragment implements TracksAdapter.OnItemInteractionListener {

    public static final String TAG = SpotifyLibraryFragment.class.getSimpleName();

    private TracksAdapter mTracksAdapter;
    private SuperRecyclerView mTracksRecyclerView;
    private MainApplication mMain;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_spotify_library, container, false);

        mTracksRecyclerView = (SuperRecyclerView) view.findViewById(R.id.tracks_recyclerview);
        mTracksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mMain = (MainApplication) getActivity().getApplication();

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(((MainActivity) getActivity()).getSpotifyAccessToken());

        SpotifyService spotifyService = spotifyApi.getService();
        spotifyService.getMySavedTracks(new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                mTracksAdapter = new TracksAdapter(getActivity(), savedTrackPager.items, false);
                mTracksRecyclerView.setAdapter(mTracksAdapter);
                mTracksAdapter.setOnItemClickListener(SpotifyLibraryFragment.this);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        // If we are in queuing mode, enable the Nearby API
        if (mMain.isQueuing()) {
            EventBus.getDefault().post(new NearbyEvent(NearbyEvent.CONNECT));
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(AudioFeedbackEvent audioFeedbackEvent) {
        mTracksAdapter.setTrackState(audioFeedbackEvent.getPlayerState());
    }

    /**
     * On item click, launch the AudioPlaybackService, passing in the selected track
     * and playlist it belongs to to the PlayTrackEvent, and posting it on the
     * event bus.
     *
     * @param position
     * @param selectedTrack
     */
    @Override
    public void onItemClick(int position, Track selectedTrack) {

        // Create service if it hasn't been created and send PlayTrackEvent to it
        Intent audioServiceIntent = new Intent(getActivity(), SpotifyAudioPlaybackService.class);
        audioServiceIntent.putExtra(MainActivity.SPOTIFY_ACCESS_TOKEN_KEY, ((MainActivity) getActivity()).getSpotifyAccessToken());
        getActivity().startService(audioServiceIntent);
        PlayTrackEvent playTrackEvent = new PlayTrackEvent();
        playTrackEvent.setCurrentTrackIndex(position);
        playTrackEvent.setUserTracks(mTracksAdapter.getSavedTracks());

        // Open NowPlayingActivity, passing relevant information
        Intent nowPlayingIntent = new Intent(getActivity(), NowPlayingActivity.class);
        nowPlayingIntent.putExtra(NowPlayingActivity.CURRENT_TRACK_POSITION_KEY, position);
        nowPlayingIntent.putParcelableArrayListExtra(NowPlayingActivity.SAVED_TRACKS_KEY, (ArrayList<? extends Parcelable>) mTracksAdapter.getSavedTracks());

        getActivity().startActivityForResult(nowPlayingIntent, MainActivity.NOW_PLAYING_REQUEST);

        // One event for a running service and one event for service that has just been instantiated
        EventBus.getDefault().post(playTrackEvent);
        EventBus.getDefault().postSticky(playTrackEvent);
    }

    @Override
    public void onItemSwiped(int position, Track swipedTrack) {

        QueuedTrack queuedTrack = new QueuedTrack();
        queuedTrack.listener_id = mMain.getUniqueID();
        queuedTrack.track = swipedTrack;
        Gson gson = new Gson();
        String queuedTrackString = gson.toJson(queuedTrack);
        try {
            queueTrack(queuedTrackString);
            ((BaseActivity) getActivity()).broadcastString(queuedTrackString, new BaseActivity.NearbyCallback() {
                @Override
                public void onResult(Status status) {
                    mMain.setIsQueuing(false);
                    mTracksAdapter.notifyDataSetChanged();
                    ((MainActivity) getActivity()).setToolbarMode();
                }
            });
        } catch (ClassCastException e) {
            Log.e(TAG, "Hosting activity must inherit from BaseActivity");
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void queueTrack(String queuedTrackString) throws JSONException {

        if (mMain.getCurrentHostId() != null) {
            ((BaseActivity) getActivity()).getPubNub().publish(mMain.getCurrentHostId(), new JSONObject(queuedTrackString), new com.pubnub.api.Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Log.i(TAG, "published message via Pubnub on channel: " + channel);
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.e(TAG, "error via Pubnub on channel: " + channel + "error: " + error.getErrorString() + " code: " + error.errorCode);
                }
            });
        }
    }
}
