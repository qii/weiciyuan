//package org.qii.weiciyuan.support.asyncdrawable;
//
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.util.DisplayMetrics;
//import android.util.LruCache;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import org.qii.weiciyuan.R;
//import org.qii.weiciyuan.bean.MessageBean;
//import org.qii.weiciyuan.support.debug.DebugColor;
//import org.qii.weiciyuan.support.error.WeiboException;
//import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
//import org.qii.weiciyuan.support.file.FileLocationMethod;
//import org.qii.weiciyuan.support.file.FileManager;
//import org.qii.weiciyuan.support.imagetool.ImageTool;
//import org.qii.weiciyuan.support.lib.MyAsyncTask;
//import org.qii.weiciyuan.support.utils.GlobalContext;
//
//import java.io.File;
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * User: qii
// * Date: 13-2-9
// */
//public class ReadMsgDetailPicTask extends MyAsyncTask<String, Void, Bitmap> implements IPictureWorker {
//
//    private LruCache<String, Bitmap> lruCache;
//       private String data = "";
//       private ImageView view;
//       private FileLocationMethod method;
//
//       private ProgressBar pb;
//
//       private boolean pbFlag = false;
//
//       private GlobalContext globalContext;
//       private MessageBean msg;
//       private Boolean loadFromCache;
//
//       public MsgDetailReadWorker(ImageView view, FileLocationMethod method, ProgressBar pb, MessageBean msg, Boolean loadFromCache) {
//           this.globalContext = GlobalContext.getInstance();
//           this.lruCache = GlobalContext.getInstance().getAvatarCache();
//           this.view = view;
//           this.method = method;
//           this.pb = pb;
//           this.msg = msg;
//           this.loadFromCache = loadFromCache;
//       }
//
//       @Override
//       protected void onPreExecute() {
//           super.onPreExecute();
//           if (!loadFromCache)
//               return;
//           PictureBitmapWorkerTask task = TimeLineBitmapDownloader.picTasks.get(msg.getOriginal_pic());
//           if (task == null) {
//               task = TimeLineBitmapDownloader.picTasks.get(msg.getBmiddle_pic());
//           }
//           if (task != null) {
//               task.addDownloadListener(new FileDownloaderHttpHelper.DownloadListener() {
//                   @Override
//                   public void pushProgress(int progress, int max) {
//                       if (pb != null) {
//                           if (pb.getVisibility() != View.VISIBLE) {
//                               pb.setVisibility(View.VISIBLE);
//                           }
//                           if (!pbFlag) {
//                               pb.setIndeterminate(false);
//                               pbFlag = true;
//                           }
//
//                           pb.setMax(max);
//                           pb.setProgress(progress);
//                       }
//                   }
//
//                   @Override
//                   public void completed() {
//                       pb.setMax(100);
//                       pb.setProgress(100);
//                       MsgDetailReadWorker picTask = new MsgDetailReadWorker(view, method, pb, msg,false);
//                       picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//                   }
//               });
//               cancel(true);
//           }
//       }
//
//       @Override
//       protected Bitmap doInBackground(Void... arg) {
//           FileLocationMethod method;
//           String middlePath = FileManager.getFilePathFromUrl(msg.getBmiddle_pic(), FileLocationMethod.picture_bmiddle);
//           String largePath = FileManager.getFilePathFromUrl(msg.getOriginal_pic(), FileLocationMethod.picture_large);
//           if (new File(largePath).exists()) {
//               data = msg.getOriginal_pic();
//               method = FileLocationMethod.picture_large;
//           } else if (new File(middlePath).exists()) {
//               data = msg.getBmiddle_pic();
//               method = FileLocationMethod.picture_bmiddle;
//           } else {
//               data = msg.getBmiddle_pic();
//               method = FileLocationMethod.picture_bmiddle;
//           }
//
//           if (!isCancelled()) {
//               return ImageTool.getMiddlePictureInBrowserMSGActivity(data, method, new FileDownloaderHttpHelper.DownloadListener() {
//                   @Override
//                   public void pushProgress(int progress, int max) {
//                       publishProgress(progress, max);
//                   }
//
//                   @Override
//                   public void completed() {
//
//                   }
//               });
//
//           }
//           return null;
//       }
//
//       /**
//        * sometime picture has been cached in sd card,so only set indeterminate equal false to show progress when downloading
//        */
//       @Override
//       protected void onProgressUpdate(Integer... values) {
//           super.onProgressUpdate(values);
//           if (pb != null) {
//               if (pb.getVisibility() != View.VISIBLE) {
//                   pb.setVisibility(View.VISIBLE);
//               }
//               if (!pbFlag) {
//                   pb.setIndeterminate(false);
//                   pbFlag = true;
//               }
//               Integer progress = values[0];
//               Integer max = values[1];
//               pb.setMax(max);
//               pb.setProgress(progress);
//           }
//       }
//
//       @Override
//       protected void onCancelled(Bitmap bitmap) {
//
//           if (pb != null)
//               pb.setVisibility(View.INVISIBLE);
//
//           super.onCancelled(bitmap);
//           clean();
//       }
//
//       @Override
//       protected void onPostExecute(Bitmap bitmap) {
//           if (pb != null)
//               pb.setVisibility(View.INVISIBLE);
//
//           if (bitmap != null) {
//               view.setTag(true);
//               view.setVisibility(View.VISIBLE);
//               view.setImageBitmap(bitmap);
//           } else {
//               view.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
//           }
//
//           clean();
//       }
//
//       private void clean() {
//
//           lruCache = null;
//           globalContext = null;
//       }
//   }