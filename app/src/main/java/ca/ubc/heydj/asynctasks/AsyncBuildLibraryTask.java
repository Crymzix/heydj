package ca.ubc.heydj.asynctasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

/**
 * Created by Chris Li on 12/11/2015.
 */
public class AsyncBuildLibraryTask extends AsyncTask<String, String, Void> {

    private Context mContext;

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

            for (int i = 0; i < mediaStoreCursor.getCount(); i++) {
                mediaStoreCursor.moveToPosition(i);

            }
        }

        return null;
    }
}
