package org.qii.weiciyuan.support.imagetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.File;
import java.io.IOException;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 */
public class ImageTool {


    public static Bitmap getThumbnailPictureWithRoundedCorner(String url) {


        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.picture_thumbnail);

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath);

        if (bitmap != null) {
            return ImageEdit.getRoundedCornerBitmap(bitmap);
        } else if (GlobalContext.getInstance().isEnablePic()) {
            String path = getBitmapFromNetWork(url, absoluteFilePath, null);
            bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null)
                return ImageEdit.getRoundedCornerBitmap(bitmap);
        }
        return null;
    }

    /**
     * 1. convert gif to normal bitmap
     * 2. cut bitmap
     */
    private static Bitmap getMiddlePictureInTimeLineGif(String absoluteFilePath, int reqWidth, int reqHeight) {
        int useWidth = reqWidth;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absoluteFilePath, options);


        options.inSampleSize = calculateInSampleSize(options, useWidth, reqHeight);

        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath, options);

        int height = options.outHeight;
        int width = options.outWidth;

        int cutHeight = 0;
        int cutWidth = 0;

        if (height >= reqHeight && width >= useWidth) {
            cutHeight = reqHeight;
            cutWidth = useWidth;

        } else if (height < reqHeight && width >= useWidth) {

            cutHeight = height;
            cutWidth = (useWidth * cutHeight) / reqHeight;

        } else if (height >= reqHeight && width < useWidth) {

            cutWidth = width;
            cutHeight = (reqHeight * cutWidth) / useWidth;

        } else if (height < reqHeight && width < useWidth) {


            float betweenWidth = ((float) useWidth - (float) width) / (float) width;
            float betweenHeight = ((float) reqHeight - (float) height) / (float) height;

            if (betweenWidth > betweenHeight) {
                cutWidth = width;
                cutHeight = (reqHeight * cutWidth) / useWidth;

            } else {
                cutHeight = height;
                cutWidth = (useWidth * cutHeight) / reqHeight;

            }


        }

        if (cutWidth > 0 && cutHeight > 0) {
            Bitmap region = Bitmap.createBitmap(bitmap, 0, 0, cutWidth, cutHeight);
//            Bitmap anotherValue = ImageEdit.getRoundedCornerBitmap(region);
//            bitmap.recycle();
//            region.recycle();
//            return anotherValue;

            Bitmap scale = null;
            if (region.getHeight() < reqHeight && region.getWidth() < reqWidth) {
                scale = Bitmap.createScaledBitmap(region, reqWidth, reqHeight, true);
            }
            if (scale == null) {
                Bitmap anotherValue = ImageEdit.getRoundedCornerBitmap(region);
                region.recycle();

                return anotherValue;
            } else {
                Bitmap anotherValue = ImageEdit.getRoundedCornerBitmap(scale);
                region.recycle();
                scale.recycle();
                return anotherValue;
            }

            //Android have bug,you cant use BitmapRegionDecoder to operate gif picture
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
//            byte[] bitmapdata = bos.toByteArray();
//
//            try {
//                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(bitmapdata, 0, bitmapdata.length, false);
//                Bitmap region = decoder.decodeRegion(new Rect(0, 0, cutWidth, cutHeight), null);
//                Bitmap anotherValue = ImageEdit.getRoundedCornerBitmap(region);
//                bitmap.recycle();
//                region.recycle();
//                return anotherValue;
//            } catch (IOException ignored) {
//
//            }

        }

        return null;

    }

    public static Bitmap getMiddlePictureInTimeLine(String url, int reqWidth, int reqHeight, FileDownloaderHttpHelper.DownloadListener downloadListener) {

//        int useWidth = 400;
        int useWidth = reqWidth;

        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.picture_bmiddle);

        File file = new File(absoluteFilePath);

        if (!file.exists() && !GlobalContext.getInstance().isEnablePic()) {
            return null;
        }

        if (!file.exists()) {
            String path = getBitmapFromNetWork(url, absoluteFilePath, downloadListener);
        }

        if (absoluteFilePath.endsWith(".gif")) {
            return getMiddlePictureInTimeLineGif(absoluteFilePath, reqWidth, reqHeight);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absoluteFilePath, options);

        int height = options.outHeight;
        int width = options.outWidth;

        int cutHeight = 0;
        int cutWidth = 0;

        if (height >= reqHeight && width >= useWidth) {
            cutHeight = reqHeight;
            cutWidth = useWidth;

        } else if (height < reqHeight && width >= useWidth) {

            cutHeight = height;
            cutWidth = (useWidth * cutHeight) / reqHeight;

        } else if (height >= reqHeight && width < useWidth) {

            cutWidth = width;
            cutHeight = (reqHeight * cutWidth) / useWidth;

        } else if (height < reqHeight && width < useWidth) {


            float betweenWidth = ((float) useWidth - (float) width) / (float) width;
            float betweenHeight = ((float) reqHeight - (float) height) / (float) height;


            if (betweenWidth > betweenHeight) {
                cutWidth = width;
                cutHeight = (reqHeight * cutWidth) / useWidth;

            } else {
                cutHeight = height;
                cutWidth = (useWidth * cutHeight) / reqHeight;

            }


        }


        if (cutWidth > 0 && cutHeight > 0) {

            int startX = 0;

            if (cutWidth < width) {
                startX = (width - cutWidth) / 2;
            }


            try {
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(absoluteFilePath, false);
                Bitmap region = decoder.decodeRegion(new Rect(startX, 0, startX + cutWidth, cutHeight), null);
                Bitmap scale = null;
                if (region.getHeight() < reqHeight && region.getWidth() < reqWidth) {
                    scale = Bitmap.createScaledBitmap(region, reqWidth, reqHeight, true);
                }
                if (scale == null) {
                    Bitmap anotherValue = ImageEdit.getRoundedCornerBitmap(region);
                    region.recycle();

                    return anotherValue;
                } else {
                    Bitmap anotherValue = ImageEdit.getRoundedCornerBitmap(scale);
                    region.recycle();
                    scale.recycle();
                    return anotherValue;
                }
            } catch (IOException ignored) {
                //do nothing
            }

        }


        return null;

    }

    public static Bitmap getNotificationAvatar(String url, int reqWidth, int reqHeight) {


        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.avatar_large);
        absoluteFilePath = absoluteFilePath + ".jpg";

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath);

        if (bitmap == null) {
            getBitmapFromNetWork(url, absoluteFilePath, null);
            bitmap = BitmapFactory.decodeFile(absoluteFilePath);
        }

        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
        }

        if (bitmap != null) {
            bitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
        }

        return bitmap;
    }

    public static Bitmap getBigAvatarWithRoundedCorner(String url) {


        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.avatar_large);
        absoluteFilePath = absoluteFilePath + ".jpg";

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath);

        if (bitmap == null && GlobalContext.getInstance().isEnablePic()) {
            String path = getBitmapFromNetWork(url, absoluteFilePath, null);
            bitmap = BitmapFactory.decodeFile(absoluteFilePath);
        }

        if (bitmap != null) {
            bitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
        }

        return bitmap;
    }


    public static Bitmap getTimeLineBigAvatarWithRoundedCorner(String url, int reqWidth, int reqHeight) {

        Bitmap bitmap = null;

        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.avatar_large);
        absoluteFilePath = absoluteFilePath + ".jpg";
        if (new File(absoluteFilePath).exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(absoluteFilePath, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            options.inInputShareable = true;

            bitmap = BitmapFactory.decodeFile(absoluteFilePath, options);
        } else if (GlobalContext.getInstance().isEnablePic()) {
            String path = getBitmapFromNetWork(url, absoluteFilePath, null);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(absoluteFilePath, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            options.inInputShareable = true;

            bitmap = BitmapFactory.decodeFile(absoluteFilePath, options);
        }

        if (bitmap != null) {
            bitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
        }

        return bitmap;
    }

    public static Bitmap getSmallAvatarWithRoundedCorner(String url) {


        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.avatar_small);

        absoluteFilePath = absoluteFilePath + ".jpg";

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath);

        if (bitmap == null && GlobalContext.getInstance().isEnablePic()) {
            String path = getBitmapFromNetWork(url, absoluteFilePath, null);
            bitmap = BitmapFactory.decodeFile(path);
        }
        if (bitmap != null) {
            bitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
        }
        return bitmap;
    }

    public static Bitmap getMiddlePictureInBrowserMSGActivity(String url, FileDownloaderHttpHelper.DownloadListener downloadListener) {


        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.picture_bmiddle);

        File file = new File(absoluteFilePath);

        if (!file.exists() && !GlobalContext.getInstance().isEnablePic()) {
            return null;
        }

        if (!file.exists()) {
            String path = getBitmapFromNetWork(url, absoluteFilePath, downloadListener);

        }
        file = new File(absoluteFilePath);
        if (file.exists()) {
            DisplayMetrics displayMetrics = GlobalContext.getInstance().getDisplayMetrics();

            Bitmap bitmap = decodeBitmapFromSDCard(absoluteFilePath, displayMetrics.widthPixels, displayMetrics.heightPixels);
            return bitmap;

        }
        return null;
    }


    public static String getLargePictureWithoutRoundedCorner(String url, FileDownloaderHttpHelper.DownloadListener downloadListener) {


        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.picture_large);

        File file = new File(absoluteFilePath);

        if (file.exists()) {
            return absoluteFilePath;

        } else {
            String path = getBitmapFromNetWork(url, absoluteFilePath, downloadListener);

            file = new File(path);
            if (file.exists()) {
                return absoluteFilePath;
            } else {
                return "about:blank";
            }


        }

    }


    public static String getMiddlePictureWithoutRoundedCorner(String url, FileDownloaderHttpHelper.DownloadListener downloadListener) {


        String absoluteFilePath = FileManager.getFileAbsolutePathFromUrl(url, FileLocationMethod.picture_bmiddle);

        File file = new File(absoluteFilePath);

        if (file.exists()) {
            return absoluteFilePath;

        } else {
            String path = getBitmapFromNetWork(url, absoluteFilePath, downloadListener);

            file = new File(path);
            if (file.exists()) {
                return absoluteFilePath;
            } else {
                return "about:blank";
            }


        }

    }


    private static Bitmap decodeBitmapFromSDCard(String path,
                                                 int reqWidth, int reqHeight) {


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);

    }


    private static String getBitmapFromNetWork(String url, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {

        HttpUtility.getInstance().executeDownloadTask(url, path, downloadListener);
        return path;

    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (height > reqHeight && reqHeight != 0) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else if (width > reqWidth && reqWidth != 0) {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

        }
        return inSampleSize;
    }
}


