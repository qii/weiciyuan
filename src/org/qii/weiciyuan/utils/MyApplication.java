package org.qii.weiciyuan.utils;

import android.app.Application;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 上午11:26
 */
public class MyApplication extends Application {

    private static MyApplication myApplication=null;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    public static MyApplication getInstance() {
        return myApplication;
    }
}
