package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.File;

/**
 * User: qii
 * Date: 13-2-8
 */
public class MsgDetailPicTask extends MyAsyncTask<MessageBean, Integer, Bitmap> {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;
    private FileLocationMethod method;

    private ProgressBar pb;

    private boolean pbFlag = false;

    private GlobalContext globalContext;


    public MsgDetailPicTask(ImageView view, FileLocationMethod method, ProgressBar pb) {
        this.globalContext = GlobalContext.getInstance();
        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.view = view;
        this.method = method;
        this.pb = pb;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (pb != null) {
            pb.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected Bitmap doInBackground(MessageBean... msg) {
        FileLocationMethod method;
        String smallPath = FileManager.getFilePathFromUrl(msg[0].getThumbnail_pic(), FileLocationMethod.picture_thumbnail);
        String middlePath = FileManager.getFilePathFromUrl(msg[0].getBmiddle_pic(), FileLocationMethod.picture_bmiddle);
        String largePath = FileManager.getFilePathFromUrl(msg[0].getOriginal_pic(), FileLocationMethod.picture_large);
        if (new File(largePath).exists()) {
            data = largePath;
            method = FileLocationMethod.picture_large;
        } else if (new File(middlePath).exists()) {
            data = middlePath;
            method = FileLocationMethod.picture_bmiddle;
        } else if (new File(smallPath).exists()) {
            data = smallPath;
            method = FileLocationMethod.picture_thumbnail;
        } else {
            data = middlePath;
            method = FileLocationMethod.picture_bmiddle;
        }

        if (!isCancelled()) {
            return ImageTool.getMiddlePictureInBrowserMSGActivity(data, method, new FileDownloaderHttpHelper.DownloadListener() {
                @Override
                public void pushProgress(int progress, int max) {
                    publishProgress(progress, max);
                }
            });

        }
        return null;
    }

    /**
     * sometime picture has been cached in sd card,so only set indeterminate equal false to show progress when downloading
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (pb != null) {
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
        clean();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (pb != null)
            pb.setVisibility(View.INVISIBLE);

        if (bitmap != null) {

            view.setVisibility(View.VISIBLE);
            view.setImageBitmap(bitmap);

            switch (method) {
                case avatar_small:
                    lruCache.put(data, bitmap);
                    break;
                case avatar_large:
                    lruCache.put(data, bitmap);
                    break;
            }

        } else {
            view.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        clean();
    }

    private void clean() {

        lruCache = null;
        globalContext = null;
    }
}