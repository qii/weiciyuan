package org.qii.weiciyuan.othercomponent;

import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * User: Jiang Qi
 * Date: 12-8-6
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        judgeNetworkStatus(context);

    }

    public static void judgeNetworkStatus(Context context) {
        if (Utility.isConnected(context)) {

            if (SettingUtility.getEnableFetchMSG()) {
                AppNewMsgAlarm.startAlarm(context, true);
            } else {
                AppNewMsgAlarm.stopAlarm(context, false);
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