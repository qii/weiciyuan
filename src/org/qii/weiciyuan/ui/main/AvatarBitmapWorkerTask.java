package org.qii.weiciyuan.ui.main;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.widget.ImageView;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.AvatarBitmapDrawable;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 */
public class AvatarBitmapWorkerTask extends MyAsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private final WeakReference<ImageView> view;
    private Map<String, AvatarBitmapWorkerTask> taskMap;
    private int position;


    public String getUrl() {
        return data;
    }


    public AvatarBitmapWorkerTask(LruCache<String, Bitmap> lruCache,
                                  Map<String, AvatarBitmapWorkerTask> taskMap,
                                  ImageView view, String url, int position) {

        this.lruCache = lruCache;
        this.taskMap = taskMap;
        this.view = new WeakReference<ImageView>(view);
        this.data = url;
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

    @Override
    protected Bitmap doInBackground(String... url) {

        if (!isCancelled()) {
            float reSize = GlobalContext.getInstance().getResources().getDisplayMetrics().density;
            int width = (int) (40 * reSize);
            int height = width;

            if (GlobalContext.getInstance().getEnableBigAvatar()) {
                return ImageTool.getTimeLineBigAvatarWithRoundedCorner(data, width, height);
            } else
                return ImageTool.getSmallAvatarWithRoundedCorner(data, width, height);
        }
        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {

        if (taskMap != null && taskMap.get(data) != null) {
            taskMap.remove(data);
        }


        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            if (view != null && view.get() != null) {
                ImageView imageView = view.get();

                AvatarBitmapWorkerTask bitmapDownloaderTask = getAvatarBitmapDownloaderTask(imageView);
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);
                    lruCache.put(data, bitmap);
                }
            }

        }

        if (taskMap != null && taskMap.get(getMemCacheKey(data, position)) != null) {
            taskMap.remove(getMemCacheKey(data, position));
        }
    }

    protected String getMemCacheKey(String urlKey, int position) {
        return urlKey + position;
    }


    private static AvatarBitmapWorkerTask getAvatarBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AvatarBitmapDrawable) {
                AvatarBitmapDrawable downloadedDrawable = (AvatarBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }
}
