package ca.ubc.heydj.media;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.BuildLibraryEvent;
import ca.ubc.heydj.models.Track;
import ca.ubc.heydj.services.BuildMusicLibraryService;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Chris Li on 12/31/2015.
 */
public class LocalMediaFragment extends Fragment {

    private Realm mRealm;

    private RecyclerView mTracksRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_local_media, container, false);
        mTracksRecyclerView = (RecyclerView) view.findViewById(R.id.tracks_recyclerview);
        mTracksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        getActivity().startService(new Intent(getActivity(), BuildMusicLibraryService.class));
        mRealm = Realm.getInstance(getActivity());

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

    public void onEvent(BuildLibraryEvent buildLibraryEvent) {
        RealmQuery<Track> query = mRealm.where(Track.class);
        RealmResults<Track> queryResults = query.findAll();
        RealmTracksAdapter realmTracksAdapter = new RealmTracksAdapter(getActivity(), queryResults);
        mTracksRecyclerView.setAdapter(realmTracksAdapter);
    }

}
