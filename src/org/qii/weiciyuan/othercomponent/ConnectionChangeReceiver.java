package org.qii.weiciyuan.othercomponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import org.qii.weiciyuan.BuildConfig;

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

            AppNewMsgAlarm.startAlarm(context,true);
        } else {
            AppNewMsgAlarm.stopAlarm(context,false);
        }

    }
}