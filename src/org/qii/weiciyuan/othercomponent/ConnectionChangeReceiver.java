package org.qii.weiciyuan.othercomponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.preference.SettingActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-6
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String avatarModeValue = sharedPref.getString(SettingActivity.LIST_AVATAR_MODE, "1");
        String picModeValue = sharedPref.getString(SettingActivity.LIST_PIC_MODE, "1");

        if (avatarModeValue.equals("3")) {
            SettingUtility.setEnableBigAvatar(Utility.isWifi(context));
        }
        if (picModeValue.equals("3")) {
            SettingUtility.setEnableBigPic(Utility.isWifi(context));
        }
    }

    private static void decideCommentRepostAvatar(Context context) {

        switch (SettingUtility.getCommentRepostAvatar()) {
            case 1:
                SettingUtility.setEnableCommentRepostAvatar(Utility.isConnected(context));
                break;
        }
    }
}