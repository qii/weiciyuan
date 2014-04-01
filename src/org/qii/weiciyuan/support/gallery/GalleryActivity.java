package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.asyncdrawable.TaskCache;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.CircleProgressView;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.SmileyPickerUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * User: qii
 * Date: 13-7-28
 */
@Deprecated
public class GalleryActivity extends Activity {

    private static final int IMAGEVIEW_SOFT_LAYER_MAX_WIDTH = 2000;

    private static final int IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT = 3000;

    private static final int STATUS_BAR_HEIGHT_DP_UNIT = 25;

    private static final int NAVIGATION_BAR_HEIGHT_DP_UNIT = 48;

    private static final String CURRENT_VISIBLE_PAGE = "currentPage";

    private ArrayList<String> urls = new ArrayList<String>();

    private TextView position;

    private HashMap<String, PicSimpleBitmapWorkerTask> taskMap
            = new HashMap<String, PicSimpleBitmapWorkerTask>();

    private PicSaveTask saveTask;

    private ViewPager pager;

    private HashSet<ViewGroup> unRecycledViews = new HashSet<ViewGroup>();

    private boolean alreadyShowPicturesTooLargeHint = false;

    private ImageView animationView;

    private View currentViewPositionLayout;

    private Rect rect;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity_layout);

        animationView = (ImageView) findViewById(R.id.animation);
        currentViewPositionLayout = findViewById(R.id.position_layout);

        position = (TextView) findViewById(R.id.position);
        TextView sum = (TextView) findViewById(R.id.sum);

        rect = getIntent().getParcelableExtra("rect");

        MessageBean msg = getIntent().getParcelableExtra("msg");
        ArrayList<String> tmp = msg.getThumbnailPicUrls();
        for (int i = 0; i < tmp.size(); i++) {
            urls.add(tmp.get(i).replace("thumbnail", "large"));
        }
        sum.setText(String.valueOf(urls.size()));

        //jump to new gallery animation activity
        if (urls.size() == 1 && rect != null && ImageUtility.isThisBitmapCanRead(
                FileManager.getFilePathFromUrl(urls.get(0), FileLocationMethod.picture_large))
                && !ImageUtility.isThisBitmapTooLargeToRead(
                FileManager.getFilePathFromUrl(urls.get(0), FileLocationMethod.picture_large))) {
            Intent intent = new Intent(this, GalleryAnimationActivity.class);
            intent.putExtra("msg", getIntent().getParcelableExtra("msg"));
            intent.putExtra("rect", getIntent().getParcelableExtra("rect"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImagePagerAdapter());
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                GalleryActivity.this.position.setText(String.valueOf(position + 1));
            }
        });
        pager.setCurrentItem(getIntent().getIntExtra("position", 0));
        pager.setOffscreenPageLimit(1);
        pager.setPageTransformer(true, new ZoomOutPageTransformer());
        pager.setPadding(0, Utility.dip2px(STATUS_BAR_HEIGHT_DP_UNIT), 0, 0);
    }

    @Override
    public void onBackPressed() {

        if (rect == null || urls.size() > 1) {
            super.onBackPressed();
            return;
        }

        View view = pager.findViewWithTag(CURRENT_VISIBLE_PAGE);

        final PhotoView imageView = (PhotoView) view.findViewById(R.id.image);

        if (imageView == null
                || (!(imageView.getDrawable() instanceof BitmapDrawable))) {
            super.onBackPressed();
            return;
        }

        animateClose(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (String url : urls) {
            MyAsyncTask task = taskMap.get(url);
            if (task != null) {
                task.cancel(true);
            }
        }
        if (pager != null && unRecycledViews != null) {
            Utility.recycleViewGroupAndChildViews(pager, true);
            for (ViewGroup viewGroup : unRecycledViews) {
                Utility.recycleViewGroupAndChildViews(viewGroup, true);
            }

            System.gc();
        }
    }


    private void animateClose(PhotoView imageView) {
        currentViewPositionLayout.setVisibility(View.INVISIBLE);
        animationView.setImageDrawable(imageView.getDrawable());

        pager.setVisibility(View.INVISIBLE);

        final Rect startBounds = rect;
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        animationView.getGlobalVisibleRect(finalBounds, globalOffset);

        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        animationView.setPivotX(0f);
        animationView.setPivotY(0f);

        final float startScaleFinal = startScale;

        animationView.animate().setInterpolator(new DecelerateInterpolator()).x(startBounds.left)
                .y(startBounds.top).scaleY(startScaleFinal).scaleX(startScaleFinal).setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        GalleryActivity.this.finish();
                        overridePendingTransition(0, 0);
                    }
                }).start();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof ViewGroup) {
                ((ViewPager) container).removeView((View) object);
                unRecycledViews.remove(object);
                ViewGroup viewGroup = (ViewGroup) object;
                Utility.recycleViewGroupAndChildViews(viewGroup, true);

            }
