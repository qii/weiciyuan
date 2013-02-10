package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 13-2-9
 * reuse download worker or create a new download worker
 */
public class ReadWorker extends MyAsyncTask<String, Void, Bitmap> implements IPictureWorker {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private WeakReference<ImageView> viewWeakReference;
    private GlobalContext globalContext;
    private FileLocationMethod method;
    private FailedResult failedResult;

    public String getUrl() {
        return data;
    }

    public ReadWorker(ImageView view, String url, FileLocationMethod method) {

        this.globalContext = GlobalContext.getInstance();
        this.lruCache = globalContext.getAvatarCache();
        this.viewWeakReference = new WeakReference<ImageView>(view);
        this.data = url;
        this.method = method;

    }


    @Override
    protected Bitmap doInBackground(String... url) {

        if (isCancelled())
            return null;

        String path = FileManager.getFilePathFromUrl(data, method);

        boolean downloaded = TaskCache.waitForPictureDownload(data, path, method);
        if (!downloaded) {
            failedResult = FailedResult.downloadFailed;
            return null;
        }

        int height = 0;
        int width = 0;

        switch (method) {
            case avatar_large:
                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);
                break;
            case avatar_small:
                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);
                break;

            case picture_thumbnail:
                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_width);
                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_height);
                break;

            case picture_large:
                DisplayMetrics metrics = globalContext.getDisplayMetrics();

                float reSize = globalContext.getResources().getDisplayMetrics().density;

                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_high_thumbnail_height);
                //8 is  layout padding
                width = (int) (metrics.widthPixels - (8 + 8) * reSize);
        }


        Bitmap bitmap = ImageTool.getRoundedCornerPic(path, width, height);
        if (bitmap == null) {
            this.failedResult = FailedResult.readFailed;
        }
        return bitmap;
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
                if (bitmap != null) {
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
            }
        }


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
        final Animation anim_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.timeline_pic_fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.timeline_pic_fade_in);

        anim_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    //clear animation avoid memory leak
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (view.getAnimation() != null && view.getAnimation().hasEnded()) {
                            view.clearAnimation();
                        }
                    }
                });

                if (canDisplay(view)) {
                    view.setImageBitmap(bitmap);
                    view.startAnimation(anim_in);
                }
            }
        });
        if (view.getAnimation() == null || view.getAnimation().hasEnded())
            view.startAnimation(anim_out);
    }

}
