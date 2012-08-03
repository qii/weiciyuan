package org.qii.weiciyuan.ui.main;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import org.qii.weiciyuan.support.imagetool.ImageTool;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 下午4:09
 */
public class PictureBitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {


    private String data = "";


    @Override
    protected Bitmap doInBackground(String... url) {
        data = url[0];

        return ImageTool.getAvatarBitmapFromSDCardOrNetWork(data);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

    }


}
