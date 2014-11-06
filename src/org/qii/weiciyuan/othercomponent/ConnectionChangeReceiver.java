package org.qii.weiciyuan.othercomponent;

import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/**
 * User: Jiang Qi
 * Date: 12-8-6
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable task = null;

    //receive multi broadcasts at the same time
    @Override
    public void onReceive(final Context context, Intent intent) {

        if (task != null) {
            handler.removeCallbacks(task);
        }

        task = new Runnable() {
            @Override
            public void run() {
                judgeNetworkStatus(context, true);
                task = null;
            }
        };

        handler.postDelayed(task, 2000);
    }

    public static void judgeNetworkStatus(Context context,
            boolean forceStartFetchNewUnreadBackgroundService) {
        if (Utility.isConnected(context)) {

            if (forceStartFetchNewUnreadBackgroundService) {
                if (SettingUtility.getEnableFetchMSG()) {
                    AppNewMsgAlarm.startAlarm(context, true);
                } else {
                    AppNewMsgAlarm.stopAlarm(context, false);
                }
            }

            decideTimeLineBigPic(context);
            decideCommentRepostAvatar(context);
        } else {
            AppNewMsgAlarm.stopAlarm(context, false);
        }
    }

    private static void decideTimeLineBigPic(Context context) {
        if (SettingUtility.getListAvatarMode() == 3) {
            SettingUtility.setEnableBigAvatar(Utility.isWifi(context));
        }
        if (SettingUtility.getListPicMode() == 3) {
            SettingUtility.setEnableBigPic(Utility.isWifi(context));
        }
    }

    private static void decideCommentRepostAvatar(Context context) {
        if (SettingUtility.getCommentRepostAvatar() == 3) {
            SettingUtility.setEnableCommentRepostAvatar(Utility.isWifi(context));
        }
    }
}