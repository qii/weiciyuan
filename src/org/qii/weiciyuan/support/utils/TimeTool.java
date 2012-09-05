package org.qii.weiciyuan.support.utils;

import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;

import java.util.Calendar;
import java.util.Date;

/**
 * User: qii
 * Date: 12-8-28
 */
public class TimeTool {


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
                if (nowMonth == month + 1) {
                    long calTime = cal.getTimeInMillis();
                    long messageCalTime = messageCal.getTimeInMillis();
                    long calMin = (calTime - messageCalTime) / (MILL_MIN);
                    if (calMin < 60) {
                        return "" + calMin + GlobalContext.getInstance().getString(R.string.min);
                    } else if (60 < calMin) {
                        long calHour = calMin / 60;
                        if (calHour < 24) {
                            return "" + calHour + GlobalContext.getInstance().getString(R.string.hour);
                        } else if (calHour > 24) {
                            long calDay = calHour / 24;
                            if (calDay < 31)
                                return "" + calDay + GlobalContext.getInstance().getString(R.string.day);
                        }
                    }

                }
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
                        if (calHour < 24)
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
                    if (time < 60)
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


    private static int MILL_MIN = 1000 * 60;
    private static int MILL_HOUR = MILL_MIN * 60;
    private static int MILL_DAY = MILL_HOUR * 24;

    public static String getListTime(MessageBean bean) {
        long now = System.currentTimeMillis();
        long msg = 0L;

        if (bean.getMills() != 0) {
            msg = bean.getMills();
        } else {
            TimeTool.dealMills(bean);
            msg = bean.getMills();
        }

        long calcMills = now - msg;

        long calSeconds = calcMills / 1000;

        if (calSeconds < 60) {
            return ""+calSeconds+GlobalContext.getInstance().getString(R.string.sec);
        }

        long calMins = calSeconds / 60;

        if (calMins < 60) {

            return "" + calMins + GlobalContext.getInstance().getString(R.string.min);
        }

        long calHours = calMins / 60;

        if (calHours < 24) {
            return "" + calHours + GlobalContext.getInstance().getString(R.string.hour);
        }

        long calDay = calHours / 24;

        if (calDay < 31) {
            return "" + calDay + GlobalContext.getInstance().getString(R.string.day);
        }

        long calMonth = calDay / 31;

        if (calMonth < 12) {
            return "" + calMonth + GlobalContext.getInstance().getString(R.string.month);

        }

        return "" + calMonth / 12 + GlobalContext.getInstance().getString(R.string.month);


    }


    public static void dealMills(MessageBean bean) {
        Date date = new Date(bean.getCreated_at());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        bean.setMills(calendar.getTimeInMillis());
    }
}
