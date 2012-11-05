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
import org.qii.weiciyuan.bean.GroupListBean;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.database.GroupDBManager;
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

    //singleton
    private static GlobalContext globalContext = null;
    private SharedPreferences sharedPref = null;

    //image size
    private Activity activity = null;
    private DisplayMetrics displayMetrics = null;

    //image memory cache
    private LruCache<String, Bitmap> avatarCache = null;

    //current account info
    private AccountBean accountBean = null;

    //preference
    private Boolean enablePic = null;
    private Boolean enableCommentRepostListAvatar = null;
    private Boolean enableBigPic = null;
    private Boolean enableBigAvatar = null;
    private Boolean enableSound = null;
    private Boolean autoRefresh = null;
    private Boolean enableFilter = null;


    private int theme = 0;
    private int fontSize = 0;

    public boolean startedApp = false;


    private Map<String, String> emotions = null;

    private GroupListBean group = null;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        buildCache();
        getEmotions();

    }

    public static GlobalContext getInstance() {
        return globalContext;
    }


    public GroupListBean getGroup() {
        if (group == null) {
            group = GroupDBManager.getInstance().getGroupInfo(GlobalContext.getInstance().getCurrentAccountId());
        }
        return group;
    }

    public void setGroup(GroupListBean group) {
        this.group = group;
    }

    public Map<String, String> getEmotions() {
        if (emotions == null) {
            InputStream inputStream = getResources().openRawResource(R.raw.emotions);
            emotions = new Gson().fromJson(new InputStreamReader(inputStream), new TypeToken<Map<String, String>>() {
            }.getType());
        }

        return emotions;
    }

    public Boolean isEnableFilter() {
        if (enableFilter == null) {
            enableFilter = sharedPref.getBoolean(SettingActivity.FILTER, false);
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
        if (accountBean == null) {
            String id = sharedPref.getString("id", "");
            if (!TextUtils.isEmpty(id)) {
                accountBean = DatabaseManager.getInstance().getAccount(id);
            } else {
                List<AccountBean> accountList = DatabaseManager.getInstance().getAccountList();
                if (accountList != null && accountList.size() > 0) {
                    accountBean = accountList.get(0);
                }
            }
        }

        return accountBean;
    }

    public String getCurrentAccountId() {
        return getAccountBean().getUid();
    }


    public String getCurrentAccountName() {

        return getAccountBean().getUsernick();
    }


    public int getFontSize() {
        if (fontSize == 0) {
            String value = sharedPref.getString(SettingActivity.FONT_SIZE, "15");
            GlobalContext.getInstance().setFontSize(Integer.valueOf(value));
            return fontSize;
        }
        return fontSize;
    }

    public int getAppTheme() {
        if (theme == 0) {
            String value = sharedPref.getString(SettingActivity.THEME, "1");
            switch (Integer.valueOf(value)) {
                case 1:
                    GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Four);
                    break;
                case 2:
                    GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Pure_Black);
                    break;
                default:
                    GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Four);
                    break;
            }
        }
        return theme;
    }

    public synchronized LruCache<String, Bitmap> getAvatarCache() {
        if (avatarCache == null) {
            buildCache();
        }
        return avatarCache;
    }

    public boolean isEnablePic() {
        if (enablePic == null) {
            enablePic = !sharedPref.getBoolean(SettingActivity.DISABLE_DOWNLOAD_AVATAR_PIC, false);
        }
        return enablePic;
    }

    public Boolean getEnableBigPic() {
        if (enableBigPic == null) {
            enableBigPic = sharedPref.getBoolean(SettingActivity.SHOW_BIG_PIC, false);
        }
        return enableBigPic;
    }

    public void setEnableBigPic(Boolean enableBigPic) {
        sharedPref.edit().putBoolean(SettingActivity.SHOW_BIG_PIC, enableBigPic).commit();
        this.enableBigPic = enableBigPic;
    }

    public Boolean getEnableCommentRepostListAvatar() {
        if (enableCommentRepostListAvatar == null) {
            enableCommentRepostListAvatar = !sharedPref.getBoolean(SettingActivity.CLOSE_COMMENT_AND_REPOST_AVATAR, false);
        }
        return enableCommentRepostListAvatar;
    }

    public void setEnableCommentRepostListAvatar(Boolean enableCommentRepostListAvatar) {
        this.enableCommentRepostListAvatar = enableCommentRepostListAvatar;
    }

    public Boolean getEnableAutoRefresh() {

        if (autoRefresh == null) {
            autoRefresh = sharedPref.getBoolean(SettingActivity.AUTO_REFRESH, false);
        }
        return autoRefresh;
    }

    public void setEnableAutoRefresh(Boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public Boolean getEnableBigAvatar() {

        if (enableBigAvatar == null) {
            enableBigAvatar = sharedPref.getBoolean(SettingActivity.SHOW_BIG_AVATAR, false);
        }
        return enableBigAvatar;

    }

    public void setEnableBigAvatar(Boolean enableBigAvatar) {
        sharedPref.edit().putBoolean(SettingActivity.SHOW_BIG_AVATAR, enableBigAvatar);
        this.enableBigAvatar = enableBigAvatar;
    }


    public Boolean getEnableSound() {

        if (enableSound == null) {
            enableSound = sharedPref.getBoolean(SettingActivity.SOUND, true);
        }
        return enableSound;
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

    public String getSpecialToken() {
        if (getAccountBean() != null)
            return getAccountBean().getAccess_token();
        else
            return "";
    }


    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
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

        avatarCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getByteCount();
            }
        };
    }


}
