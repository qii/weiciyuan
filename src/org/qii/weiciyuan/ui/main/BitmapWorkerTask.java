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
public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private int reqHeight = 0;
    private int reqWidth = 0;

    public BitmapWorkerTask(LruCache<String, Bitmap> cache) {

        lruCache = cache;

    }

    @Override
    protected Bitmap doInBackground(String... url) {
        data = url[0];

        return ImageTool.getBitmapFromSDCardOrNetWork(data, reqWidth, reqHeight);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            lruCache.put(data, bitmap);

        }
    }


}
