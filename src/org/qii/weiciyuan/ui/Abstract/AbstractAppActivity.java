package org.qii.weiciyuan.ui.Abstract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;
import org.qii.weiciyuan.ui.preference.SettingActivity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class AbstractAppActivity extends FragmentActivity {

    private int theme = 0;


    Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = new ConcurrentHashMap<String, AvatarBitmapWorkerTask>();
    Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = new ConcurrentHashMap<String, PictureBitmapWorkerTask>();

    protected String getMemCacheKey(String urlKey, int position) {
        return urlKey + position;
    }

    protected ICommander commander = new ICommander() {


        @Override
        public void downloadAvatar(ImageView view, String urlKey, int position, ListView listView) {

            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                avatarBitmapWorkerTaskHashMap.remove(getMemCacheKey(urlKey, position));
            } else {
                view.setImageDrawable(getResources().getDrawable(R.drawable.account_black));
                if (avatarBitmapWorkerTaskHashMap.get(getMemCacheKey(urlKey, position)) == null) {
                    AvatarBitmapWorkerTask avatarTask = new AvatarBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), avatarBitmapWorkerTaskHashMap, view, listView, position);
                    avatarTask.execute(urlKey);
                    avatarBitmapWorkerTaskHashMap.put(getMemCacheKey(urlKey, position), avatarTask);
                }
            }

        }

        @Override
        public void downContentPic(ImageView view, String urlKey, int position, ListView listView) {

            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                pictureBitmapWorkerTaskMap.remove(urlKey);
            } else {
                view.setImageDrawable(getResources().getDrawable(R.drawable.picture_black));
                if (pictureBitmapWorkerTaskMap.get(urlKey) == null) {
                    PictureBitmapWorkerTask avatarTask = new PictureBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), pictureBitmapWorkerTaskMap, view, listView, position);
                    avatarTask.execute(urlKey);
                    pictureBitmapWorkerTaskMap.put(urlKey, avatarTask);
                }
            }


        }


    };

    //only execute in AccountActivity and MainTimeLineActivity
    protected void buildThemeSetting() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sharedPref.getString(SettingActivity.THEME, "1");
        if (value.equals("1"))
            GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Black);
        if (value.equals("2"))
            GlobalContext.getInstance().setAppTheme(R.style.AppTheme_White);
        if (value.equals("3"))
            GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Black_White);
    }

    //only execute in AccountActivity and MainTimeLineActivity
    protected void buildFontSetting() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sharedPref.getString(SettingActivity.FONT_SIZE, "15");
        GlobalContext.getInstance().setFontSize(Integer.valueOf(value));
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalContext.getInstance().setActivity(this);
        if (getResources().getBoolean(R.bool.is_phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (theme == GlobalContext.getInstance().getAppTheme()) {

        } else {
            reload();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        theme = GlobalContext.getInstance().getAppTheme();
        setTheme(theme);
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.is_phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (String task : avatarBitmapWorkerTaskHashMap.keySet()) {
            avatarBitmapWorkerTaskHashMap.get(task).cancel(true);
        }
        avatarBitmapWorkerTaskHashMap = null;
        for (String task : pictureBitmapWorkerTaskMap.keySet()) {
            pictureBitmapWorkerTaskMap.get(task).cancel(true);
        }
        pictureBitmapWorkerTaskMap = null;
    }

    private void reload() {

        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    protected Bitmap getBitmapFromMemCache(String key) {
        return GlobalContext.getInstance().getAvatarCache().get(key);
    }

    public Map<String, PictureBitmapWorkerTask> getPictureBitmapWorkerTaskMap() {
        return pictureBitmapWorkerTaskMap;
    }

    public Map<String, AvatarBitmapWorkerTask> getAvatarBitmapWorkerTaskHashMap() {
        return avatarBitmapWorkerTaskHashMap;
    }

    public ICommander getCommander() {
        return commander;
    }

    protected void dealWithException(WeiboException e) {
        Toast.makeText(this, e.getError(), Toast.LENGTH_SHORT).show();
    }
}
