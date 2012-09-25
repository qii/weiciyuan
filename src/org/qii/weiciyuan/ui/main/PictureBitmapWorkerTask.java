package org.qii.weiciyuan.ui.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Display;
import android.widget.ImageView;
import org.qii.weiciyuan.support.file.FileLocationMethod;
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

    private FileLocationMethod method;

    int reqWidth;
    int reqHeight;

    public String getUrl() {
        return data;
    }

    public PictureBitmapWorkerTask(LruCache<String, Bitmap> lruCache,
                                   Map<String, PictureBitmapWorkerTask> taskMap,
                                   ImageView view, String url, int position, Activity activity, FileLocationMethod method) {

        this.lruCache = lruCache;
        this.taskMap = taskMap;
        this.view = new WeakReference<ImageView>(view);
        this.data = url;
        this.position = position;
        this.activity = activity;
        this.method = method;

    }


    @Override
    protected Bitmap doInBackground(String... url) {


        if (!isCancelled()) {
            switch (method) {

                case picture_thumbnail:
                    return ImageTool.getThumbnailPictureWithRoundedCorner(data);

                case picture_bmiddle:
                    DisplayMetrics metrics = new DisplayMetrics();
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    display.getMetrics(metrics);
                    float reSize = activity.getResources().getDisplayMetrics().density;
                    //because height is 80dp
                    int height = (int) (reSize * 120);
                    //5 is left layout margin 16 is right layout margin 40 is avatar width 5 is the range between avatar and username
                    int width = (int) (metrics.widthPixels - (16 + 5 + 40 + 5) * reSize);

                    return ImageTool.getMiddlePictureInTimeLine(data, width, height, null);

            }
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
                    switch (method) {
                        case picture_thumbnail:
                            imageView.setImageBitmap(bitmap);
                            imageView.setBackgroundColor(Color.TRANSPARENT);
                            break;
                        case picture_bmiddle:
                            imageView.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), bitmap));
                            break;
                    }

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
