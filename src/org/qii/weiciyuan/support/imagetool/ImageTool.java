package org.qii.weiciyuan.support.imagetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 上午9:25
 */
public class ImageTool {


    private static Bitmap decodeBitmapFromSDCard(String url,
                                                 int reqWidth, int reqHeight) {


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.picture);

        absoluteFilePath = absoluteFilePath + ".jpg";

        File file = new File(absoluteFilePath);

        boolean is = file.exists();

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap != null) {

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(absoluteFilePath, options);
        } else {

            return null;
        }
    }

    private static Bitmap decodeBitmapFromSDCard(String url) {


        final BitmapFactory.Options options = new BitmapFactory.Options();

        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url,FileLocationMethod.avatar);

        absoluteFilePath = absoluteFilePath + ".jpg";

        File file = new File(absoluteFilePath);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap != null) {

            return bitmap;
        } else {

            return null;
        }
    }

    public static Bitmap getBitmapFromSDCardOrNetWork(String url,
                                                      int reqWidth, int reqHeight) {
        Bitmap bitmap = decodeBitmapFromSDCard(url, reqWidth, reqHeight);

        if (bitmap != null) {
            return bitmap;
        } else {
            return getBitmapFromNetWork(url, reqWidth, reqHeight);
        }
    }

    public static Bitmap getAvatarBitmapFromSDCardOrNetWork(String url) {

        Bitmap bitmap = decodeBitmapFromSDCard(url);

        if (bitmap != null) {
            return bitmap;
        } else {
            return getBitmapFromNetWork(url);
        }
    }

    private static Bitmap getBitmapFromNetWork(String url,
                                               int reqWidth, int reqHeight) {
        Map<String, String> parms = new HashMap<String, String>();
        String relativeFilePaht = HttpUtility.getInstance().execute(HttpMethod.Get_AVATAR_File, url, parms);
        return decodeBitmapFromSDCard(url, reqWidth, reqHeight);
    }

    private static Bitmap getBitmapFromNetWork(String url) {
        Map<String, String> parms = new HashMap<String, String>();
        String relativeFilePaht = HttpUtility.getInstance().execute(HttpMethod.Get_AVATAR_File, url, parms);
        return decodeBitmapFromSDCard(url);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

        }
        return inSampleSize;
    }
}


