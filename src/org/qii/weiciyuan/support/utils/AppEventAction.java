package org.qii.weiciyuan.support.utils;

import android.content.IntentFilter;

/**
 * User: qii
 * Date: 13-4-21
 */
public class AppEventAction {
    public static final String NEW_MSG_PRIORITY_BROADCAST = "org.qii.weiciyuan.newmsg.priority";
    public static final String NEW_MSG_BROADCAST = "org.qii.weiciyuan.newmsg";

    public static IntentFilter getSystemMusicBroadcastFilterAction() {
        IntentFilter musicFilter = new IntentFilter();
        musicFilter.addAction("com.android.music.metachanged");
        musicFilter.addAction("com.android.music.playstatechanged");
        musicFilter.addAction("com.android.music.playbackcomplete");
        musicFilter.addAction("com.android.music.queuechanged");

        musicFilter.addAction("com.htc.music.metachanged");
        musicFilter.addAction("fm.last.android.metachanged");
        musicFilter.addAction("com.sec.android.app.music.metachanged");
        musicFilter.addAction("com.nullsoft.winamp.metachanged");
        musicFilter.addAction("com.amazon.mp3.metachanged");
        musicFilter.addAction("com.miui.player.metachanged");
        musicFilter.addAction("com.real.IMP.metachanged");
        musicFilter.addAction("com.sonyericsson.music.metachanged");
        musicFilter.addAction("com.rdio.android.metachanged");
        musicFilter.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        musicFilter.addAction("com.andrew.apollo.metachanged");
        return musicFilter;
    }

}
