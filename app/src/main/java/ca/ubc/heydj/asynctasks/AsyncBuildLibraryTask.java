package ca.ubc.heydj.asynctasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.Date;
import java.util.HashMap;

import ca.ubc.heydj.R;
import ca.ubc.heydj.models.Track;
import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Builds the local music library (files stored on the device)
 * and stores them in the Realm database
 *
 * Created by Chris Li on 12/11/2015.
 */
public class AsyncBuildLibraryTask extends AsyncTask<String, String, Void> {

    private Context mContext;

    private HashMap<String, Integer> mAlbumsCountMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> mSongsCountMap = new HashMap<String, Integer>();
    private HashMap<String, String> mGenresHashMap = new HashMap<String, String>();
    private HashMap<String, Integer> mGenresSongCountHashMap = new HashMap<String, Integer>();
    private HashMap<String, Uri> mMediaStoreAlbumArtMap = new HashMap<String, Uri>();

    private AsyncBuildLibraryListener mListener;

    public AsyncBuildLibraryTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        String projection[] = { MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media._ID};

        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor mediaStoreCursor = contentResolver.query(uri, null, selection, null, null);
        if (mediaStoreCursor != null) {
            saveMediaToRealmDB(mediaStoreCursor);
            mediaStoreCursor.close();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (mListener != null) {
            mListener.onFinished();
        }
    }

