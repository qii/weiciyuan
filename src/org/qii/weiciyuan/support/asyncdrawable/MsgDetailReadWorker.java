package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.WeiboDetailImageView;

import java.io.File;

/**
 * User: qii
 * Date: 13-2-8
 * insert progress update listener into  download worker if it exists
 * or create a new download worker
 */
public class MsgDetailReadWorker extends MyAsyncTask<Void, Integer, Bitmap> {

    private WeiboDetailImageView view;
    private ProgressBar pb;
    private Button retry;

    private boolean pbFlag = false;

    private MessageBean msg;

    public MsgDetailReadWorker(WeiboDetailImageView view, MessageBean msg) {
        this.view = view;
        this.pb = this.view.getProgressBar();
        this.msg = msg;
        this.retry = view.getRetryButton();

        if (pb != null && pb.getVisibility() != View.VISIBLE) {
            pb.setVisibility(View.VISIBLE);
        }

        if (retry != null) {
            retry.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... arg) {
        if (isCancelled()) {
            return null;
        }

        TaskCache.waitForMsgDetailPictureDownload(msg, downloadListener);

        FileLocationMethod method;
        String middlePath = FileManager.getFilePathFromUrl(msg.getBmiddle_pic(), FileLocationMethod.picture_bmiddle);
        String largePath = FileManager.getFilePathFromUrl(msg.getOriginal_pic(), FileLocationMethod.picture_large);
        String data = "";
        if (new File(largePath).exists()) {
            data = msg.getOriginal_pic();
            method = FileLocationMethod.picture_large;
        } else if (new File(middlePath).exists()) {
            data = msg.getBmiddle_pic();
            method = FileLocationMethod.picture_bmiddle;
        } else {
            data = msg.getBmiddle_pic();
            method = FileLocationMethod.picture_bmiddle;
        }

        return ImageTool.getMiddlePictureInBrowserMSGActivity(data, method, downloadListener);

    }


    FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
        @Override
        public void pushProgress(int progress, int max) {
            publishProgress(progress, max);
        }

        @Override
        public void completed() {

        }

        @Override
        public void cancel() {

        }
    };

    /**
     * sometime picture has been cached in sd card,so only set indeterminate equal false to show progress when downloading
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (pb != null) {
            if (pb.getVisibility() != View.VISIBLE) {
                pb.setVisibility(View.VISIBLE);
            }
            if (!pbFlag) {
                pb.setIndeterminate(false);
                pbFlag = true;
            }
            Integer progress = values[0];
            Integer max = values[1];
            pb.setMax(max);
            pb.setProgress(progress);
        }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {

        if (pb != null)
            pb.setVisibility(View.INVISIBLE);

        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (pb != null)
            pb.setVisibility(View.INVISIBLE);
        if (bitmap != null) {
            view.setTag(true);
            view.getImageView().setTag(true);
            view.setVisibility(View.VISIBLE);
            view.setImageBitmap(bitmap);
            view.setAlpha(0.0f);
            view.animate().alpha(1.0f).setDuration(200);
        } else {
            view.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
            retry.setVisibility(View.VISIBLE);
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MsgDetailReadWorker picTask = new MsgDetailReadWorker(view, msg);
                    picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }


    }

}