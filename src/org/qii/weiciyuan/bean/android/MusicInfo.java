package org.qii.weiciyuan.bean.android;

import android.text.TextUtils;

/**
 * User: qii
 * Date: 13-4-19
 */
public class MusicInfo {

    public String artist;
    public String album;
    public String track;

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    @Override
    public String toString() {
        if (!TextUtils.isEmpty(artist))
            return "Now Playing:" + artist + ":" + track;
        else
            return "Now Playing:" + track;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(track);
    }
}
