package ca.ubc.heydj.media;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.ubc.heydj.R;
import ca.ubc.heydj.services.BuildMusicLibraryService;

/**
 * Created by Chris Li on 12/31/2015.
 */
public class LocalMediaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_local_media, container, false);

        getActivity().startService(new Intent(getActivity(), BuildMusicLibraryService.class));

        return view;
    }
}
