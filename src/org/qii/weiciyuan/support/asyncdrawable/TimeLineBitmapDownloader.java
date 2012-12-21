package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: qii
 * Date: 12-12-12
 */
public class TimeLineBitmapDownloader {

    private Drawable transPic = new ColorDrawable(Color.TRANSPARENT);

    private Map<String, PictureBitmapWorkerTask> picTasks = new ConcurrentHashMap<String, PictureBitmapWorkerTask>();

    protected Bitmap getBitmapFromMemCache(String key) {
        return GlobalContext.getInstance().getAvatarCache().get(key);
    }


    public void downloadAvatar(ImageView view, UserBean user) {
        downloadAvatar(view, user, false);
    }


    public void downloadAvatar(ImageView view, UserBean user, AbstractTimeLineFragment fragment) {
        boolean isFling = fragment.isListViewFling();
        downloadAvatar(view, user, isFling);
    }

    public void downloadAvatar(ImageView view, UserBean user, boolean isFling) {

        if (user == null) {
            view.setImageDrawable(transPic);
            return;
        }

        String url;
        FileLocationMethod method;
        if (SettingUtility.getEnableBigAvatar()) {
            url = user.getAvatar_large();
            method = FileLocationMethod.avatar_large;
        } else {
            url = user.getProfile_image_url();
            method = FileLocationMethod.avatar_small;
        }
        display(view, url, method, isFling);
    }

    public void downContentPic(ImageView view, MessageBean msg, AbstractTimeLineFragment fragment) {
        String picUrl;

        boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

        if (SettingUtility.getEnableBigPic()) {
            picUrl = msg.getBmiddle_pic();
            display(view, picUrl, FileLocationMethod.picture_bmiddle, isFling);

        } else {
            picUrl = msg.getThumbnail_pic();
            display(view, picUrl, FileLocationMethod.picture_thumbnail, isFling);

        }
    }

    private void display(ImageView view, String urlKey, FileLocationMethod method, boolean isFling) {
        view.clearAnimation();
        final Bitmap bitmap = getBitmapFromMemCache(urlKey);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            cancelPotentialDownload(urlKey, view);
            picTasks.remove(urlKey);
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
                task = new PictureBitmapWorkerTask(picTasks, view, urlKey, method);
                PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(task);
                view.setImageDrawable(downloadedDrawable);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                picTasks.put(urlKey, task);
                return;
            }

        }

    }


    public void totalStopLoadPicture() {
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
            } else {
                return false;
            }
        }
        return true;
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


}