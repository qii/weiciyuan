//package org.qii.weiciyuan.ui.browser;
//
//import android.app.Activity;
//import android.content.ClipData;
//import android.content.ClipboardManager;
//import android.content.Context;
//import android.content.Intent;
//import android.content.res.TypedArray;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.TextUtils;
//import android.view.*;
//import android.webkit.WebView;
//import android.widget.ImageButton;
//import android.widget.Toast;
//import org.qii.weiciyuan.R;
//import org.qii.weiciyuan.bean.MessageBean;
//import org.qii.weiciyuan.support.asyncdrawable.TaskCache;
//import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
//import org.qii.weiciyuan.support.file.FileLocationMethod;
//import org.qii.weiciyuan.support.file.FileManager;
//import org.qii.weiciyuan.support.imageutility.ImageUtility;
//import org.qii.weiciyuan.support.lib.CheatSheet;
//import org.qii.weiciyuan.support.lib.CircleProgressView;
//import org.qii.weiciyuan.support.lib.MyAsyncTask;
//import org.qii.weiciyuan.support.utils.Utility;
//
//import java.io.File;
//
///**
// * User: qii
// * Date: 12-8-18
// */
//public class BrowserBigPicActivity extends Activity {
//
//    private PicSimpleBitmapWorkerTask task;
//    private PicSaveTask saveTask;
//    private String path;
//    private MessageBean msg;
//    private Layout layout;
//    private boolean hd;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setBackgroundDrawable(new ColorDrawable(0));
//        setContentView(R.layout.browserbigpicactivity_layout);
//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.width = getResources().getDimensionPixelOffset(R.dimen.browser_pic_activity_width);
//        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
//
//        layout = new Layout();
//        layout.refresh = (ImageButton) findViewById(R.id.ib_refresh);
//        layout.copy = (ImageButton) findViewById(R.id.ib_copy);
//        layout.share = (ImageButton) findViewById(R.id.ib_share);
//        layout.save = (ImageButton) findViewById(R.id.ib_save);
//        layout.hd = (ImageButton) findViewById(R.id.ib_hd);
//
//        layout.pb = (CircleProgressView) findViewById(R.id.pb);
//
//        layout.webView = (WebView) findViewById(R.id.iv);
//
//        layout.webView.setBackgroundColor(getResources().getColor(R.color.transparent));
//        layout.webView.getSettings().setJavaScriptEnabled(true);
//        layout.webView.getSettings().setUseWideViewPort(true);
//        layout.webView.getSettings().setLoadWithOverviewMode(true);
//        layout.webView.getSettings().setBuiltInZoomControls(true);
//        layout.webView.getSettings().setDisplayZoomControls(false);
//
//        layout.webView.setVerticalScrollBarEnabled(false);
//        layout.webView.setHorizontalScrollBarEnabled(false);
//        layout.webView.setOnTouchListener(new View.OnTouchListener() {
//            boolean mPressed;
//            boolean mClose;
//            CheckForSinglePress mPendingCheckForSinglePress = new CheckForSinglePress();
//            long lastTime = 0;
//            float[] location = new float[2];
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getActionMasked()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mPressed = true;
//                        if (System.currentTimeMillis() - lastTime > ViewConfiguration.getDoubleTapTimeout() + 100) {
//                            mClose = true;
//                            new Handler().postDelayed(mPendingCheckForSinglePress,
//                                    ViewConfiguration.getDoubleTapTimeout() + 100);
//                        } else {
//                            mClose = false;
//                        }
//                        lastTime = System.currentTimeMillis();
//
//                        location[0] = event.getRawX();
//                        location[1] = event.getRawY();
//
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        mPressed = false;
//                        break;
//                    case MotionEvent.ACTION_CANCEL:
//                        mPressed = false;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        float x = event.getRawX();
//                        float y = event.getRawY();
//                        if (Math.abs(location[0] - x) > 5.0f && Math.abs(location[1] - y) > 5.0f) {
//                            mClose = false;
//                        }
//                        break;
//                }
//
//                return false;
//            }
//
//
//            class CheckForSinglePress implements Runnable {
//
//                public void run() {
//
//                    if (!mPressed && mClose)
//                        finish();
//
//                }
//
//            }
//        });
//
//        msg = (MessageBean) getIntent().getParcelableExtra("msg");
//        if (Utility.isTaskStopped(task)) {
//            task = new PicSimpleBitmapWorkerTask(hd);
//            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//        }
//
//        initOnClickListener();
//
//        String largePath = FileManager.getFilePathFromUrl(msg.getOriginal_pic(), FileLocationMethod.picture_large);
//        if (new File(largePath).exists()) {
//            layout.hd.setVisibility(View.GONE);
//        }
//    }
//
//    private void initOnClickListener() {
//        layout.refresh.setOnClickListener(onClickListener);
//        layout.copy.setOnClickListener(onClickListener);
//        layout.share.setOnClickListener(onClickListener);
//        layout.save.setOnClickListener(onClickListener);
//        layout.hd.setOnClickListener(onClickListener);
//
//        CheatSheet.setup(this, layout.refresh, R.string.refresh);
//        CheatSheet.setup(this, layout.copy, R.string.copy_link_to_clipboard);
//        CheatSheet.setup(this, layout.share, R.string.share);
//        CheatSheet.setup(this, layout.save, R.string.save_pic_album);
//        CheatSheet.setup(this, layout.hd, R.string.switch_to_ori_pic);
//    }
//
//    private View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.ib_refresh:
//                    if (task != null) {
//                        task.cancel(true);
//                    }
//
//                    if (!TextUtils.isEmpty(path)) {
//                        new File(path).delete();
//                    }
//
//                    task = new PicSimpleBitmapWorkerTask(hd);
//
//                    task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//
//                    break;
//                case R.id.ib_copy:
//                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", msg.getOriginal_pic()));
//                    Toast.makeText(BrowserBigPicActivity.this, getString(R.string.copy_successfully), Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.ib_share:
//                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                    sharingIntent.setType("image/jpeg");
//                    if (!TextUtils.isEmpty(path)) {
//                        Uri uri = Uri.fromFile(new File(path));
//                        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                        if (Utility.isIntentSafe(BrowserBigPicActivity.this, sharingIntent)) {
//                            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));
//                        }
//                    }
//                    break;
//                case R.id.ib_save:
//                    if (task != null && task.getStatus() == MyAsyncTask.Status.FINISHED) {
//                        if (saveTask == null) {
//                            saveTask = new PicSaveTask();
//                            saveTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//                        } else if (saveTask.getStatus() == MyAsyncTask.Status.FINISHED) {
//                            Toast.makeText(BrowserBigPicActivity.this, getString(R.string.already_saved), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    break;
//                case R.id.ib_hd:
//                    if (task != null) {
//                        task.cancel(true);
//                    }
//                    hd = true;
//                    task = new PicSimpleBitmapWorkerTask(hd);
//                    task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//                    path = "";
//                    break;
//            }
//        }
//    };
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelable("msg", msg);
//    }
//
//
//    private class PicSaveTask extends MyAsyncTask<Void, Boolean, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            return FileManager.saveToPicDir(path);
//        }
//
//
//        @Override
//        protected void onPostExecute(Boolean value) {
//            super.onPostExecute(value);
//            saveTask = null;
//            if (value)
//                Toast.makeText(BrowserBigPicActivity.this, getString(R.string.save_to_album_successfully), Toast.LENGTH_SHORT).show();
//            else
//                Toast.makeText(BrowserBigPicActivity.this, getString(R.string.cant_save_pic), Toast.LENGTH_SHORT).show();
//        }
//
//
//    }
//
//    class PicSimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, String> {
//
//        FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
//            @Override
//            public void pushProgress(int progress, int max) {
//                publishProgress(progress, max);
//            }
//
//
//        };
//
//        boolean hd;
//
//        public PicSimpleBitmapWorkerTask(boolean hd) {
//            this.hd = hd;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            pb.setIndeterminate(true);
//            layout.pb.setVisibility(View.VISIBLE);
//            layout.webView.setVisibility(View.INVISIBLE);
//        }
//
//        @Override
//        protected String doInBackground(String... dd) {
//            if (isCancelled()) {
//                return null;
//            }
//
//            TaskCache.waitForMsgDetailPictureDownload(msg, downloadListener);
//
//            String middlePath = FileManager.getFilePathFromUrl(msg.getBmiddle_pic(), FileLocationMethod.picture_bmiddle);
//            String largePath = FileManager.getFilePathFromUrl(msg.getOriginal_pic(), FileLocationMethod.picture_large);
//            if (new File(largePath).exists()) {
//                return largePath;
//            } else if (!this.hd && new File(middlePath).exists()) {
//                return middlePath;
//            } else {
//                String data = (this.hd ? msg.getOriginal_pic() : msg.getBmiddle_pic());
//                FileLocationMethod method = (this.hd ? FileLocationMethod.picture_large : FileLocationMethod.picture_bmiddle);
//                return ImageUtility.getLargePictureWithoutRoundedCorner(data, downloadListener, method);
//
//            }
//
//
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//            int progress = values[0];
//            int max = values[1];
////            pb.setIndeterminate(false);
//            layout.pb.setMax(max);
//            layout.pb.setProgress(progress);
//        }
//
//        @Override
//        protected void onCancelled(String bitmap) {
//            if (bitmap != null) {
//
//
//            }
//
//            super.onCancelled(bitmap);
//        }
//
//        @Override
//        protected void onPostExecute(final String bitmapPath) {
//
//            if (!TextUtils.isEmpty(bitmapPath)) {
//                path = bitmapPath;
//
//                File file = new File(bitmapPath);
//
//                String str1 = "file://" + file.getAbsolutePath().replace("/mnt/sdcard/", "/sdcard/");
//                String str2 = "<html>\n<head>\n     <style>\n          html,body{background:transparent;margin:0;padding:0;}          *{-webkit-tap-highlight-color:rgba(0, 0, 0, 0);}\n     </style>\n     <script type=\"text/javascript\">\n     var imgUrl = \"" + str1 + "\";" + "     var objImage = new Image();\n" + "     var realWidth = 0;\n" + "     var realHeight = 0;\n" + "\n" + "     function onLoad() {\n" + "          objImage.onload = function() {\n" + "               realWidth = objImage.width;\n" + "               realHeight = objImage.height;\n" + "\n" + "               document.gagImg.src = imgUrl;\n" + "               onResize();\n" + "          }\n" + "          objImage.src = imgUrl;\n" + "     }\n" + "\n" + "     function onResize() {\n" + "          var scale = 1;\n" + "          var newWidth = document.gagImg.width;\n" + "          if (realWidth > newWidth) {\n" + "               scale = realWidth / newWidth;\n" + "          } else {\n" + "               scale = newWidth / realWidth;\n" + "          }\n" + "\n" + "          hiddenHeight = Math.ceil(30 * scale);\n" + "          document.getElementById('hiddenBar').style.height = hiddenHeight + \"px\";\n" + "          document.getElementById('hiddenBar').style.marginTop = -hiddenHeight + \"px\";\n" + "     }\n" + "     </script>\n" + "</head>\n" + "<body onload=\"onLoad()\" onresize=\"onResize()\" onclick=\"Android.toggleOverlayDisplay();\">\n" + "     <table style=\"width: 100%;height:100%;\">\n" + "          <tr style=\"width: 100%;\">\n" + "               <td valign=\"middle\" align=\"center\" style=\"width: 100%;\">\n" + "                    <div style=\"display:block\">\n" + "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" />\n" + "                    </div>\n" + "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 100%; background: transparent;\"></div>\n" + "               </td>\n" + "          </tr>\n" + "     </table>\n" + "</body>\n" + "</html>";
//                layout.webView.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8", null);
//                layout.webView.setVisibility(View.VISIBLE);
//                layout.pb.setVisibility(View.INVISIBLE);
//
//            } else {
//                layout.pb.setVisibility(View.GONE);
//                int[] attrs = new int[]{R.attr.error};
//                TypedArray ta = BrowserBigPicActivity.this.obtainStyledAttributes(attrs);
//                Drawable drawableFromTheme = ta.getDrawable(0);
//                //                webView.setImageDrawable(drawableFromTheme);
//            }
//
//        }
//
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Utility.cancelTasks(task, saveTask);
//        layout.webView.loadUrl("about:blank");
//        layout.webView.stopLoading();
//    }
//
//    private class Layout {
//        WebView webView;
//        CircleProgressView pb;
//
//        ImageButton refresh;
//        ImageButton copy;
//        ImageButton share;
//        ImageButton save;
//        ImageButton hd;
//    }
//}
