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

    private Map<String, AvatarBitmapWorkerTask> avatarTasks = new ConcurrentHashMap<String, AvatarBitmapWorkerTask>();

    private Map<String, PictureBitmapWorkerTask> picTasks = new ConcurrentHashMap<String, PictureBitmapWorkerTask>();


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
            avatarTasks.remove(urlKey);
        } else {

            if (isFling) {
                view.setImageDrawable(transPic);
                return;
            }

            AvatarBitmapWorkerTask task = avatarTasks.get(urlKey);

            if (task != null) {
                task.addView(view);
                view.setImageDrawable(new AvatarBitmapDrawable(task));
                return;
            }


            if (cancelPotentialAvatarDownload(urlKey, view)) {
                task = new AvatarBitmapWorkerTask(avatarTasks, view, urlKey, activity);
                AvatarBitmapDrawable downloadedDrawable = new AvatarBitmapDrawable(task);
                view.setImageDrawable(downloadedDrawable);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                avatarTasks.put(urlKey, task);
                return;
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
                    picTasks.remove(urlKey);
                    break;
                case picture_bmiddle:
                    view.setImageBitmap(bitmap);
                    cancelPotentialDownload(urlKey, view);
                    picTasks.remove(urlKey);
                    break;
            }

        } else {

            if (isFling) {
                view.setImageDrawable(transPic);
                return;
            }


            PictureBitmapWorkerTask task = picTasks.get(urlKey);

            if (task != null) {
                task.addView(view);
                view.setImageDrawable(new PictureBitmapDrawable(task));
                return;
            }


            if (cancelPotentialDownload(urlKey, view)) {
                task = new PictureBitmapWorkerTask(picTasks, view, urlKey, activity, method);
                PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(task);
                view.setImageDrawable(downloadedDrawable);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                picTasks.put(urlKey, task);
                return;
            }


        }

    }

    @Override
    public void totalStopLoadPicture() {
        if (avatarTasks != null) {
            for (String task : avatarTasks.keySet()) {
                avatarTasks.get(task).cancel(true);
            }
        }
        if (picTasks != null) {
            for (String task : picTasks.keySet()) {
                picTasks.get(task).cancel(true);
            }
        }
    }


    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        PictureBitmapWorkerTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else{
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
            } else  {
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