package org.qii.weiciyuan.ui.main;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import org.qii.weiciyuan.support.imagetool.ImageTool;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 下午12:25
 */
public class AvatarBitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";


    public AvatarBitmapWorkerTask(LruCache<String, Bitmap> cache) {

        lruCache = cache;

    }

    @Override
    protected Bitmap doInBackground(String... url) {
        data = url[0];

        return ImageTool.getAvatarBitmapFromSDCardOrNetWork(data);
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

        }
    }


}
