package org.qii.weiciyuan.ui.browser;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-8-5
 * Time: 下午7:50
 * To change this template use File | Settings | File Templates.
 */
public class SimpleBitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;


    public SimpleBitmapWorkerTask(ImageView view) {

        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.view = view;

    }

    @Override
    protected Bitmap doInBackground(String... url) {
        data = url[0];
        if (!isCancelled()) {
            return ImageTool.getAvatarBitmap(data);
        }

        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (bitmap != null) {

            lruCache.put(data, bitmap);

        }

        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            lruCache.put(data, bitmap);

            view.setImageBitmap(bitmap);


        }

    }


}