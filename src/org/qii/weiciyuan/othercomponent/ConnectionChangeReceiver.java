package org.qii.weiciyuan.othercomponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

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

        if (SettingUtility.getListAvatarMode() == 3) {
            SettingUtility.setEnableBigAvatar(Utility.isWifi(context));
        }
        if (SettingUtility.getListPicMode() == 3) {
            boolean currentStatus = Utility.isWifi(context);
            boolean lastStatus = SettingUtility.getEnableBigPic();
            if (currentStatus != lastStatus) {
                SettingUtility.setEnableBigPic(currentStatus);
                GlobalContext.getInstance().getAvatarCache().evictAll();
            }
        }
    }

    private static void decideCommentRepostAvatar(Context context) {

        if (SettingUtility.getCommentRepostAvatar() == 3) {
            SettingUtility.setEnableCommentRepostAvatar(Utility.isWifi(context));
        }
    }
}