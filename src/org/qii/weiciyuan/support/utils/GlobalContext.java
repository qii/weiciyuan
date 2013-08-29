package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Display;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.GroupListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.MusicInfo;
import org.qii.weiciyuan.support.crashmanager.CrashManager;
import org.qii.weiciyuan.support.crashmanager.CrashManagerConstants;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.database.GroupDBTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.smileypicker.SmileyMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public final class GlobalContext extends Application {

    //singleton
    private static GlobalContext globalContext = null;

    //image size
    private Activity activity = null;
    private Activity currentRunningActivity = null;
    private DisplayMetrics displayMetrics = null;

    //image memory cache
    private LruCache<String, Bitmap> avatarCache = null;

    //current account info
    private AccountBean accountBean = null;

    public boolean startedApp = false;

    private Map<String, Bitmap> emotionsPic = new LinkedHashMap<String, Bitmap>();

    private GroupListBean group = null;

    private MusicInfo musicInfo = new MusicInfo();

    private Handler handler = new Handler();

    public boolean tokenExpiredDialogIsShowing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
        buildCache();
        CrashManagerConstants.loadFromContext(this);
        CrashManager.registerHandler();
    }

    public static GlobalContext getInstance() {
        return globalContext;
    }

    public Handler getHandler() {
        return handler;
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

    public void setAccountBean(final AccountBean accountBean) {
        this.accountBean = accountBean;
    }

    public void updateUserInfo(final UserBean userBean) {
        this.accountBean.setInfo(userBean);
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (MyProfileInfoChangeListener listener : profileListenerSet) {
                    listener.onChange(userBean);
                }
            }
        });
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

    private Set<MyProfileInfoChangeListener> profileListenerSet = new HashSet<MyProfileInfoChangeListener>();

    public void registerForAccountChangeListener(MyProfileInfoChangeListener listener) {
        if (listener != null)
            profileListenerSet.add(listener);
    }

    public void unRegisterForAccountChangeListener(MyProfileInfoChangeListener listener) {
        profileListenerSet.remove(listener);
    }

    public static interface MyProfileInfoChangeListener {
        public void onChange(UserBean newUserBean);
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

    public Activity getCurrentRunningActivity() {
        return currentRunningActivity;
    }

    public void setCurrentRunningActivity(Activity currentRunningActivity) {
        this.currentRunningActivity = currentRunningActivity;
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
        Map<String, String> emotions = SmileyMap.getInstance().get();
        List<String> index = new ArrayList<String>();
        index.addAll(emotions.keySet());
        for (String str : index) {
            String name = emotions.get(str);
            AssetManager assetManager = GlobalContext.getInstance().getAssets();
            InputStream inputStream;
            try {
                inputStream = assetManager.open(name);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                            Utility.dip2px(getResources().getInteger(R.integer.emotion_size)),
                            Utility.dip2px(getResources().getInteger(R.integer.emotion_size)),
                            true);
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

    public void updateMusicInfo(MusicInfo musicInfo) {
        this.musicInfo = musicInfo;
    }

    public MusicInfo getMusicInfo() {
        return musicInfo;
    }
}

