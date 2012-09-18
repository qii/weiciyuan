package org.qii.weiciyuan.ui.browser;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-8-5
 */
public class SimpleBitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;
    private FileLocationMethod method;


    public SimpleBitmapWorkerTask(ImageView view, FileLocationMethod method) {

        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.view = view;
        this.method = method;

    }

    @Override
    protected Bitmap doInBackground(String... url) {
        data = url[0];
        if (!isCancelled()) {
            switch (method) {
                case picture_bmiddle:
                    return ImageTool.getMiddlePictureInBrowserMSGActivity(data, null);
                case avatar_small:
                    return ImageTool.getSmallAvatarWithRoundedCorner(data);
                case avatar_large:
                    return ImageTool.getBigAvatarWithRoundedCorner(data);
            }
        }

        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (bitmap != null) {

            switch (method) {
                case avatar_small:
                    lruCache.put(data, bitmap);
                    break;
                case avatar_large:
                    lruCache.put(data, bitmap);
                    break;
            }

        }

        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {


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