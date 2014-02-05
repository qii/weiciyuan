package org.qii.weiciyuan.othercomponent;

import org.qii.weiciyuan.bean.android.MusicInfo;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.lib.RecordOperationAppBroadcastReceiver;
import org.qii.weiciyuan.support.utils.GlobalContext;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * User: qii
 * Date: 14-2-5
 */
public class MusicReceiver extends RecordOperationAppBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        if (!TextUtils.isEmpty(track)) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setArtist(artist);
            musicInfo.setAlbum(album);
            musicInfo.setTrack(track);
            AppLogger.d("Music" + artist + ":" + album + ":" + track);
            GlobalContext.getInstance().updateMusicInfo(musicInfo);
        }
    }
}
