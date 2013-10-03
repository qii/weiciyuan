package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 13-2-9
 * reuse download worker or create a new download worker
 */
public class ReadWorker extends MyAsyncTask<String, Integer, Bitmap> implements IPictureWorker {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private WeakReference<ImageView> viewWeakReference;

    private GlobalContext globalContext;
    private FileLocationMethod method;
    private FailedResult failedResult;
    private int mShortAnimationDuration;
    private WeakReference<ProgressBar> pbWeakReference;
    private boolean isMultiPictures = false;
    private IWeiciyuanDrawable IWeiciyuanDrawable;

    public String getUrl() {
        return data;
    }

    public ReadWorker(ImageView view, String url, FileLocationMethod method, boolean isMultiPictures) {

        this.globalContext = GlobalContext.getInstance();
        this.lruCache = globalContext.getAvatarCache();
        this.viewWeakReference = new WeakReference<ImageView>(view);
        this.data = url;
        this.method = method;
        this.mShortAnimationDuration = GlobalContext.getInstance().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        this.isMultiPictures = isMultiPictures;
    }

    public ReadWorker(ImageView view, String url, FileLocationMethod method) {
        this(view, url, method, false);
    }

    public ReadWorker(IWeiciyuanDrawable view, String url, FileLocationMethod method, boolean isMultiPictures) {

        this(view.getImageView(), url, method);
        this.IWeiciyuanDrawable = view;
        this.pbWeakReference = new WeakReference<ProgressBar>(view.getProgressBar());
        view.setGifFlag(false);
        if (SettingUtility.getEnableBigPic()) {
            if (view.getProgressBar() != null) {
                view.getProgressBar().setVisibility(View.VISIBLE);
                view.getProgressBar().setProgress(0);
            }
        } else {
            if (view.getProgressBar() != null) {
                view.getProgressBar().setVisibility(View.INVISIBLE);
                view.getProgressBar().setProgress(0);
            }
        }
        this.isMultiPictures = isMultiPictures;

    }

    public ReadWorker(IWeiciyuanDrawable view, String url, FileLocationMethod method) {
        this(view, url, method, false);
    }