    private void saveMediaToRealmDB(Cursor mediaStoreCursor) {

        if (mediaStoreCursor != null) {

            //Populate a hash of all songs in MediaStore and their genres.
            buildGenresLibrary();

            //Populate a hash of all artists and their number of albums.
            buildArtistsLibrary();

            //Populate a hash of all albums and their number of songs.
            buildAlbumsLibrary();

            //Populate a hash of all albums and their album art path.
            buildMediaStoreAlbumArtHash();

            //Prefetch each column's index.
            final int titleColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            final int artistColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            final int albumColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            final int albumIdColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            final int durationColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            final int trackColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
            final int yearColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
            final int dateAddedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
            final int dateModifiedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            final int filePathColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            final int idColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumArtistColIndex = mediaStoreCursor.getColumnIndex("album_artist");

            if (albumArtistColIndex==-1) {
                albumArtistColIndex = artistColIndex;
            }

            Date date = new Date();
            Realm realm = Realm.getInstance(mContext);
            realm.beginTransaction();
            for (int i = 0; i < mediaStoreCursor.getCount(); i++) {
                mediaStoreCursor.moveToPosition(i);

                String songTitle = mediaStoreCursor.getString(titleColIndex);
                String songArtist = mediaStoreCursor.getString(artistColIndex);
                String songAlbum = mediaStoreCursor.getString(albumColIndex);
                String songAlbumId = mediaStoreCursor.getString(albumIdColIndex);
                String songAlbumArtist = mediaStoreCursor.getString(albumArtistColIndex);
                String songFilePath = mediaStoreCursor.getString(filePathColIndex);
                String songGenre = getSongGenre(songFilePath);
                String songDuration = mediaStoreCursor.getString(durationColIndex);
                String songTrackNumber = mediaStoreCursor.getString(trackColIndex);
                String songYear = mediaStoreCursor.getString(yearColIndex);
                String songDateAdded = mediaStoreCursor.getString(dateAddedColIndex);
                String songDateModified = mediaStoreCursor.getString(dateModifiedColIndex);
                String songId = mediaStoreCursor.getString(idColIndex);
                String numberOfAlbums = "" + mAlbumsCountMap.get(songArtist);
                String numberOfTracks = "" + mSongsCountMap.get(songAlbum + songArtist);
                String numberOfSongsInGenre = "" + getGenreSongsCount(songGenre);
                String songSource = "local";
                String songSavedPosition = "-1";

                String songAlbumArtPath = "";
                if (mMediaStoreAlbumArtMap.get(songAlbumId)!=null)
                    songAlbumArtPath = mMediaStoreAlbumArtMap.get(songAlbumId).toString();

                if (numberOfAlbums.equals("1"))
                    numberOfAlbums += " " + mContext.getResources().getString(R.string.album_small);
                else
                    numberOfAlbums += " " + mContext.getResources().getString(R.string.albums_small);

                if (numberOfTracks.equals("1"))
                    numberOfTracks += " " + mContext.getResources().getString(R.string.song_small);
                else
                    numberOfTracks += " " + mContext.getResources().getString(R.string.songs_small);

                if (numberOfSongsInGenre.equals("1"))
                    numberOfSongsInGenre += " " + mContext.getResources().getString(R.string.song_small);
                else
                    numberOfSongsInGenre += " " + mContext.getResources().getString(R.string.songs_small);

                //Check if any of the other tags were empty/null and set them to "Unknown xxx" values.
                if (songArtist==null || songArtist.isEmpty()) {
                    songArtist = mContext.getResources().getString(R.string.unknown_artist);
                }

                if (songAlbumArtist==null || songAlbumArtist.isEmpty()) {
                    if (songArtist!=null && !songArtist.isEmpty()) {
                        songAlbumArtist = songArtist;
                    } else {
                        songAlbumArtist = mContext.getResources().getString(R.string.unknown_album_artist);
                    }

                }

                if (songAlbum==null || songAlbum.isEmpty()) {
                    songAlbum = mContext.getResources().getString(R.string.unknown_album);;
                }

                if (songGenre==null || songGenre.isEmpty()) {
                    songGenre = mContext.getResources().getString(R.string.unknown_genre);
                }

                //Filter out track numbers and remove any bogus values.
                if (songTrackNumber!=null) {
                    if (songTrackNumber.contains("/")) {
                        int index = songTrackNumber.lastIndexOf("/");
                        songTrackNumber = songTrackNumber.substring(0, index);
                    }

                    try {
                        if (Integer.parseInt(songTrackNumber) <= 0) {
                            songTrackNumber = "";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        songTrackNumber = "";
                    }

                }

                long durationLong = 0;
                try {
                    durationLong = Long.parseLong(songDuration);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Track track = new Track();
                track.setSongTitle(songTitle);
                track.setSongArtist(songArtist);
                track.setSongAlbum(songAlbum);
                track.setSongAlbumArtist(songAlbumArtist);
                track.setDuration(convertMillisToMinsSecs(durationLong));
                track.setSongFilePath(songFilePath);
                track.setSongTrackNumber(songTrackNumber);
                track.setSongGenre(songGenre);
                track.setSongYear(songYear);
                track.setSongAlbumArtPath(songAlbumArtPath);
                track.setAddedDate(date.getTime());
                track.setRating(0);
                track.setSongDateModified(songDateModified);
                track.setSongSource(songSource);
                track.setSongId(songId);
                track.setSongSavedPosition(songSavedPosition);
                track.setNumberOfAlbums(numberOfAlbums);
                track.setNumberOfTracks(numberOfTracks);
                track.setNumberOfSongsInGenre(numberOfSongsInGenre);

                realm.copyToRealmOrUpdate(track);
            }
            realm.commitTransaction();
        }
    }

    private void buildGenresLibrary() {

        //Get a cursor of all genres in MediaStore.
        Cursor genresCursor = mContext.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME},
                null,
                null,
                null);

        //Iterate thru all genres in MediaStore.
        for (genresCursor.moveToFirst(); !genresCursor.isAfterLast(); genresCursor.moveToNext()) {
            String genreId = genresCursor.getString(0);
            String genreName = genresCursor.getString(1);

            if (genreName==null || genreName.isEmpty() ||
                    genreName.equals(" ") || genreName.equals("   ") ||
                    genreName.equals("    "))
                genreName = mContext.getResources().getString(R.string.unknown_genre);


            Cursor cursor = mContext.getContentResolver().query(makeGenreUri(genreId),
                    new String[] { MediaStore.Audio.Media.DATA },
                    null,
                    null,
                    null);

            //Add the songs' file paths and their genre names to the hash.
            if (cursor!=null) {
                for (int i=0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    mGenresHashMap.put(cursor.getString(0), genreName);
                    mGenresSongCountHashMap.put(genreName, cursor.getCount());
                }

                cursor.close();
            }

        }

        if (genresCursor!=null)
            genresCursor.close();

    }

