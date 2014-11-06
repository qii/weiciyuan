package org.qii.weiciyuan.support.imageutility;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.file.FileManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class ImageEditUtility {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        return getRoundedCornerBitmap(bitmap, 3);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int cornerRadius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = cornerRadius;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    public static String convertStringToBitmap(Context context, View et) {
        Bitmap bitmap = et.getDrawingCache();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        output.eraseColor(context.getResources().getColor(R.color.white));
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, rect, rect, paint);

        try {
            String path = FileManager.getTxt2picPath() + File.separator + "tmp.png";
            AppLogger.e(path);
            FileManager.createNewFileInSDCard(path);
            FileOutputStream out = new FileOutputStream(path);
            output.compress(Bitmap.CompressFormat.PNG, 90, out);
//            bitmap.recycle();
            if (new File(path).exists()) {
                return path;
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return "";
    }
}
