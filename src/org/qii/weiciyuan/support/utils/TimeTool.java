package org.qii.weiciyuan.support.utils;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ItemBean;

import java.util.Calendar;
import java.util.Date;

/**
 * User: qii
 * Date: 12-8-28
 */
public class TimeTool {

    private static int MILL_MIN = 1000 * 60;
    private static int MILL_HOUR = MILL_MIN * 60;
    private static int MILL_DAY = MILL_HOUR * 24;

    public static String getListTime(ItemBean bean) {
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
            return GlobalContext.getInstance().getString(R.string.justnow);
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

        return "" + calMonth / 12 + GlobalContext.getInstance().getString(R.string.year);

    }


    public static void dealMills(ItemBean bean) {
        Date date = new Date(bean.getCreated_at());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        bean.setMills(calendar.getTimeInMillis());
    }
}
