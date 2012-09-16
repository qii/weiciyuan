package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.io.File;

/**
 * User: qii
 * Date: 12-8-18
 */
public class BrowserBigPicActivity extends AbstractAppActivity {

    private String url;
    private WebView imageView;
    private ProgressBar pb;
    private FrameLayout fl;
    private PicSimpleBitmapWorkerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_listview_pic_big_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.browser_picture);


        pb = (ProgressBar) findViewById(R.id.pb);
        fl = (FrameLayout) findViewById(R.id.fl);
        imageView = (WebView) findViewById(R.id.iv);
//        imageView.getSettings().setSupportZoom(true);
//        imageView.getSettings().setBuiltInZoomControls(true);
//        imageView.getSettings().setDisplayZoomControls(false);
        imageView.setBackgroundColor(getResources().getColor(R.color.transparent));
//        imageView.getSettings().setUseWideViewPort(true);


        url = getIntent().getStringExtra("url");
        if (task == null || task.getStatus() == MyAsyncTask.Status.FINISHED) {
            task = new PicSimpleBitmapWorkerTask();
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class PicSimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(String... dd) {

            FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
                @Override
                public void pushProgress(int progress, int max) {
                    publishProgress(progress, max);
                }
            };

            if (!isCancelled()) {
                return ImageTool.getLargePictureWithoutRoundedCorner(url, downloadListener);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            int max = values[1];
            pb.setIndeterminate(false);
            pb.setMax(max);
            pb.setProgress(progress);
        }

        @Override
        protected void onCancelled(String bitmap) {
            if (bitmap != null) {


            }

            super.onCancelled(bitmap);
        }

        @Override
        protected void onPostExecute(final String bitmap) {

            if (bitmap != null) {


                pb.setVisibility(View.GONE);
//                imageView.loadUrl("file://" + bitmap);

                File file = new File(bitmap);

                AppLogger.e(file.getParent());
                AppLogger.e(file.getName());


                imageView.loadDataWithBaseURL("file://" + file.getParent() + "/", "<html><center><img src=\"" + file.getName() + "\"></html>", "text/html", "utf-8", "");


                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(bitmap, options);
                int width = options.outWidth;
                int height = options.outHeight;
                getActionBar().setTitle(getActionBar().getTitle() + "(" + String.valueOf(width) + "x" + String.valueOf(height) + ")");


            } else {
                pb.setVisibility(View.GONE);
                int[] attrs = new int[]{R.attr.error};
                TypedArray ta = BrowserBigPicActivity.this.obtainStyledAttributes(attrs);
                Drawable drawableFromTheme = ta.getDrawable(0);
                //                imageView.setImageDrawable(drawableFromTheme);
            }

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (task != null)
            task.cancel(true);
        imageView.loadUrl("about:blank");
        imageView.stopLoading();
        imageView = null;
    }
}
