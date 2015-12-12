package ca.ubc.heydj.spotify;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.ubc.heydj.MainActivity;
import ca.ubc.heydj.R;
import ca.ubc.heydj.events.PlayTrackEvent;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class SpotifyLibraryFragment extends Fragment {

    private static final String TAG = SpotifyLibraryFragment.class.getSimpleName();

    private TracksAdapter mTracksAdapter;
    private RecyclerView mTracksRecyclerView;
    private FloatingActionButton mHostButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_spotify_library, container, false);

        mTracksRecyclerView = (RecyclerView) view.findViewById(R.id.tracks_recyclerview);
        mTracksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mHostButton = (FloatingActionButton) view.findViewById(R.id.broadcast_playlist_button);

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(((MainActivity) getActivity()).getSpotifyAccessToken());

        SpotifyService spotifyService = spotifyApi.getService();
        spotifyService.getMySavedTracks(new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                mTracksAdapter = new TracksAdapter(getActivity(), savedTrackPager.items);
                mTracksRecyclerView.setAdapter(mTracksAdapter);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTracksAdapter != null) {
                    PlayTrackEvent playTrackEvent = new PlayTrackEvent();
                    playTrackEvent.setUserTracks(mTracksAdapter.getSavedTracks());
                    EventBus.getDefault().post(playTrackEvent);
                }
            }
        });

        return view;
    }
}
