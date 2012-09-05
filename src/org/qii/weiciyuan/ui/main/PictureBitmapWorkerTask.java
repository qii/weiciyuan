package org.qii.weiciyuan.ui.main;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 */
public class PictureBitmapWorkerTask extends MyAsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private final WeakReference<ImageView> view;

    private Map<String, PictureBitmapWorkerTask> taskMap;
    private ListView listView;
    private int position;


    public PictureBitmapWorkerTask(LruCache<String, Bitmap> lruCache,
                                   Map<String, PictureBitmapWorkerTask> taskMap,
                                   ImageView view, String url, ListView listView, int position) {

        this.lruCache = lruCache;
        this.taskMap = taskMap;
        this.view = new WeakReference<ImageView>(view);
        this.data = url;
        this.listView = listView;
        this.position = position;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (view != null) {
            ImageView imageView = view.get();
            if (imageView != null) {
                imageView.setTag(data);
            }
        }

    }

    int reqHeight;
    int reqWidth;

    @Override
    protected Bitmap doInBackground(String... url) {


        if (!isCancelled()) {
            return ImageTool.getPictureHighDensityThumbnailBitmap(data);
        }
        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (bitmap != null) {

            lruCache.put(getMemCacheKey(data, position), bitmap);

        }
        if (taskMap != null && taskMap.get(data) != null) {
            taskMap.remove(data);
        }
        if (view != null) {
            ImageView imageView = view.get();
            if (imageView != null && imageView.getTag().equals(data)) {
                imageView.setTag("");
            }
        }
        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            lruCache.put(getMemCacheKey(data, position), bitmap);

            if (view != null) {
                ImageView imageView = view.get();
                if (imageView != null && imageView.getTag().equals(data)
                        && position <= listView.getLastVisiblePosition()
                        && position >= listView.getFirstVisiblePosition()) {
                    imageView.setImageBitmap(bitmap);
                }
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
