package org.qii.weiciyuan.support.asyncdrawable;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 */
public class AvatarBitmapWorkerTask extends MyAsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String url = "";
    private final List<WeakReference<ImageView>> viewList = new ArrayList<WeakReference<ImageView>>();
    private Map<String, AvatarBitmapWorkerTask> taskMap;

    private Activity activity;

    public String getUrl() {
        return url;
    }


    public AvatarBitmapWorkerTask(Map<String, AvatarBitmapWorkerTask> taskMap,
                                  ImageView view, String url, Activity activity) {

        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.taskMap = taskMap;
        viewList.add(new WeakReference<ImageView>(view));
        this.url = url;
        this.activity = activity;
    }

    public void addView(ImageView view) {
        viewList.add(new WeakReference<ImageView>(view));
    }

    @Override
    protected Bitmap doInBackground(String... url) {

        if (!isCancelled()) {
            int width = Utility.dip2px(40);
            int height = width;

            if (SettingUtility.getEnableBigAvatar()) {
                return ImageTool.getTimeLineBigAvatarWithRoundedCorner(this.url, width, height);
            } else
                return ImageTool.getSmallAvatarWithRoundedCorner(this.url, width, height);
        }
        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {

        if (taskMap != null && taskMap.get(url) != null) {
            taskMap.remove(url);
        }

        clean();
        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            for (WeakReference<ImageView> view : viewList) {
                if (view != null && view.get() != null) {
                    if (canDisplay(view)) {
                        playImageViewAnimation(view, bitmap);
                        lruCache.put(url, bitmap);
                    }
                }

            }

            if (taskMap != null && taskMap.get(url) != null) {
                taskMap.remove(url);
            }
        }
        clean();
    }

    private boolean canDisplay(WeakReference<ImageView> view) {
        if (view != null && view.get() != null) {
            ImageView imageView = view.get();
            AvatarBitmapWorkerTask bitmapDownloaderTask = getAvatarBitmapDownloaderTask(imageView);
            if (this == bitmapDownloaderTask) {
                return true;
            }
        }
        return false;
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

    private void playImageViewAnimation(final WeakReference<ImageView> view, final Bitmap bitmap) {
        final Animation anim_out = AnimationUtils.loadAnimation(activity, R.anim.timeline_pic_fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(activity, R.anim.timeline_pic_fade_in);

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

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });

                if (canDisplay(view)) {
                    view.get().setImageBitmap(bitmap);
                    view.get().startAnimation(anim_in);
                }
            }
        });

        view.get().startAnimation(anim_out);
    }

    private void clean() {
        viewList.clear();
        activity = null;
        taskMap = null;
        lruCache = null;
    }
}
