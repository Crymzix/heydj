package ca.ubc.heydj.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Object to be used with the Realm DB
 * <p/>
 * Created by Chris Li on 12/11/2015.
 */
public class Track extends RealmObject{

    @PrimaryKey
    private String songId;

    private String songTitle;
    private String songArtist;
    private String songAlbum;
    private String songAlbumArtist;
    private String duration;
    private String songFilePath;
    private String songTrackNumber;
    private String songGenre;
    private String songYear;
    private String songDateModified;
    private String songAlbumArtPath;
    private long addedDate;
    private int rating;
    private String songSource;
    private String songSavedPosition;
    private String numberOfAlbums;
    private String numberOfTracks;
    private String numberOfSongsInGenre;

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public void setSongAlbum(String songAlbum) {
        this.songAlbum = songAlbum;
    }

    public String getSongAlbumArtist() {
        return songAlbumArtist;
    }

    public void setSongAlbumArtist(String songAlbumArtist) {
        this.songAlbumArtist = songAlbumArtist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSongFilePath() {
        return songFilePath;
    }

    public void setSongFilePath(String songFilePath) {
        this.songFilePath = songFilePath;
    }

    public String getSongTrackNumber() {
        return songTrackNumber;
    }

    public void setSongTrackNumber(String songTrackNumber) {
        this.songTrackNumber = songTrackNumber;
    }

    public String getSongGenre() {
        return songGenre;
    }

    public void setSongGenre(String songGenre) {
        this.songGenre = songGenre;
    }

    public String getSongYear() {
        return songYear;
    }

    public void setSongYear(String songYear) {
        this.songYear = songYear;
    }

    public String getSongDateModified() {
        return songDateModified;
    }

    public void setSongDateModified(String songDateModified) {
        this.songDateModified = songDateModified;
    }

    public String getSongAlbumArtPath() {
        return songAlbumArtPath;
    }

    public void setSongAlbumArtPath(String songAlbumArtPath) {
        this.songAlbumArtPath = songAlbumArtPath;
    }

    public long getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(long addedDate) {
        this.addedDate = addedDate;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getSongSource() {
        return songSource;
    }

    public void setSongSource(String songSource) {
        this.songSource = songSource;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getSongSavedPosition() {
        return songSavedPosition;
    }

    public void setSongSavedPosition(String songSavedPosition) {
        this.songSavedPosition = songSavedPosition;
    }

    public String getNumberOfAlbums() {
        return numberOfAlbums;
    }

    public void setNumberOfAlbums(String numberOfAlbums) {
        this.numberOfAlbums = numberOfAlbums;
    }

    public String getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(String numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    public String getNumberOfSongsInGenre() {
        return numberOfSongsInGenre;
    }

    public void setNumberOfSongsInGenre(String numberOfSongsInGenre) {
        this.numberOfSongsInGenre = numberOfSongsInGenre;
    }

}