//            ((ViewPager) container).removeView((View) object);
        }


        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View contentView = inflater.inflate(R.layout.galleryactivity_item, view, false);

            handlePage(position, contentView, true);

            ((ViewPager) view).addView(contentView, 0);
            unRecycledViews.add((ViewGroup) contentView);
            return contentView;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            View contentView = (View) object;
            if (contentView == null) {
                return;
            }

            contentView.setTag(CURRENT_VISIBLE_PAGE);

            if (SettingUtility.allowClickToCloseGallery()) {
                contentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }

            ImageView imageView = (ImageView) contentView.findViewById(R.id.image);

            if (imageView.getDrawable() != null) {
                return;
            }

            handlePage(position, contentView, false);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }


    }

    private void handlePage(int position, View contentView, boolean fromInstantiateItem) {

        final PhotoView imageView = (PhotoView) contentView.findViewById(R.id.image);
        imageView.setVisibility(View.INVISIBLE);

        if (SettingUtility.allowClickToCloseGallery()) {
            imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {

                    if (rect == null
                            || imageView == null
                            || (!(imageView.getDrawable() instanceof BitmapDrawable))) {
                        GalleryActivity.this.finish();
                        return;
                    }

                    animateClose(imageView);

                }

            });
        }

        WebView gif = (WebView) contentView.findViewById(R.id.gif);
        gif.setBackgroundColor(getResources().getColor(R.color.transparent));
        gif.setVisibility(View.INVISIBLE);

        WebView large = (WebView) contentView.findViewById(R.id.large);
        large.setBackgroundColor(getResources().getColor(R.color.transparent));
        large.setVisibility(View.INVISIBLE);
        large.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (Utility.doThisDeviceOwnNavigationBar(GalleryActivity.this)) {
            imageView.setPadding(0, 0, 0,
                    Utility.dip2px(NAVIGATION_BAR_HEIGHT_DP_UNIT));
            //webview has a bug, padding is ignored
            gif.setPadding(0, 0, 0,
                    Utility.dip2px(NAVIGATION_BAR_HEIGHT_DP_UNIT));
            large.setPadding(0, 0, 0,
                    Utility.dip2px(NAVIGATION_BAR_HEIGHT_DP_UNIT));
        }

        TextView wait = (TextView) contentView.findViewById(R.id.wait);

        TextView readError = (TextView) contentView.findViewById(R.id.error);

        String path = FileManager
                .getFilePathFromUrl(urls.get(position), FileLocationMethod.picture_large);

        boolean shouldDownLoadPicture = !fromInstantiateItem || (fromInstantiateItem && Utility
                .isWifi(GalleryActivity.this));

        //sometime picture is not downloaded completely, but android already can read it....
        if (ImageUtility.isThisBitmapCanRead(path)
                && taskMap.get(urls.get(position)) == null
                && TaskCache.isThisUrlTaskFinished(urls.get(position))) {
            wait.setVisibility(View.INVISIBLE);
            readPicture(imageView, gif, large, readError, urls.get(position), path);

        } else if (shouldDownLoadPicture) {

            final CircleProgressView spinner = (CircleProgressView) contentView
                    .findViewById(R.id.loading);
            spinner.setVisibility(View.VISIBLE);

            if (taskMap.get(urls.get(position)) == null) {
                wait.setVisibility(View.VISIBLE);
                PicSimpleBitmapWorkerTask task = new PicSimpleBitmapWorkerTask(imageView, gif,
                        large, spinner, wait, readError, urls.get(position), taskMap);
                taskMap.put(urls.get(position), task);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                PicSimpleBitmapWorkerTask task = taskMap.get(urls.get(position));
                task.setWidget(imageView, gif, spinner, wait, readError);
            }
        }
    }


    private class PicSimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, String> {

        private FileDownloaderHttpHelper.DownloadListener downloadListener
                = new FileDownloaderHttpHelper.DownloadListener() {
            @Override
            public void pushProgress(int progress, int max) {
                publishProgress(progress, max);
            }


        };

        public void setWidget(ImageView iv, WebView gif, CircleProgressView spinner, TextView wait,
                TextView readError) {
            this.iv = iv;
            this.spinner = spinner;
            this.wait = wait;
            this.readError = readError;
            this.gif = gif;
        }

        private ImageView iv;

        private WebView gif;

        private WebView large;

        private TextView wait;

        private String url;

        private CircleProgressView spinner;

        private TextView readError;

        private HashMap<String, PicSimpleBitmapWorkerTask> taskMap;

        public PicSimpleBitmapWorkerTask(ImageView iv, WebView gif, WebView large,
                CircleProgressView spinner, TextView wait,
                TextView readError, String url,
                HashMap<String, PicSimpleBitmapWorkerTask> taskMap) {
            this.iv = iv;
            this.url = url;
            this.spinner = spinner;
            this.readError = readError;
            this.taskMap = taskMap;
            this.gif = gif;
            this.large = large;
            this.wait = wait;
            this.readError.setVisibility(View.INVISIBLE);
            this.spinner.setVisibility(View.VISIBLE);

        }


        @Override
        protected String doInBackground(String... dd) {
            if (isCancelled()) {
                return null;
            }

            boolean downloaded = TaskCache.waitForMsgDetailPictureDownload(url, downloadListener);
            if (downloaded) {
                return FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_large);
            } else {
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            this.wait.setVisibility(View.INVISIBLE);
            int progress = values[0];
            int max = values[1];
            spinner.setMax(max);
            spinner.setProgress(progress);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            taskMap.remove(url);
            this.spinner.setVisibility(View.INVISIBLE);
            this.wait.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(final String bitmapPath) {

            this.spinner.setVisibility(View.INVISIBLE);
            this.wait.setVisibility(View.INVISIBLE);

            if (isCancelled()) {
                return;
            }

            taskMap.remove(url);

            if (TextUtils.isEmpty(bitmapPath) || iv == null) {

                readError.setVisibility(View.VISIBLE);
                readError.setText(getString(R.string.picture_cant_download_or_sd_cant_read));
                return;
            } else {
                readError.setVisibility(View.INVISIBLE);
            }

            if (!ImageUtility.isThisBitmapCanRead(bitmapPath)) {
                Toast.makeText(GalleryActivity.this,
                        R.string.download_finished_but_cant_read_picture_file, Toast.LENGTH_SHORT)
                        .show();
            }

            readPicture(iv, gif, large, readError, url, bitmapPath);


        }
    }


    private void readPicture(final ImageView imageView, WebView gif, WebView large,
            final TextView readError, final String url, final String bitmapPath) {

        if (bitmapPath.endsWith(".gif")) {
            readGif(gif, large, readError, url, bitmapPath);
            return;
        }

        if (!ImageUtility.isThisBitmapCanRead(bitmapPath)) {
            Toast.makeText(GalleryActivity.this,
                    R.string.download_finished_but_cant_read_picture_file, Toast.LENGTH_SHORT)
                    .show();
        }

        boolean isThisBitmapTooLarge = ImageUtility.isThisBitmapTooLargeToRead(bitmapPath);
        if (isThisBitmapTooLarge && !alreadyShowPicturesTooLargeHint) {
//            Toast.makeText(GalleryActivity.this,
//                    R.string.picture_is_too_large_so_enable_software_layer, Toast.LENGTH_LONG)
//                    .show();
            alreadyShowPicturesTooLargeHint = true;
        }

        if (isThisBitmapTooLarge) {
            readLarge(large, url, bitmapPath);
            return;
        }

        //ImageView already have bitmap, ignore it
        if (imageView.getDrawable() != null) {
            return;
        }

        new MyAsyncTask<Void, Bitmap, Bitmap>() {

            //todo
            //when I finish new ImageView in the future, I will refactor these code....
            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bitmap = null;
                try {
                    bitmap = ImageUtility
                            .decodeBitmapFromSDCard(bitmapPath, IMAGEVIEW_SOFT_LAYER_MAX_WIDTH,
                                    IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT);
                } catch (OutOfMemoryError ignored) {
                    GlobalContext.getInstance().getBitmapCache().evictAll();
                    try {
                        bitmap = ImageUtility
                                .decodeBitmapFromSDCard(bitmapPath, IMAGEVIEW_SOFT_LAYER_MAX_WIDTH,
                                        IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT);
                    } catch (OutOfMemoryError ignoredToo) {

                    }
                }

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                if (imageView.getDrawable() != null) {
                    return;
                }

                if (bitmap != null) {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                    bindImageViewLongClickListener(imageView, url, bitmapPath);
                    readError.setVisibility(View.INVISIBLE);
                } else {
                    readError.setText(getString(R.string.picture_read_failed));
                    imageView.setVisibility(View.INVISIBLE);
                    readError.setVisibility(View.VISIBLE);
                }
            }
        }.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void readGif(WebView webView, WebView large, TextView readError, String url,
            String bitmapPath) {
        readError.setVisibility(View.INVISIBLE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapPath, options);

        int picWidth = options.outWidth;
        int picHeight = options.outHeight;
        int availableWidth = Utility.getScreenWidth()
                - getResources().getDimensionPixelOffset(R.dimen.normal_gif_webview_margin_left)
                - getResources().getDimensionPixelOffset(R.dimen.normal_gif_webview_margin_right);
        int availableHeight = SmileyPickerUtility.getAppHeight(GalleryActivity.this);

        int maxPossibleResizeHeight = availableWidth * availableHeight / picWidth;

        if (picWidth >= availableWidth || picHeight >= availableHeight
                || maxPossibleResizeHeight >= availableHeight) {
            readLarge(large, url, bitmapPath);
            return;
        }

        webView.setVisibility(View.VISIBLE);
        bindImageViewLongClickListener(((View) webView.getParent()), url, bitmapPath);

        if (webView.getTag() != null) {
            return;
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(false);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        File file = new File(bitmapPath);
        String str1 = "file://" + file.getAbsolutePath().replace("/mnt/sdcard/", "/sdcard/");
        String str2 =
                "<html>\n<head>\n     <style>\n          html,body{background:transparent;margin:0;padding:0;}          *{-webkit-tap-highlight-color:rgba(0, 0, 0, 0);}\n     </style>\n     <script type=\"text/javascript\">\n     var imgUrl = \""
                        + str1 + "\";" + "     var objImage = new Image();\n"
                        + "     var realWidth = 0;\n" + "     var realHeight = 0;\n" + "\n"
                        + "     function onLoad() {\n"
                        + "          objImage.onload = function() {\n"
                        + "               realWidth = objImage.width;\n"
                        + "               realHeight = objImage.height;\n" + "\n"
                        + "               document.gagImg.src = imgUrl;\n"
                        + "               onResize();\n" + "          }\n"
                        + "          objImage.src = imgUrl;\n" + "     }\n" + "\n"
                        + "     function onResize() {\n" + "          var scale = 1;\n"
                        + "          var newWidth = document.gagImg.width;\n"
                        + "          if (realWidth > newWidth) {\n"
                        + "               scale = realWidth / newWidth;\n" + "          } else {\n"
                        + "               scale = newWidth / realWidth;\n" + "          }\n" + "\n"
                        + "          hiddenHeight = Math.ceil(30 * scale);\n"
                        + "          document.getElementById('hiddenBar').style.height = hiddenHeight + \"px\";\n"
                        + "          document.getElementById('hiddenBar').style.marginTop = -hiddenHeight + \"px\";\n"
                        + "     }\n" + "     </script>\n" + "</head>\n"
                        + "<body onload=\"onLoad()\" onresize=\"onResize()\" onclick=\"Android.toggleOverlayDisplay();\">\n"
                        + "     <table style=\"width: 100%;height:100%;\">\n"
                        + "          <tr style=\"width: 100%;\">\n"
                        + "               <td valign=\"middle\" align=\"center\" style=\"width: 100%;\">\n"
                        + "                    <div style=\"display:block\">\n"
                        + "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" />\n"
                        + "                    </div>\n"
                        + "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 100%; background: transparent;\"></div>\n"
                        + "               </td>\n" + "          </tr>\n" + "     </table>\n"
                        + "</body>\n" + "</html>";
        webView.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8", null);

        webView.setTag(new Object());
    }

    private void readLarge(WebView large, String url, String bitmapPath) {
        large.setVisibility(View.VISIBLE);
        bindImageViewLongClickListener(large, url, bitmapPath);
        if (SettingUtility.allowClickToCloseGallery()) {
            large.setOnTouchListener(largeOnTouchListener);
        }

        if (large.getTag() != null) {
            return;
        }

        large.getSettings().setJavaScriptEnabled(true);
        large.getSettings().setUseWideViewPort(true);
        large.getSettings().setLoadWithOverviewMode(true);
        large.getSettings().setBuiltInZoomControls(true);
        large.getSettings().setDisplayZoomControls(false);

        large.setVerticalScrollBarEnabled(false);
        large.setHorizontalScrollBarEnabled(false);

        File file = new File(bitmapPath);

        String str1 = "file://" + file.getAbsolutePath().replace("/mnt/sdcard/", "/sdcard/");
        String str2 =
                "<html>\n<head>\n     <style>\n          html,body{background:transparent;margin:0;padding:0;}          *{-webkit-tap-highlight-color:rgba(0, 0, 0, 0);}\n     </style>\n     <script type=\"text/javascript\">\n     var imgUrl = \""
                        + str1 + "\";" + "     var objImage = new Image();\n"
                        + "     var realWidth = 0;\n" + "     var realHeight = 0;\n" + "\n"
                        + "     function onLoad() {\n"
                        + "          objImage.onload = function() {\n"
                        + "               realWidth = objImage.width;\n"
                        + "               realHeight = objImage.height;\n" + "\n"
                        + "               document.gagImg.src = imgUrl;\n"
                        + "               onResize();\n" + "          }\n"
                        + "          objImage.src = imgUrl;\n" + "     }\n" + "\n"
                        + "     function onResize() {\n" + "          var scale = 1;\n"
                        + "          var newWidth = document.gagImg.width;\n"
                        + "          if (realWidth > newWidth) {\n"
                        + "               scale = realWidth / newWidth;\n" + "          } else {\n"
                        + "               scale = newWidth / realWidth;\n" + "          }\n" + "\n"
                        + "          hiddenHeight = Math.ceil(30 * scale);\n"
                        + "          document.getElementById('hiddenBar').style.height = hiddenHeight + \"px\";\n"
                        + "          document.getElementById('hiddenBar').style.marginTop = -hiddenHeight + \"px\";\n"
                        + "     }\n" + "     </script>\n" + "</head>\n"
                        + "<body onload=\"onLoad()\" onresize=\"onResize()\" onclick=\"Android.toggleOverlayDisplay();\">\n"
                        + "     <table style=\"width: 100%;height:100%;\">\n"
                        + "          <tr style=\"width: 100%;\">\n"
                        + "               <td valign=\"middle\" align=\"center\" style=\"width: 100%;\">\n"
                        + "                    <div style=\"display:block\">\n"
                        + "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" />\n"
                        + "                    </div>\n"
                        + "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 100%; background: transparent;\"></div>\n"
                        + "               </td>\n" + "          </tr>\n" + "     </table>\n"
                        + "</body>\n" + "</html>";
        large.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8", null);
        large.setVisibility(View.VISIBLE);

        large.setTag(new Object());
    }

    private View.OnTouchListener largeOnTouchListener = new View.OnTouchListener() {
        boolean mPressed;

        boolean mClose;

        CheckForSinglePress mPendingCheckForSinglePress;

        long lastTime = 0;

        float[] location = new float[2];

        class CheckForSinglePress implements Runnable {

            View view;

            public CheckForSinglePress(View view) {
                this.view = view;
            }

            public void run() {
                if (!mPressed && mClose) {
                    Utility.playClickSound(view);
                    finish();
                }
            }

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mPendingCheckForSinglePress = new CheckForSinglePress(v);
                    mPressed = true;
                    if (System.currentTimeMillis() - lastTime
                            > ViewConfiguration.getDoubleTapTimeout() + 100) {
                        mClose = true;
                        new Handler().postDelayed(mPendingCheckForSinglePress,
                                ViewConfiguration.getDoubleTapTimeout() + 100);
                    } else {
                        mClose = false;
                    }
                    lastTime = System.currentTimeMillis();

                    location[0] = event.getRawX();
                    location[1] = event.getRawY();

                    break;
                case MotionEvent.ACTION_UP:
                    mPressed = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mClose = false;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX();
                    float y = event.getRawY();
                    if (Math.abs(location[0] - x) > 5.0f && Math.abs(location[1] - y) > 5.0f) {
                        mClose = false;
                    }
                    break;
            }

            return false;
        }
    };


    private void bindImageViewLongClickListener(View view, final String url,
            final String filePath) {

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String[] values = {getString(R.string.copy_link_to_clipboard),
                        getString(R.string.share), getString(R.string.save_pic_album)};

                new AlertDialog.Builder(GalleryActivity.this)
                        .setItems(values, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        ClipboardManager cm = (ClipboardManager) getSystemService(
                                                Context.CLIPBOARD_SERVICE);
                                        cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", url));
                                        Toast.makeText(GalleryActivity.this,
                                                getString(R.string.copy_successfully),
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                        sharingIntent.setType("image/jpeg");
                                        if (!TextUtils.isEmpty(filePath)) {
                                            Uri uri = Uri.fromFile(new File(filePath));
                                            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                            if (Utility.isIntentSafe(GalleryActivity.this,
                                                    sharingIntent)) {
                                                startActivity(Intent.createChooser(sharingIntent,
                                                        getString(R.string.share)));
                                            }
                                        }
                                        break;
                                    case 2:
                                        saveBitmapToPictureDir(filePath);
                                        break;
                                }
                            }
                        }).show();

                return true;
            }
        });
    }


    private void saveBitmapToPictureDir(String filePath) {
        if (Utility.isTaskStopped(saveTask)) {
            saveTask = new PicSaveTask(filePath);
            saveTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

    }


    private class PicSaveTask extends MyAsyncTask<Void, Boolean, Boolean> {

        String path;

        public PicSaveTask(String path) {
            this.path = path;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return FileManager.saveToPicDir(path);
        }


        @Override
        protected void onPostExecute(Boolean value) {
            super.onPostExecute(value);
            if (value) {
                Toast.makeText(GalleryActivity.this, getString(R.string.save_to_album_successfully),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GalleryActivity.this, getString(R.string.cant_save_pic),
                        Toast.LENGTH_SHORT).show();
            }
        }


    }
}