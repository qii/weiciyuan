package org.qii.weiciyuan.support.utils;

import android.content.res.TypedArray;

/**
 * User: qii
 * Date: 13-8-4
 */
public class ThemeUtility {

    public static int getColor(int attr) {
        int[] attrs = new int[]{attr};
        TypedArray ta = GlobalContext.getInstance().obtainStyledAttributes(attrs);
        return ta.getColor(0, 430);

    }

}
