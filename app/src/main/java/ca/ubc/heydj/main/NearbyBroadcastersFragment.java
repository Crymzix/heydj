package ca.ubc.heydj.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.BroadcastPlaylistEvent;
import ca.ubc.heydj.events.NearbyEvent;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/13/2015.
 */
public class NearbyBroadcastersFragment extends Fragment {

    public static final String TAG = NearbyBroadcastersFragment.class.getSimpleName();

    private ProgressBar mProgressBar;
    private RecyclerView mHostsRecyclerView;
    private NearbyBroadcastersAdapter mHostsAdapter;
    private HashMap<String, Integer> mHostIds = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearby_hosts, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mHostsRecyclerView = (RecyclerView) view.findViewById(R.id.hosts_recyclerview);
        mHostsAdapter = new NearbyBroadcastersAdapter(getActivity(), new ArrayList<BroadcastPlaylistEvent>());
        mHostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mHostsRecyclerView.setAdapter(mHostsAdapter);
        mHostsAdapter.setOnItemClickListener(new NearbyBroadcastersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, List<Track> tracks) {
                Intent intent = new Intent(getActivity(), BroadcastedPlaylistActivity.class);
                intent.putParcelableArrayListExtra(BroadcastedPlaylistActivity.BROADCASTED_TRACKS_KEY, (ArrayList<? extends Parcelable>) tracks);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().post(new NearbyEvent(NearbyEvent.CONNECT));
    }

    @Override
    public void onStop() {
        EventBus.getDefault().post(new NearbyEvent(NearbyEvent.DISCONNECT));
        super.onStop();
    }

    public void onBroadcastPlaylistEvent(final BroadcastPlaylistEvent broadcastPlaylistEvent) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mHostsRecyclerView != null) {
                    mHostsRecyclerView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);

                    if (!mHostIds.containsKey(broadcastPlaylistEvent.host_id)) {
                        mHostIds.put(broadcastPlaylistEvent.host_id, mHostsAdapter.getItemCount());
                        mHostsAdapter.addItem(broadcastPlaylistEvent);
                    } else {
                        mHostsAdapter.updateItem(mHostIds.get(broadcastPlaylistEvent.host_id), broadcastPlaylistEvent);
                    }
                }
            }
        });
    }

}
