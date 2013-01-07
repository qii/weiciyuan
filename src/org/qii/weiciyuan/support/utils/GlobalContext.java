package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.database.GroupDBTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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

    public boolean startedApp = false;


    private Map<String, String> emotions = null;

    private Map<String, Bitmap> emotionsPic = new HashMap<String, Bitmap>();

    private GroupListBean group = null;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        buildCache();
    }

    public static GlobalContext getInstance() {
        return globalContext;
    }


    public GroupListBean getGroup() {
        if (group == null) {
            group = GroupDBTask.get(GlobalContext.getInstance().getCurrentAccountId());
        }
        return group;
    }

    public void setGroup(GroupListBean group) {
        this.group = group;
    }

    private Map<String, String> getEmotions() {
        if (emotions == null) {
            InputStream inputStream = getResources().openRawResource(R.raw.emotions);
            emotions = new Gson().fromJson(new InputStreamReader(inputStream), new TypeToken<Map<String, String>>() {
            }.getType());
        }

        return emotions;
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
            String id = SettingUtility.getDefaultAccountId();
            if (!TextUtils.isEmpty(id)) {
                accountBean = AccountDBTask.getAccount(id);
            } else {
                List<AccountBean> accountList = AccountDBTask.getAccountList();
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


    public synchronized LruCache<String, Bitmap> getAvatarCache() {
        if (avatarCache == null) {
            buildCache();
        }
        return avatarCache;
    }

    public String getSpecialToken() {
        if (getAccountBean() != null)
            return getAccountBean().getAccess_token();
        else
            return "";
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private void buildCache() {
        int memClass = ((ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();

//        int cacheSize = 1024 * 1024 * memClass / 5;

        int cacheSize = 1024 * 1024 * 8;

        avatarCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getByteCount();
            }
        };
    }

    public synchronized Map<String, Bitmap> getEmotionsPics() {
        if (emotionsPic != null && emotionsPic.size() > 0) {
            return emotionsPic;
        } else {
            getEmotionsTask();
            return emotionsPic;
        }
    }


    private void getEmotionsTask() {
        Map<String, String> emotions = GlobalContext.getInstance().getEmotions();
        List<String> index = new ArrayList<String>();
        index.addAll(emotions.keySet());
        for (String str : index) {
            String url = emotions.get(str);
            int position = url.lastIndexOf("/");
            String name = url.substring(position + 1);
            AssetManager assetManager = GlobalContext.getInstance().getAssets();
            InputStream inputStream;
            try {
                inputStream = assetManager.open(name);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, Utility.dip2px(20), Utility.dip2px(20), true);
                    if (bitmap != scaledBitmap) {
                        bitmap.recycle();
                        bitmap = scaledBitmap;
                    }
                    emotionsPic.put(str, bitmap);
                }
            } catch (IOException ignored) {

            }
        }
    }
}

