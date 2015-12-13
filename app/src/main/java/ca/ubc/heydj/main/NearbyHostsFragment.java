package ca.ubc.heydj.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.ubc.heydj.R;
import ca.ubc.heydj.events.BroadcastPlaylistEvent;
import ca.ubc.heydj.services.NearbyService;
import de.greenrobot.event.EventBus;

/**
 * Created by Chris Li on 12/13/2015.
 */
public class NearbyHostsFragment extends Fragment {

    private static final String TAG = NearbyHostsFragment.class.getSimpleName();

    private ProgressBar mProgressBar;
    private RecyclerView mHostsRecyclerView;
    private NearbyHostsAdapter mHostsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearby_hosts, container, false);

        Intent nearbyService = new Intent(getActivity(), NearbyService.class);
        nearbyService.putExtra(NearbyService.IS_HOST_KEY, false);
        getActivity().startService(nearbyService);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mHostsRecyclerView = (RecyclerView) view.findViewById(R.id.hosts_recyclerview);
        mHostsAdapter = new NearbyHostsAdapter(getActivity(), new ArrayList<BroadcastPlaylistEvent>());
        mHostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mHostsRecyclerView.setAdapter(mHostsAdapter);

        return view;
    }

    @Override
    public void onStop() {
        getActivity().stopService(new Intent(getActivity(), NearbyService.class));
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(final BroadcastPlaylistEvent broadcastPlaylistEvent){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mHostsRecyclerView != null) {
                    mHostsRecyclerView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    mHostsAdapter.addItem(broadcastPlaylistEvent);
                }
            }
        });
    }

}
