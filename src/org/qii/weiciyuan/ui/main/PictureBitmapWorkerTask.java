package org.qii.weiciyuan.ui.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.widget.ImageView;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.PictureBitmapDrawable;

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
    private int position;

    private Activity activity;

    public String getUrl() {
        return data;
    }

    public PictureBitmapWorkerTask(LruCache<String, Bitmap> lruCache,
                                   Map<String, PictureBitmapWorkerTask> taskMap,
                                   ImageView view, String url, int position, Activity activity) {

        this.lruCache = lruCache;
        this.taskMap = taskMap;
        this.view = new WeakReference<ImageView>(view);
        this.data = url;
        this.position = position;
        this.activity = activity;
    }


    @Override
    protected Bitmap doInBackground(String... url) {

        int reqWidth = 396;
        int reqHeight = 500;

        if (!isCancelled()) {
            return ImageTool.getPictureHighDensityThumbnailBitmap(data, reqWidth, reqHeight);
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

        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            lruCache.put(getMemCacheKey(data, position), bitmap);

            if (view != null && view.get() != null) {
                ImageView imageView = view.get();

                PictureBitmapWorkerTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }

        if (taskMap.get(data) != null) {
            taskMap.remove(data);
        }
    }

    private static PictureBitmapWorkerTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof PictureBitmapDrawable) {
                PictureBitmapDrawable downloadedDrawable = (PictureBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }


    protected String getMemCacheKey(String urlKey, int position) {
        return urlKey + position;
    }

}
