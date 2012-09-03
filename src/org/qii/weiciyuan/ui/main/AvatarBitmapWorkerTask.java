package org.qii.weiciyuan.ui.main;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 下午12:25
 */
public class AvatarBitmapWorkerTask extends MyAsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;

    private Map<String, AvatarBitmapWorkerTask> taskMap;


    public AvatarBitmapWorkerTask(LruCache<String, Bitmap> lruCache,
                                  Map<String, AvatarBitmapWorkerTask> taskMap,
                                  ImageView view, String url) {

        this.lruCache = lruCache;
        this.taskMap = taskMap;
        this.view = view;
        this.data = url;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        view.setTag(data);
    }

    @Override
    protected Bitmap doInBackground(String... url) {

        if (!isCancelled()) {
            return ImageTool.getAvatarBitmap(data);
        }
        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (bitmap != null) {

            lruCache.put(data, bitmap);

        }
        if (taskMap != null && taskMap.get(data) != null) {
            taskMap.remove(data);
        }
        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            lruCache.put(data, bitmap);

            if (view != null && view.getTag().equals(data)) {
                view.setImageBitmap(bitmap);
            }

        }

        if (taskMap != null && taskMap.get(data) != null) {
            taskMap.remove(data);
        }
    }


}
