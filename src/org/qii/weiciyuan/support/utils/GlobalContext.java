package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.LruCache;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.ui.preference.SettingActivity;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 上午11:26
 */
public final class GlobalContext extends Application {

    private static GlobalContext globalContext = null;


    private Activity activity = null;

    private LruCache<String, Bitmap> avatarCache;

    private boolean enablePic = true;

    private int theme = 0;

    private int fontSize = 0;

    private AccountBean accountBean = null;

    public boolean startedApp = false;

    public AccountBean getAccountBean() {
        return accountBean;
    }

    public void setAccountBean(AccountBean accountBean) {
        this.accountBean = accountBean;
    }

    public int getFontSize() {
        if (fontSize != 0) {
            return fontSize;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String value = sharedPref.getString(SettingActivity.FONT_SIZE, "15");
            GlobalContext.getInstance().setFontSize(Integer.valueOf(value));
            return fontSize;
        }
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getAppTheme() {
        if (theme != 0) {
            return theme;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String value = sharedPref.getString(SettingActivity.THEME, "3");
            if (value.equals("1"))
                GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Black);
            if (value.equals("2"))
                GlobalContext.getInstance().setAppTheme(R.style.AppTheme_White);
            if (value.equals("3"))
                GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Black_White);

            return theme;
        }
    }

    public void setAppTheme(int theme) {
        this.theme = theme;
    }

    public boolean isEnablePic() {
        return enablePic;
    }

    public void setEnablePic(boolean enablePic) {
        this.enablePic = enablePic;
    }

    //for userinfo and topic

    public String getSpecialToken() {
        return specialToken;
    }

    public void setSpecialToken(String specialToken) {
        this.specialToken = specialToken;
    }

    private String specialToken = "";

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

        avatarCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                return bitmap.getByteCount();
            }
        };
    }


}
