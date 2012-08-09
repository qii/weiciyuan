package org.qii.weiciyuan.ui.Abstract;

import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.ListView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;
import org.qii.weiciyuan.ui.timeline.Commander;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 * Time: 上午9:30
 */
public class AbstractAppActivity extends FragmentActivity {


    Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = new ConcurrentHashMap<String, AvatarBitmapWorkerTask>();
    Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = new ConcurrentHashMap<String, PictureBitmapWorkerTask>();

    protected Commander commander = new Commander() {


        @Override
        public void downloadAvatar(ImageView view, String urlKey, int position, ListView listView) {

            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                avatarBitmapWorkerTaskHashMap.remove(urlKey);
            } else {
                view.setImageDrawable(getResources().getDrawable(R.drawable.account));
                if (avatarBitmapWorkerTaskHashMap.get(urlKey) == null) {
                    AvatarBitmapWorkerTask avatarTask = new AvatarBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), avatarBitmapWorkerTaskHashMap, view, listView, position);
                    avatarTask.execute(urlKey);
                    avatarBitmapWorkerTaskHashMap.put(urlKey, avatarTask);
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
                view.setImageDrawable(getResources().getDrawable(R.drawable.picture));
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

    public Commander getCommander() {
        return commander;
    }
}
