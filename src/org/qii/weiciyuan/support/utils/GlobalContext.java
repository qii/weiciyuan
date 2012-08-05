package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 上午11:26
 */
public final class GlobalContext extends Application {

    private static GlobalContext globalContext = null;

    private boolean isAppForeground = false;

    private Activity activity = null;

    private LruCache<String, Bitmap> avatarCache;

    public LruCache<String, Bitmap> getAvatarCache() {
        return avatarCache;
    }

    public void setAvatarCache(LruCache<String, Bitmap> avatarCache) {
        this.avatarCache = avatarCache;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
        buildCache();
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

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private void buildCache() {
        final int memClass = ((ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();

        final int cacheSize = 1024 * 1024 * memClass / 8;

        avatarCache = new LruCache<String, Bitmap>(cacheSize);

    }


}
