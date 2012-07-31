package org.qii.weiciyuan.support.utils;

import android.widget.Toast;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 * Time: 下午3:22
 */
public class ActivityUtils {

    public static void showTips(final String str) {
        GlobalContext.getInstance().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GlobalContext.getInstance(),str,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
