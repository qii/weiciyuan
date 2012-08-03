package org.qii.weiciyuan.ui.main;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import org.qii.weiciyuan.support.picturetool.ImageTool;

import java.lang.ref.WeakReference;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 下午12:25
 */
public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {


    private final WeakReference<ImageView> imageViewReference;
    private String data = "";
    private int reqHeight = 0;
    private int reqWidth = 0;

    public BitmapWorkerTask(ImageView imageView) {

        imageViewReference = new WeakReference(imageView);
        reqHeight = imageView.getHeight();
        reqWidth = imageView.getWidth();
    }

//    @Override
//    protected void onPreExecute() {
//        if (imageViewReference != null  ) {
//            final ImageView imageView = imageViewReference.get();
//            if (imageView != null) {
//                imageView.setImageDrawable(GlobalContext.getInstance().getResources().getDrawable(R.drawable.app));
//            }
//        }
//        super.onPreExecute();
//    }

    @Override
    protected Bitmap doInBackground(String... url) {
        data = url[0];
        return ImageTool.getBitmapFromSDCardOrNetWork(data, reqWidth, reqHeight);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }


}
