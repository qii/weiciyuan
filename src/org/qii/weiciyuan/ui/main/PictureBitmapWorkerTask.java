package org.qii.weiciyuan.ui.main;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 下午4:09
 */
public class PictureBitmapWorkerTask extends MyAsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;

    private Map<String, PictureBitmapWorkerTask> taskMap;
    private ListView listView;
    private int position;


    public PictureBitmapWorkerTask(LruCache<String, Bitmap> lruCache,
                                   Map<String, PictureBitmapWorkerTask> taskMap,
                                   ImageView view, String url, ListView listView, int position) {

        this.lruCache = lruCache;
        this.taskMap = taskMap;
        this.view = view;
        this.data = url;
        this.listView = listView;
        this.position = position;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        view.setTag(data);
    }

    int reqHeight;
    int reqWidth;

    @Override
    protected Bitmap doInBackground(String... url) {


        view.post(new Runnable() {
            @Override
            public void run() {
                reqHeight = view.getHeight();
                reqWidth = view.getWidth();
            }
        });

        if (!isCancelled()) {
            return ImageTool.getPictureHighDensityThumbnailBitmap(data);
        }
        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (bitmap != null) {

            lruCache.put(getMemCacheKey(data,position), bitmap);

        }
        if (taskMap != null && taskMap.get(data) != null) {
            taskMap.remove(data);
        }
        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            lruCache.put(getMemCacheKey(data,position), bitmap);
            if (view != null && view.getTag().equals(data) && position <= listView.getLastVisiblePosition() && position >= listView.getFirstVisiblePosition()) {
                view.setImageBitmap(bitmap);
            }

        }

        if (taskMap.get(data) != null) {
            taskMap.remove(data);
        }
    }
    protected String getMemCacheKey(String urlKey, int position) {
        return urlKey + position;
    }

}
