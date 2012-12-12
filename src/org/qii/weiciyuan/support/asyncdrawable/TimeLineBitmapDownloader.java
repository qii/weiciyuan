package org.qii.weiciyuan.support.asyncdrawable;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ListView;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.interfaces.ICommander;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: qii
 * Date: 12-12-12
 */
public class TimeLineBitmapDownloader implements ICommander {

    private Drawable transPic = new ColorDrawable(Color.TRANSPARENT);

    private Activity activity;

    private Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = new ConcurrentHashMap<String, AvatarBitmapWorkerTask>();

    private Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = new ConcurrentHashMap<String, PictureBitmapWorkerTask>();


    public TimeLineBitmapDownloader(Activity activity) {
        this.activity = activity;
    }

    protected Bitmap getBitmapFromMemCache(String key) {
        return GlobalContext.getInstance().getAvatarCache().get(key);
    }

    @Override
    public void downloadAvatar(ImageView view, String urlKey, int position, ListView listView, boolean isFling) {
        view.clearAnimation();
        Bitmap bitmap = getBitmapFromMemCache(urlKey);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            cancelPotentialAvatarDownload(urlKey, view);
            avatarBitmapWorkerTaskHashMap.remove(getMemCacheKey(urlKey, position));
        } else {

            if (cancelPotentialAvatarDownload(urlKey, view) && !isFling) {
                AvatarBitmapWorkerTask task = new AvatarBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), avatarBitmapWorkerTaskHashMap, view, urlKey, position, activity);
                AvatarBitmapDrawable downloadedDrawable = new AvatarBitmapDrawable(task);
                view.setImageDrawable(downloadedDrawable);
                view.setTag(urlKey);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                avatarBitmapWorkerTaskHashMap.put(getMemCacheKey(urlKey, position), task);
            } else if (isFling) {
                view.setImageDrawable(transPic);
            }
        }

    }

    @Override
    public void downContentPic(final ImageView view, String urlKey, int position, ListView
            listView, FileLocationMethod method, boolean isFling) {
        view.clearAnimation();
        final Bitmap bitmap = getBitmapFromMemCache(urlKey);
        if (bitmap != null) {
            switch (method) {
                case picture_thumbnail:
                    view.setImageBitmap(bitmap);
                    cancelPotentialDownload(urlKey, view);
                    pictureBitmapWorkerTaskMap.remove(urlKey);
                    break;
                case picture_bmiddle:
                    view.setImageBitmap(bitmap);
                    cancelPotentialDownload(urlKey, view);
                    pictureBitmapWorkerTaskMap.remove(urlKey);
                    break;
            }

        } else {
            if (cancelPotentialDownload(urlKey, view) && !isFling) {
                PictureBitmapWorkerTask task = new PictureBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), pictureBitmapWorkerTaskMap, view, urlKey, activity, method);
                PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(task);
                view.setImageDrawable(downloadedDrawable);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                pictureBitmapWorkerTaskMap.put(urlKey, task);
            } else if (isFling) {
                view.setImageDrawable(transPic);
            }
        }

    }

    @Override
    public void totalStopLoadPicture() {
        if (avatarBitmapWorkerTaskHashMap != null) {
            for (String task : avatarBitmapWorkerTaskHashMap.keySet()) {
                avatarBitmapWorkerTaskHashMap.get(task).cancel(true);
            }
        }
        if (pictureBitmapWorkerTaskMap != null) {
            for (String task : pictureBitmapWorkerTaskMap.keySet()) {
                pictureBitmapWorkerTaskMap.get(task).cancel(true);
            }
        }
    }


    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        PictureBitmapWorkerTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else if (bitmapDownloaderTask.getStatus() == MyAsyncTask.Status.PENDING || bitmapDownloaderTask.getStatus() == MyAsyncTask.Status.RUNNING) {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }


    private static boolean cancelPotentialAvatarDownload(String url, ImageView imageView) {
        AvatarBitmapWorkerTask bitmapDownloaderTask = getAvatarBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
                AppLogger.e("nx");
            } else if (bitmapDownloaderTask.getStatus() == MyAsyncTask.Status.PENDING || bitmapDownloaderTask.getStatus() == MyAsyncTask.Status.RUNNING) {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
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