package ca.ubc.heydj.nowplaying;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ca.ubc.heydj.R;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Chris Li on 12/12/2015.
 */
public class TrackFragment extends Fragment {

    private static final String TRACK_KEY = "track_key";


    public TrackFragment() {
    }

    public static TrackFragment newInstance(Track track) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(TRACK_KEY, track);
        TrackFragment trackFragment = new TrackFragment();
        trackFragment.setArguments(bundle);
        return trackFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track, container, false);

        Track track = getArguments().getParcelable(TRACK_KEY);

        ImageView albumImage = (ImageView) view.findViewById(R.id.album_image);
        TextView trackAlbum = (TextView) view.findViewById(R.id.track_album);

        trackAlbum.setText(track.album.name);
        Picasso.with(getActivity())
                .load(track.album.images.get(0).url)
                .fit()
                .centerCrop()
                .into(albumImage);

        return view;
    }
}
