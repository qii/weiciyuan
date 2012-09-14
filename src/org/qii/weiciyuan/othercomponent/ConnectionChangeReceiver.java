package org.qii.weiciyuan.othercomponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.qii.weiciyuan.BuildConfig;
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
        } else {
            AppNewMsgAlarm.stopAlarm(context, false);
        }

    }
}