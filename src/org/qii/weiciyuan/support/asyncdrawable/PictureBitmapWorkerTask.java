package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 */
public class PictureBitmapWorkerTask extends MyAsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private final List<WeakReference<ImageView>> viewList = new ArrayList<WeakReference<ImageView>>();
    private Map<String, PictureBitmapWorkerTask> taskMap;
    private GlobalContext globalContext;

    private FileLocationMethod method;

    public String getUrl() {
        return data;
    }

    public PictureBitmapWorkerTask(Map<String, PictureBitmapWorkerTask> taskMap,
                                   ImageView view, String url, FileLocationMethod method) {

        this.globalContext = GlobalContext.getInstance();
        this.lruCache = globalContext.getAvatarCache();
        this.taskMap = taskMap;
        this.viewList.add(new WeakReference<ImageView>(view));
        this.data = url;
        this.method = method;

    }

    public void addView(ImageView view) {
        viewList.add(new WeakReference<ImageView>(view));
    }


    @Override
    protected Bitmap doInBackground(String... url) {

        int avatarWidth = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
        int avatarHeight = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);

        int thumbnailWidth = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_width);
        int thumbnailHeight = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_height);

        if (!isCancelled()) {
            switch (method) {
                case avatar_large:
                    return ImageTool.getRoundedCornerPic(this.data, avatarWidth, avatarHeight, FileLocationMethod.avatar_large);
                case avatar_small:
                    return ImageTool.getRoundedCornerPic(this.data, avatarWidth, avatarHeight, FileLocationMethod.avatar_small);

                case picture_thumbnail:
                    return ImageTool.getRoundedCornerPic(this.data, thumbnailWidth, thumbnailHeight, FileLocationMethod.picture_thumbnail);

                case picture_large:
                    DisplayMetrics metrics = globalContext.getDisplayMetrics();

                    float reSize = globalContext.getResources().getDisplayMetrics().density;

                    int height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_high_thumbnail_height);
                    //8 is  layout padding
                    int width = (int) (metrics.widthPixels - (8 + 8) * reSize);
                    switch (SettingUtility.getHighPicMode()) {
                        case 1:
                            return ImageTool.getMiddlePictureInTimeLine(data, width, height, null);
                        case 2:
                            return ImageTool.getRoundedCornerPic(data, width, height, FileLocationMethod.picture_large);
                        default:
                            throw new IllegalArgumentException();
                    }

            }
        }
        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        displayBitmap(bitmap);
        clean();
        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        super.onPostExecute(bitmap);
        displayBitmap(bitmap);
        clean();
    }

    private void displayBitmap(Bitmap bitmap) {
        for (WeakReference<ImageView> view : viewList) {
            ImageView imageView = view.get();
            if (imageView != null) {
                if (canDisplay(imageView)) {
                    if (bitmap != null) {
                        playImageViewAnimation(imageView, bitmap);
                        lruCache.put(data, bitmap);
                    } else {
                        imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                }
            }

        }
    }

    private boolean canDisplay(ImageView view) {
        if (view != null) {
            PictureBitmapWorkerTask bitmapDownloaderTask = getBitmapDownloaderTask(view);
            if (this == bitmapDownloaderTask) {
                return true;
            }
        }
        return false;
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

    private void clean() {
        if (taskMap != null && taskMap.get(data) != null) {
            taskMap.remove(data);
        }
        viewList.clear();
        taskMap = null;
        lruCache = null;
        globalContext = null;
    }
}
