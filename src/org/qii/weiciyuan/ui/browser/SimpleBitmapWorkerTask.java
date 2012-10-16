package org.qii.weiciyuan.ui.browser;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-8-5
 */
public class SimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, Bitmap> {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;
    private FileLocationMethod method;

    private ProgressBar pb;

    private boolean pbFlag = false;


    public SimpleBitmapWorkerTask(ImageView view, FileLocationMethod method) {

        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.view = view;
        this.method = method;

    }

    public SimpleBitmapWorkerTask(ImageView view, FileLocationMethod method, ProgressBar pb) {

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
    protected Bitmap doInBackground(String... url) {
        data = url[0];
        if (!isCancelled()) {
            switch (method) {
                case picture_bmiddle:
                    return ImageTool.getMiddlePictureInBrowserMSGActivity(data, new FileDownloaderHttpHelper.DownloadListener() {
                        @Override
                        public void pushProgress(int progress, int max) {
                            publishProgress(progress, max);
                        }
                    });
                case avatar_small:
                    return ImageTool.getSmallAvatarWithRoundedCorner(data);
                case avatar_large:
                    return ImageTool.getBigAvatarWithRoundedCorner(data);
            }
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
            pb.setVisibility(View.GONE);

        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (pb != null)
            pb.setVisibility(View.GONE);

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

        }


    }


}