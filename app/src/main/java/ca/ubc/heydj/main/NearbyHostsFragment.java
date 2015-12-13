package ca.ubc.heydj.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.ubc.heydj.R;
import ca.ubc.heydj.services.NearbyHostService;

/**
 * Created by Chris Li on 12/13/2015.
 */
public class NearbyHostsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearby_hosts, container, false);

        FloatingActionButton scanButton = (FloatingActionButton) view.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(getActivity(), NearbyHostService.class));
            }
        });

        return view;
    }
}
