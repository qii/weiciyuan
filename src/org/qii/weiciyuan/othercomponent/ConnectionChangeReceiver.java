package org.qii.weiciyuan.othercomponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import org.qii.weiciyuan.BuildConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.preference.SettingActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-6
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (BuildConfig.DEBUG) {
            Toast.makeText(context, "connection changed", Toast.LENGTH_SHORT).show();
        }

        if (networkInfo != null && networkInfo.isConnected()) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            boolean value = sharedPref.getBoolean(SettingActivity.ENABLE_FETCH_MSG, false);
            if (value) {
                AppNewMsgAlarm.startAlarm(context, true);
            } else {
                AppNewMsgAlarm.stopAlarm(context, false);
            }

            decideTimeLineBigPic(context, networkInfo);

        } else {
            AppNewMsgAlarm.stopAlarm(context, false);
        }

    }

    private static void decideTimeLineBigPic(Context context, NetworkInfo networkInfo) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String avatarModeValue = sharedPref.getString(SettingActivity.LIST_AVATAR_MODE, "1");
        String picModeValue = sharedPref.getString(SettingActivity.LIST_PIC_MODE, "1");

        if (!avatarModeValue.equals("3") && !picModeValue.equals("3"))
            return;

        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            //wifi network
            if (avatarModeValue.equals("3")) {
                GlobalContext.getInstance().setEnableBigAvatar(true);
            }
            if (picModeValue.equals("3")) {
                GlobalContext.getInstance().setEnableBigPic(true);
            }
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

            int subType = networkInfo.getSubtype();

            if (subType == TelephonyManager.NETWORK_TYPE_GPRS) {
                //gprs network
                if (avatarModeValue.equals("3")) {
                    GlobalContext.getInstance().setEnableBigAvatar(false);
                }
                if (picModeValue.equals("3")) {
                    GlobalContext.getInstance().setEnableBigPic(false);
                }

            } else {
                //3G or other 2.5g network,there are too many mobile technologies
                if (avatarModeValue.equals("3")) {
                    GlobalContext.getInstance().setEnableBigAvatar(false);
                }
                if (picModeValue.equals("3")) {
                    GlobalContext.getInstance().setEnableBigPic(false);
                }

            }

        }
    }
}