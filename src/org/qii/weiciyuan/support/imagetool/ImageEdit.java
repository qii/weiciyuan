package org.qii.weiciyuan.support.imagetool;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.File;
import java.io.FileOutputStream;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class ImageEdit {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 3;

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
            String path = GlobalContext.getInstance().getExternalCacheDir().getAbsolutePath() + File.separator + "wo.png";
            AppLogger.e(path);
            FileOutputStream out = new FileOutputStream(path);
            output.compress(Bitmap.CompressFormat.PNG, 90, out);
//            bitmap.recycle();
            if (new File(path).exists())
                return path;
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return "";
    }

}
