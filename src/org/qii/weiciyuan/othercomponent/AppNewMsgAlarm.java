package org.qii.weiciyuan.othercomponent;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-9-14
 */
public class AppNewMsgAlarm {

    private static final int REQUEST_CODE = 195;

    public static void startAlarm(Context context, boolean silent) {

        String value = SettingUtility.getFrequency();

        long time = AlarmManager.INTERVAL_DAY;

        if (value.equals("1"))
            time = (3 * 60 * 1000);
        if (value.equals("2"))
            time = (AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        if (value.equals("3"))
            time = (AlarmManager.INTERVAL_HALF_HOUR);

        AlarmManager alarm = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        Intent intent = new Intent(context, FetchNewMsgService.class);
        PendingIntent sender = PendingIntent.getService(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, time, sender);
        if (!silent) {
            Toast.makeText(context, context.getString(R.string.start_fetch_msg), Toast.LENGTH_SHORT).show();
        }
    }

    public static void stopAlarm(Context context, boolean clearNotification) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        Intent intent = new Intent(context, FetchNewMsgService.class);
        PendingIntent sender = PendingIntent.getService(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm.cancel(sender);
        if (clearNotification) {
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(Long.valueOf(GlobalContext.getInstance().getCurrentAccountId()).intValue());
        }
    }
}
