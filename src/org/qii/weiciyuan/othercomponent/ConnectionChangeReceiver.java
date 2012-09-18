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

    private void decideTimeLineBigPic(Context context, NetworkInfo networkInfo) {

        SharedPreferences autoShowBigPic = PreferenceManager.getDefaultSharedPreferences(context);
        boolean value = autoShowBigPic.getBoolean(SettingActivity.AUTO_SHOW_BIG_PIC, true);

        if (!value)
            return;

        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            //wifi network
            GlobalContext.getInstance().setEnableBigPic(true);
            SharedPreferences bigPicSP = PreferenceManager.getDefaultSharedPreferences(context);
            bigPicSP.edit().putBoolean(SettingActivity.SHOW_BIG_PIC, true).commit();
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

            int subType = networkInfo.getSubtype();

            if (subType == TelephonyManager.NETWORK_TYPE_GPRS) {
                //gprs network
                GlobalContext.getInstance().setEnableBigPic(false);
                SharedPreferences bigPicSP = PreferenceManager.getDefaultSharedPreferences(context);
                bigPicSP.edit().putBoolean(SettingActivity.SHOW_BIG_PIC, false).commit();
            } else {
                //3G or other 2.5g network,there are too many mobile technologies
                GlobalContext.getInstance().setEnableBigPic(true);
                SharedPreferences bigPicSP = PreferenceManager.getDefaultSharedPreferences(context);
                bigPicSP.edit().putBoolean(SettingActivity.SHOW_BIG_PIC, true).commit();
            }

        }
    }
}