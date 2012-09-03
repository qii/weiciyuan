package org.qii.weiciyuan.ui.Abstract;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class AbstractAppActivity extends FragmentActivity {

    private int theme = 0;

    private Drawable defaultAvatar = null;
    private Drawable defaultPic = null;


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
                view.setImageDrawable(defaultAvatar);
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
                view.setImageDrawable(defaultPic);
                if (pictureBitmapWorkerTaskMap.get(urlKey) == null) {
                    PictureBitmapWorkerTask avatarTask = new PictureBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), pictureBitmapWorkerTaskMap, view, listView, position);
                    avatarTask.execute(urlKey);
                    pictureBitmapWorkerTaskMap.put(urlKey, avatarTask);
                }
            }


        }


    };


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
        forceShowActionBarOverflowMenu();
        initDefaultAvatar();
        initDefaultPic();

    }


    private void initDefaultAvatar() {
        int[] attrs = new int[]{R.attr.account};
        TypedArray ta = obtainStyledAttributes(attrs);
        defaultAvatar = ta.getDrawable(0);
    }

    private void initDefaultPic() {
        int[] attrs = new int[]{R.attr.picture};
        TypedArray ta = obtainStyledAttributes(attrs);
        defaultPic = ta.getDrawable(0);
    }

    private void forceShowActionBarOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        totalStopLoadPicture();

        defaultAvatar = null;
        defaultPic = null;
    }

    protected void totalStopLoadPicture() {
        if (avatarBitmapWorkerTaskHashMap != null) {
            for (String task : avatarBitmapWorkerTaskHashMap.keySet()) {
                avatarBitmapWorkerTaskHashMap.get(task).cancel(true);
            }
            avatarBitmapWorkerTaskHashMap = null;
        }
        if (pictureBitmapWorkerTaskMap != null) {
            for (String task : pictureBitmapWorkerTaskMap.keySet()) {
                pictureBitmapWorkerTaskMap.get(task).cancel(true);
            }
            pictureBitmapWorkerTaskMap = null;
        }
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
