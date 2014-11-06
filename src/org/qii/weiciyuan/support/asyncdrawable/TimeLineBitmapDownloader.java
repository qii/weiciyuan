package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ThemeUtility;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * User: qii
 * Date: 12-12-12
 */
public class TimeLineBitmapDownloader {

    private int defaultPictureResId;

    private Handler handler;

    static volatile boolean pauseDownloadWork = false;
    static final Object pauseDownloadWorkLock = new Object();

    static volatile boolean pauseReadWork = false;
    static final Object pauseReadWorkLock = new Object();

    private static final Object lock = new Object();

    private static TimeLineBitmapDownloader instance;

    private TimeLineBitmapDownloader(Handler handler) {
        this.handler = handler;
        this.defaultPictureResId = ThemeUtility.getResourceId(R.attr.listview_pic_bg);
    }

    public static TimeLineBitmapDownloader getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new TimeLineBitmapDownloader(new Handler(Looper.getMainLooper()));
            }
        }
        return instance;
    }

    public static void refreshThemePictureBackground() {
        synchronized (lock) {
            instance = new TimeLineBitmapDownloader(new Handler(Looper.getMainLooper()));
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p/>
     * If work is paused, be sure setPauseDownloadWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseDownloadWork(boolean pauseWork) {
        synchronized (pauseDownloadWorkLock) {
            TimeLineBitmapDownloader.pauseDownloadWork = pauseWork;
            if (!TimeLineBitmapDownloader.pauseDownloadWork) {
                pauseDownloadWorkLock.notifyAll();
            }
        }
    }

    public void setPauseReadWork(boolean pauseWork) {
        synchronized (pauseReadWorkLock) {
            TimeLineBitmapDownloader.pauseReadWork = pauseWork;
            if (!TimeLineBitmapDownloader.pauseReadWork) {
                pauseReadWorkLock.notifyAll();
            }
        }
    }

    protected Bitmap getBitmapFromMemCache(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        } else {
            return GlobalContext.getInstance().getBitmapCache().get(key);
        }
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
            view.setImageResource(defaultPictureResId);
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
        displayImageView(view, url, method, isFling, false);
    }

    public void downContentPic(ImageView view, MessageBean msg, AbstractTimeLineFragment fragment) {
        String picUrl;

        boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

        if (SettingUtility.getEnableBigPic()) {
            picUrl = msg.getOriginal_pic();
            displayImageView(view, picUrl, FileLocationMethod.picture_large, isFling, false);
        } else {
            picUrl = msg.getThumbnail_pic();
            displayImageView(view, picUrl, FileLocationMethod.picture_thumbnail, isFling, false);
        }
    }

    public void displayMultiPicture(IWeiciyuanDrawable view, String picUrl,
            FileLocationMethod method, AbstractTimeLineFragment fragment) {

        boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

        display(view, picUrl, method, isFling, true);
    }

    public void displayMultiPicture(IWeiciyuanDrawable view, String picUrl,
            FileLocationMethod method) {

        display(view, picUrl, method, false, true);
    }

    public void downContentPic(IWeiciyuanDrawable view, MessageBean msg,
            AbstractTimeLineFragment fragment) {
        String picUrl;

        boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

        if (SettingUtility.getEnableBigPic()) {
            picUrl = msg.getOriginal_pic();
            display(view, picUrl, FileLocationMethod.picture_large, isFling, false);
        } else {
            picUrl = msg.getThumbnail_pic();
            display(view, picUrl, FileLocationMethod.picture_thumbnail, isFling, false);
        }
    }

    /**
     * when user open weibo detail, the activity will setResult to previous Activity,
     * timeline will refresh at the time user press back button to display the latest repost count
     * and comment count. But sometimes, weibo detail's pictures are very large that bitmap memory
     * cache has cleared those timeline bitmap to save memory, app have to read bitmap from sd card
     * again, then app play annoying animation , this method will check whether we should read
     * again
     * or not.
     */
    private boolean shouldReloadPicture(ImageView view, String urlKey) {
        if (urlKey.equals(view.getTag())
                && view.getDrawable() != null
                && view.getDrawable() instanceof BitmapDrawable
                && ((BitmapDrawable) view.getDrawable() != null
                && ((BitmapDrawable) view.getDrawable()).getBitmap() != null)) {
//            AppLogger.d("shouldReloadPicture=false");
            return false;
        } else {
            view.setTag(null);
//            AppLogger.d("shouldReloadPicture=true");
            return true;
        }
    }

    private void displayImageView(final ImageView view, final String urlKey,
            final FileLocationMethod method, boolean isFling, boolean isMultiPictures) {
        view.clearAnimation();

        if (!shouldReloadPicture(view, urlKey)) {
            return;
        }

        final Bitmap bitmap = getBitmapFromMemCache(urlKey);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            view.setTag(urlKey);
            if (view.getAlpha() != 1.0f) {
                view.setAlpha(1.0f);
            }
            cancelPotentialDownload(urlKey, view);
        } else {

            if (isFling) {
                view.setImageResource(defaultPictureResId);
                return;
            }

            if (!cancelPotentialDownload(urlKey, view)) {
                return;
            }

            final LocalOrNetworkChooseWorker newTask = new LocalOrNetworkChooseWorker(view, urlKey,
                    method, isMultiPictures);
            PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
            view.setImageDrawable(downloadedDrawable);

            //listview fast scroll performance
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (getBitmapDownloaderTask(view) == newTask) {
                        newTask.executeOnNormal();
                    }
                    return;
                }
            }, 400);
        }
    }

    private void display(final IWeiciyuanDrawable view, final String urlKey,
            final FileLocationMethod method, boolean isFling, boolean isMultiPictures) {
        view.getImageView().clearAnimation();

        if (!shouldReloadPicture(view.getImageView(), urlKey)) {
            return;
        }

        final Bitmap bitmap = getBitmapFromMemCache(urlKey);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            view.getImageView().setTag(urlKey);
            if (view.getProgressBar() != null) {
                view.getProgressBar().setVisibility(View.INVISIBLE);
            }
            if (view.getImageView().getAlpha() != 1.0f) {
                view.getImageView().setAlpha(1.0f);
            }
            view.setGifFlag(ImageUtility.isThisPictureGif(urlKey));
            cancelPotentialDownload(urlKey, view.getImageView());
        } else {

            if (isFling) {
                view.getImageView().setImageResource(defaultPictureResId);
                if (view.getProgressBar() != null) {
                    view.getProgressBar().setVisibility(View.INVISIBLE);
                }
                view.setGifFlag(ImageUtility.isThisPictureGif(urlKey));
                return;
            }

            if (!cancelPotentialDownload(urlKey, view.getImageView())) {
                return;
            }

            final LocalOrNetworkChooseWorker newTask = new LocalOrNetworkChooseWorker(view, urlKey,
                    method, isMultiPictures);
            PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
            view.setImageDrawable(downloadedDrawable);

            //listview fast scroll performance
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (getBitmapDownloaderTask(view.getImageView()) == newTask) {
                        newTask.executeOnNormal();
                    }
                    return;
                }
            }, 400);
        }
    }

    public void totalStopLoadPicture() {

    }

    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        IPictureWorker bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                if (bitmapDownloaderTask instanceof MyAsyncTask) {
                    ((MyAsyncTask) bitmapDownloaderTask).cancel(true);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private static IPictureWorker getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof PictureBitmapDrawable) {
                PictureBitmapDrawable downloadedDrawable = (PictureBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    public void display(final ImageView imageView, final int width, final int height,
            final String url, final FileLocationMethod method) {
        ArrayList<ImageView> imageViewArrayList = new ArrayList<ImageView>();
        imageViewArrayList.add(imageView);
        display(imageViewArrayList, width, height, url, method, null);
    }

    public void display(final ArrayList<ImageView> imageView, final int width, final int height,
            final String url, final FileLocationMethod method,
            final ArrayList<Animation> animations) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        final Bitmap bitmap = getBitmapFromMemCache(url);
        if (bitmap != null && bitmap.getHeight() == height && bitmap.getWidth() == width) {
            for (int i = 0; i < imageView.size(); i++) {
                ImageView imageView1 = imageView.get(i);
                imageView1.setImageDrawable(
                        new BitmapDrawable(GlobalContext.getInstance().getResources(),
                                bitmap));
                if (animations != null && animations.size() > i) {
                    Animation animation = animations.get(i);
                    imageView1.startAnimation(animation);
                }
            }
            return;
        }

        new MyAsyncTask<Void, Bitmap, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bitmap = null;

                String path = FileManager.getFilePathFromUrl(url, method);

                if (!(ImageUtility.isThisBitmapCanRead(path) && TaskCache
                        .isThisUrlTaskFinished(url))) {

                    boolean downloaded = TaskCache
                            .waitForPictureDownload(url, null,
                                    FileManager.generateDownloadFileName(url), method);
                    if (downloaded) {
                        path = FileManager.getFilePathFromUrl(url, method);
                    }
                }

                if (!TextUtils.isEmpty(path)) {
                    bitmap = ImageUtility
                            .readNormalPic(path, width,
                                    height);
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    GlobalContext.getInstance().getBitmapCache().put(url, bitmap);
                    for (int i = 0; i < imageView.size(); i++) {
                        ImageView imageView1 = imageView.get(i);
                        imageView1.setImageDrawable(
                                new BitmapDrawable(GlobalContext.getInstance().getResources(),
                                        bitmap));
                        if (animations != null && animations.size() > i) {
                            Animation animation = animations.get(i);
                            imageView1.startAnimation(animation);
                        }
                    }
                }
            }
        }.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static class DownloadCallback {

        public void onSubmitJobButNotBegin() {

        }

        public void onSubmitJobButAlreadyBegin() {

        }

        public void onBegin() {

        }

        public void onUpdate(int value, int max) {

        }

        public void onComplete(String localPath) {

        }
    }

    public void download(final Activity activity, final String url, final FileLocationMethod method,
            final DownloadCallback callback) {
        downloadInner(activity, url, method, callback);
    }

    public void download(final Fragment fragment, final String url, final FileLocationMethod method,
            final DownloadCallback callback) {
        downloadInner(fragment, url, method, callback);
    }

    private void downloadInner(final Object object, final String url,
            final FileLocationMethod method,
            final DownloadCallback callback) {

        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (TaskCache.isThisUrlTaskFinished(url)) {
            callback.onSubmitJobButNotBegin();
        } else {
            callback.onSubmitJobButAlreadyBegin();
        }

        new MyAsyncTask<Void, Integer, String>() {

            WeakReference<Object> activityRef;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                activityRef = new WeakReference<Object>(object);
                callback.onBegin();
            }

            @Override
            protected String doInBackground(Void... params) {

                String path = FileManager.getFilePathFromUrl(url, method);

                if (!(ImageUtility.isThisBitmapCanRead(path) && TaskCache
                        .isThisUrlTaskFinished(url))) {
                    boolean downloaded = TaskCache.waitForPictureDownload(
                            url, new FileDownloaderHttpHelper.DownloadListener() {
                                @Override
                                public void pushProgress(final int progress, final int max) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onProgressUpdate(progress, max);
                                        }
                                    });
                                }

                                @Override
                                public void completed() {

                                }

                                @Override
                                public void cancel() {

                                }
                            }, FileManager.getFilePathFromUrl(url, method), method);
                    if (downloaded) {
                        path = FileManager.getFilePathFromUrl(url, method);
                    }
                }

                return path;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (!isComponentLifeCycleFinished()) {
                    int progress = values[0];
                    int max = values[1];
                    callback.onUpdate(progress, max);
                }
            }

            @Override
            protected void onPostExecute(String value) {
                super.onPostExecute(value);
                if (!isComponentLifeCycleFinished()) {
                    callback.onComplete(value);
                }
            }

            boolean isComponentLifeCycleFinished() {
                Object object = activityRef.get();
                if (object == null) {
                    return true;
                }

                if (object instanceof Fragment) {
                    Fragment fragment = (Fragment) object;
                    if (fragment.getActivity() == null) {
                        return true;
                    }
                } else if (object instanceof Activity) {
                    Activity activity = (Activity) object;
                    if (activity.isDestroyed()) {
                        return true;
                    }
                }

                return false;
            }
        }.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }
}