    @Override
    protected Bitmap doInBackground(String... url) {

        synchronized (TimeLineBitmapDownloader.pauseReadWorkLock) {
            while (TimeLineBitmapDownloader.pauseReadWork && !isCancelled()) {
                try {
                    TimeLineBitmapDownloader.pauseReadWorkLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (isCancelled())
            return null;

        String path = FileManager.getFilePathFromUrl(data, method);

        boolean downloaded = TaskCache.waitForPictureDownload(data, (SettingUtility.getEnableBigPic() ? downloadListener : null), path, method);

        if (!downloaded) {
            failedResult = FailedResult.downloadFailed;
            return null;
        }

        int height = 0;
        int width = 0;

        switch (method) {
            case avatar_small:
            case avatar_large:
                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width) - Utility.dip2px(5) * 2;
                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height) - Utility.dip2px(5) * 2;
                break;

            case picture_thumbnail:
                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_width);
                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_height);
                break;

            case picture_large:
            case picture_bmiddle:
                if (!isMultiPictures) {
                    DisplayMetrics metrics = globalContext.getDisplayMetrics();

                    float reSize = globalContext.getResources().getDisplayMetrics().density;

                    height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_high_thumbnail_height);
                    //8 is  layout padding
                    width = (int) (metrics.widthPixels - (8 + 8) * reSize);
                } else {
                    height = width = Utility.dip2px(120);
                }
                break;
        }

        synchronized (TimeLineBitmapDownloader.pauseReadWorkLock) {
            while (TimeLineBitmapDownloader.pauseReadWork && !isCancelled()) {
                try {
                    TimeLineBitmapDownloader.pauseReadWorkLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (isCancelled())
            return null;

        Bitmap bitmap;

        switch (method) {
            case avatar_small:
            case avatar_large:
                bitmap = ImageUtility.getRoundedCornerPic(path, width, height, Utility.dip2px(2));
                break;
            default:
                bitmap = ImageUtility.getRoundedCornerPic(path, width, height, 0);
                break;
        }

        if (bitmap == null) {
            this.failedResult = FailedResult.readFailed;
        }
        return bitmap;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (TimeLineBitmapDownloader.pauseDownloadWork)
            return;
        ImageView imageView = viewWeakReference.get();
        if (imageView != null) {
            if (canDisplay(imageView) && pbWeakReference != null) {
                ProgressBar pb = pbWeakReference.get();
                if (pb != null) {
                    Integer progress = values[0];
                    Integer max = values[1];
                    pb.setMax(max);
                    pb.setProgress(progress);
                }
            } else if (isImageViewDrawableBitmap(imageView)) {
                //imageview drawable actually is bitmap, so hide progressbar
                resetProgressBarStatues();
                pbWeakReference = null;
            }
        }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        super.onCancelled(bitmap);
        this.failedResult = FailedResult.taskCanceled;
        displayBitmap(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        displayBitmap(bitmap);
    }

    private void displayBitmap(Bitmap bitmap) {

        ImageView imageView = viewWeakReference.get();
        if (imageView != null) {
            if (canDisplay(imageView)) {
                if (pbWeakReference != null) {
                    ProgressBar pb = pbWeakReference.get();
                    if (pb != null) {
                        pb.setVisibility(View.INVISIBLE);
                    }
                }

                if (bitmap != null) {
                    if (IWeiciyuanDrawable != null)
                        IWeiciyuanDrawable.setGifFlag(ImageUtility.isThisPictureGif(getUrl()));
                    playImageViewAnimation(imageView, bitmap);
                    lruCache.put(data, bitmap);
                } else if (failedResult != null) {
                    switch (failedResult) {
                        case downloadFailed:
                            imageView.setImageDrawable(new ColorDrawable(DebugColor.DOWNLOAD_FAILED));
                            break;
                        case readFailed:
                            imageView.setImageDrawable(new ColorDrawable(DebugColor.PICTURE_ERROR));
                            break;
                        case taskCanceled:
                            imageView.setImageDrawable(new ColorDrawable(DebugColor.DOWNLOAD_CANCEL));
                            break;
                    }

                }


            } else if (isImageViewDrawableBitmap(imageView)) {
                //imageview drawable actually is bitmap, so hide progressbar
                resetProgressBarStatues();
            }
        }


    }

    private void resetProgressBarStatues() {
        if (pbWeakReference == null)
            return;
        ProgressBar pb = pbWeakReference.get();
        if (pb != null) {
            pb.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isImageViewDrawableBitmap(ImageView imageView) {
        return !(imageView.getDrawable() instanceof PictureBitmapDrawable);
    }

    private boolean canDisplay(ImageView view) {
        if (view != null) {
            IPictureWorker bitmapDownloaderTask = getBitmapDownloaderTask(view);
            if (this == bitmapDownloaderTask) {
                return true;
            }
        }
        return false;
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

    private void playImageViewAnimation(final ImageView view, final Bitmap bitmap) {

        view.setImageBitmap(bitmap);
        resetProgressBarStatues();
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(500);
        view.startAnimation(alphaAnimation);
        view.setTag(getUrl());


//        final Animation anim_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.timeline_pic_fade_out);
//        final Animation anim_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.timeline_pic_fade_in);
//
//        anim_out.setAnimationListener(new Animation.AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//                anim_in.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//                    }
//
//                    //clear animation avoid memory leak
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        if (view.getAnimation() != null && view.getAnimation().hasEnded()) {
//                            view.clearAnimation();
//                        }
//                        resetProgressBarStatues();
//                    }
//                });
//
//                if (isImageViewDrawableBitmap(view)) {
//                    resetProgressBarStatues();
//                    return;
//                } else if (!canDisplay(view)) {
//                    return;
//                }
//
//
//                view.setImageBitmap(bitmap);
//                view.setTag(getUrl());
//                view.startAnimation(anim_in);
//
//            }
//        });
//
//        if (view.getAnimation() == null || view.getAnimation().hasEnded())
//            view.startAnimation(anim_out);
    }

    FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
        @Override
        public void pushProgress(int progress, int max) {
            onProgressUpdate(progress, max);
        }

        @Override
        public void completed() {

        }

        @Override
        public void cancel() {

        }
    };
}
