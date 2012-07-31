package org.qii.weiciyuan.support.utils;

import android.app.Application;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 上午11:26
 */
public final class GlobalContext extends Application {

    private static GlobalContext globalContext = null;

    private boolean isAppForeground = false;


    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
    }

    public static GlobalContext getInstance() {
        return globalContext;
    }

    public void setAppForegroundFlag() {
        isAppForeground = true;
    }

    public void removeAppForegroundFlag() {
        isAppForeground = false;
    }

    public boolean isAppForeground() {
        return isAppForeground;
    }


}
