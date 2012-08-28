package org.qii.weiciyuan.support.utils;

import android.text.TextUtils;
import org.qii.weiciyuan.R;

import java.util.Calendar;
import java.util.Date;

/**
 * User: qii
 * Date: 12-8-28
 */
public class TimeTool {

    private static int MILL_MIN = 1000 * 60;

    public static String getListTime(String created_at) {
        if (!TextUtils.isEmpty(created_at)) {
            Calendar cal = Calendar.getInstance();
            int nowMonth = cal.get(Calendar.MONTH) + 1;
            int nowDay = cal.get(Calendar.DAY_OF_MONTH);
            int nowHour = cal.get(Calendar.HOUR_OF_DAY);
            int nowMinute = cal.get(Calendar.MINUTE);
            int nowSeconds = cal.get(Calendar.SECOND);

            Calendar messageCal = Calendar.getInstance();
            messageCal.setTime(new Date(created_at));
            int month = messageCal.get(Calendar.MONTH) + 1;
            int day = messageCal.get(Calendar.DAY_OF_MONTH);
            int hour = messageCal.get(Calendar.HOUR_OF_DAY);
            int minute = messageCal.get(Calendar.MINUTE);
            int seconds = messageCal.get(Calendar.SECOND);

            if (nowMonth > month) {
                return "" + (nowMonth - month) + GlobalContext.getInstance().getString(R.string.month);
            }
            if (nowDay > day) {
                if (nowDay == day + 1) {
                    long calTime = cal.getTimeInMillis();
                    long messageCalTime = messageCal.getTimeInMillis();
                    //minutes
                    long calMin = (calTime - messageCalTime) / (MILL_MIN);
                    if (calMin < 60) {
                        return "" + calMin + GlobalContext.getInstance().getString(R.string.min);
                    } else if (60 < calMin) {
                        long calHour = calMin / 60;
                        return "" + calHour + GlobalContext.getInstance().getString(R.string.hour);
                    }
                }
                return "" + (nowDay - day) + GlobalContext.getInstance().getString(R.string.day);
            }
            if (nowHour > hour) {
                if (nowHour == hour + 1) {
                    long calTime = cal.getTimeInMillis();
                    long messageCalTime = messageCal.getTimeInMillis();
                    long time = (calTime - messageCalTime) / (MILL_MIN);
                    return "" + time + GlobalContext.getInstance().getString(R.string.min);
                }
                return "" + (nowHour - hour) + GlobalContext.getInstance().getString(R.string.hour);

            }
            if (nowMinute > minute) {
                return "" + (nowMinute - minute) + GlobalContext.getInstance().getString(R.string.min);
            }
            if (nowMinute == minute) {
                return GlobalContext.getInstance().getString(R.string.justnow);
            }

            return "";

        }

        return "";
    }
}
