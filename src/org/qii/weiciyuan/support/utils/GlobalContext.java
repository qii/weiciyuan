package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.LruCache;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.preference.SettingActivity;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public final class GlobalContext extends Application {

    private static GlobalContext globalContext = null;


    private Activity activity = null;

    private LruCache<String, Bitmap> avatarCache = null;

    private Boolean enablePic = null;

    private int theme = 0;

    private int fontSize = 0;

    private String currentAccountId = null;

    public boolean startedApp = false;

    private String specialToken = "";


    public String getCurrentAccountId() {
        if (!TextUtils.isEmpty(currentAccountId)) {
            return currentAccountId;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String value = sharedPref.getString("currentAccountId", "");
            GlobalContext.getInstance().setCurrentAccountId(value);
            return currentAccountId;
        }
    }

    public void setCurrentAccountId(String id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().putString("currentAccountId", id).commit();
        this.currentAccountId = id;
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

    public LruCache<String, Bitmap> getAvatarCache() {
        if (avatarCache != null) {
            return avatarCache;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            buildCache();
            return avatarCache;
        }
    }

    public boolean isEnablePic() {
        if (enablePic != null) {
            return enablePic;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            enablePic = sharedPref.getBoolean(SettingActivity.ENABLE_PIC, true);
            return enablePic;
        }
    }

    public void setAppTheme(int theme) {
        this.theme = theme;
    }


    public void setEnablePic(boolean enablePic) {
        this.enablePic = enablePic;
    }

    //for userinfo and topic

    public String getSpecialToken() {
        if (!TextUtils.isEmpty(specialToken)) {
            return specialToken;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String token = sharedPref.getString("token", "");
            this.specialToken = token;
            return specialToken;
        }
    }

    public void setSpecialToken(String specialToken) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().putString("token", specialToken).commit();
        this.specialToken = specialToken;
    }


    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
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
