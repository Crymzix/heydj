package ca.ubc.heydj.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ca.ubc.heydj.main.MainActivity;
import ca.ubc.heydj.R;
import ca.ubc.heydj.events.PlayTrackEvent;
import ca.ubc.heydj.nowplaying.NowPlayingActivity;
import ca.ubc.heydj.services.AudioPlaybackService;
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
public class SpotifyLibraryFragment extends Fragment implements TracksAdapter.OnItemClickListener{

    private static final String TAG = SpotifyLibraryFragment.class.getSimpleName();

    private TracksAdapter mTracksAdapter;
    private RecyclerView mTracksRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_spotify_library, container, false);

        mTracksRecyclerView = (RecyclerView) view.findViewById(R.id.tracks_recyclerview);
        mTracksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(((MainActivity) getActivity()).getSpotifyAccessToken());

        SpotifyService spotifyService = spotifyApi.getService();
        spotifyService.getMySavedTracks(new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                mTracksAdapter = new TracksAdapter(getActivity(), savedTrackPager.items);
                mTracksRecyclerView.setAdapter(mTracksAdapter);
                mTracksAdapter.setOnItemClickListener(SpotifyLibraryFragment.this);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        return view;
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
        Intent audioServiceIntent = new Intent(getActivity(), AudioPlaybackService.class);
        audioServiceIntent.putExtra(MainActivity.SPOTIFY_ACCESS_TOKEN_KEY, ((MainActivity) getActivity()).getSpotifyAccessToken());
        getActivity().startService(audioServiceIntent);
        PlayTrackEvent playTrackEvent = new PlayTrackEvent();
        playTrackEvent.setCurrentTrackIndex(position);
        playTrackEvent.setUserTracks(mTracksAdapter.getSavedTracks());

        // Open NowPlayingActivity, passing relevant information
        Intent nowPlayingIntent = new Intent(getActivity(), NowPlayingActivity.class);
        nowPlayingIntent.putExtra(NowPlayingActivity.CURRENT_TRACK_POSITION_KEY, position);
        nowPlayingIntent.putParcelableArrayListExtra(NowPlayingActivity.SAVED_TRACKS_KEY, (ArrayList<? extends Parcelable>) mTracksAdapter.getSavedTracks());
        startActivity(nowPlayingIntent);

        // one event for a running service and one event for service that has just been instantiated
        EventBus.getDefault().post(playTrackEvent);
        EventBus.getDefault().postSticky(playTrackEvent);
    }
}