    private Uri makeGenreUri(String genreId) {
        String CONTENTDIR = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY;
        return Uri.parse(new StringBuilder().append(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString())
                .append("/")
                .append(genreId)
                .append("/")
                .append(CONTENTDIR)
                .toString());
    }



    private void buildArtistsLibrary() {
        Cursor artistsCursor = mContext.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_ALBUMS },
                null,
                null,
                null);

        if (artistsCursor==null)
            return;

        for (int i=0; i < artistsCursor.getCount(); i++) {
            artistsCursor.moveToPosition(i);
            mAlbumsCountMap.put(artistsCursor.getString(0), artistsCursor.getInt(1));

        }

        artistsCursor.close();
    }

    private void buildAlbumsLibrary() {
        Cursor albumsCursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS },
                null,
                null,
                null);

        if (albumsCursor==null)
            return;

        for (int i=0; i < albumsCursor.getCount(); i++) {
            albumsCursor.moveToPosition(i);
            mSongsCountMap.put(albumsCursor.getString(0) + albumsCursor.getString(1), albumsCursor.getInt(2));

        }

        albumsCursor.close();
    }

    private void buildMediaStoreAlbumArtHash() {

        Cursor albumsCursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.ALBUM_ID },
                MediaStore.Audio.Media.IS_MUSIC + "=1",
                null,
                null);

        final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        if (albumsCursor==null)
            return;

        for (int i=0; i < albumsCursor.getCount(); i++) {
            albumsCursor.moveToPosition(i);
            Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, albumsCursor.getLong(0));
            mMediaStoreAlbumArtMap.put(albumsCursor.getString(0), albumArtUri);
        }

        albumsCursor.close();
    }

    private String getSongGenre(String filePath) {
        if (mGenresHashMap!=null)
            return mGenresHashMap.get(filePath);
        else
            return mContext.getResources().getString(R.string.unknown_genre);
    }

    private int getGenreSongsCount(String genre) {
        if (mGenresSongCountHashMap!=null)
            if (genre!=null)
                if (mGenresSongCountHashMap.get(genre)!=null)
                    return mGenresSongCountHashMap.get(genre);
                else
                    return 0;
            else
            if (mGenresSongCountHashMap.get(mContext.getResources().getString(R.string.unknown_genre))!=null)
                return mGenresSongCountHashMap.get(mContext.getResources().getString(R.string.unknown_genre));
            else
                return 0;
        else
            return 0;
    }

    private String convertMillisToMinsSecs(long milliseconds) {

        int secondsValue = (int) (milliseconds / 1000) % 60;
        int minutesValue = (int) ((milliseconds / (1000*60)) % 60);
        int hoursValue  = (int) ((milliseconds / (1000*60*60)) % 24);

        String seconds = "";
        String minutes = "";
        String hours = "";

        if (secondsValue < 10) {
            seconds = "0" + secondsValue;
        } else {
            seconds = "" + secondsValue;
        }

        minutes = "" + minutesValue;
        hours = "" + hoursValue;

        String output = "";
        if (hoursValue!=0) {
            minutes = "0" + minutesValue;
            hours = "" + hoursValue;
            output = hours + ":" + minutes + ":" + seconds;
        } else {
            minutes = "" + minutesValue;
            hours = "" + hoursValue;
            output = minutes + ":" + seconds;
        }

        return output;
    }

    public void setListener(AsyncBuildLibraryListener listener) {
        this.mListener = listener;
    }

    public interface AsyncBuildLibraryListener {
        void onFinished();
    }

}
