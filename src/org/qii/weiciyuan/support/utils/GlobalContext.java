package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Display;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.ui.preference.SettingActivity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public final class GlobalContext extends Application {

    private static GlobalContext globalContext = null;


    private Activity activity = null;

    private LruCache<String, Bitmap> avatarCache = null;

    private Boolean enablePic = null;

    private Boolean enableBigPic = null;

    private Boolean enableBigAvatar = null;

    private Boolean enableSound = null;

    private Boolean autoRefresh = null;


    private int theme = 0;

    private int fontSize = 0;

    private String currentAccountId = null;

    private String currentAccountName = null;

    private AccountBean accountBean = null;

    public boolean startedApp = false;

    private String specialToken = "";

    private DisplayMetrics displayMetrics = null;

    private Boolean enableFilter = null;

    private Map<String, String> emotions = null;

    public Map<String, String> getEmotions() {
        if (emotions != null) {
            return emotions;
        } else {
            InputStream inputStream = getResources().openRawResource(R.raw.emotions);
            emotions = new Gson().fromJson(new InputStreamReader(inputStream), new TypeToken<Map<String, String>>() {
            }.getType());
        }

        return emotions;
    }

//    public void setEmotions(Map<String, String> value) {
//        this.emotions = value;
//    }

    public Boolean isEnableFilter() {
        if (enableFilter == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean result = sharedPreferences.getBoolean(SettingActivity.FILTER, false);
            this.enableFilter = result;
            return result;

        }
        return enableFilter;
    }

    public void setEnableFilter(boolean enableFilter) {
        this.enableFilter = enableFilter;
    }

    public DisplayMetrics getDisplayMetrics() {
        if (displayMetrics != null) {
            return displayMetrics;
        } else {
            Activity a = getActivity();
            if (a != null) {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                this.displayMetrics = metrics;
                return metrics;
            } else {
                //default screen is 800x480
                DisplayMetrics metrics = new DisplayMetrics();
                metrics.widthPixels = 480;
                metrics.heightPixels = 800;
                return metrics;
            }
        }
    }

    public void setAccountBean(AccountBean accountBean) {
        this.accountBean = accountBean;
    }

    public AccountBean getAccountBean() {
        if (accountBean != null) {
            return accountBean;
        } else {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String id = sharedPref.getString("id", "");
            if (!TextUtils.isEmpty(id)) {
                accountBean = DatabaseManager.getInstance().getAccount(id);
                if (accountBean != null) {
                    return accountBean;
                }
            } else {
                List<AccountBean> accountList = DatabaseManager.getInstance().getAccountList();
                if (accountList != null && accountList.size() > 0) {
                    accountBean = accountList.get(0);
                    return accountBean;
                }
            }
        }

        return null;
    }

    public String getCurrentAccountId() {
        return getAccountBean().getUid();
    }


    public String getCurrentAccountName() {

        return getAccountBean().getUsernick();
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
            if (value.equals("4"))
                GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Pure_Black);

            return theme;
        }
    }

    public synchronized LruCache<String, Bitmap> getAvatarCache() {
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
            enablePic = !sharedPref.getBoolean(SettingActivity.DISABLE_DOWNLOAD_AVATAR_PIC, false);
            return enablePic;
        }
    }

    public Boolean getEnableBigPic() {

        if (enableBigPic != null) {
            return enableBigPic;
        } else {
            AppLogger.e("GlobalContext is empty by system");

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            enableBigPic = sharedPref.getBoolean(SettingActivity.SHOW_BIG_PIC, false);
            return enableBigPic;
        }

    }

    public void setEnableBigPic(Boolean enableBigPic) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().putBoolean(SettingActivity.SHOW_BIG_PIC, enableBigPic).commit();
        this.enableBigPic = enableBigPic;
    }


    public Boolean getEnableAutoRefresh() {

        if (autoRefresh != null) {
            return autoRefresh;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            autoRefresh = sharedPref.getBoolean(SettingActivity.AUTO_REFRESH, false);
            return autoRefresh;
        }

    }

    public void setEnableAutoRefresh(Boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }


    public Boolean getEnableBigAvatar() {

        if (enableBigAvatar != null) {
            return enableBigAvatar;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            enableBigAvatar = sharedPref.getBoolean(SettingActivity.SHOW_BIG_AVATAR, false);
            return enableBigAvatar;

        }

    }

    public void setEnableBigAvatar(Boolean enableBigAvatar) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().putBoolean(SettingActivity.SHOW_BIG_AVATAR, enableBigAvatar);
        this.enableBigAvatar = enableBigAvatar;
    }


    public Boolean getEnableSound() {

        if (enableSound != null) {
            return enableSound;
        } else {
            AppLogger.e("GlobalContext is empty by system");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            enableSound = sharedPref.getBoolean(SettingActivity.SOUND, true);
            return enableSound;
        }

    }

    public void setEnableSound(Boolean enableSound) {
        this.enableSound = enableSound;
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
        getEmotions();

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

        final int cacheSize = 1024 * 1024 * memClass / 5;

        AppLogger.e("lruCache size=" + memClass / 5 + "mb");

        avatarCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                return bitmap.getByteCount();
            }
        };
    }


}
