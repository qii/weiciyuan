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
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.File;
import java.io.IOException;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 */
public class ImageTool {


    public static Bitmap getThumbnailPictureWithRoundedCorner(String url) {

        String absoluteFilePath = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_thumbnail);

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath);

        if (bitmap != null) {
            Bitmap roundedCornerBitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
            if (roundedCornerBitmap != bitmap) {
                bitmap.recycle();
                bitmap = roundedCornerBitmap;

            }
        } else if (SettingUtility.isEnablePic()) {
            getBitmapFromNetWork(url, absoluteFilePath, null);
            bitmap = BitmapFactory.decodeFile(absoluteFilePath);
            if (bitmap != null) {
                Bitmap roundedCornerBitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
                if (roundedCornerBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = roundedCornerBitmap;

                }
            }
        }
        return bitmap;
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

        String absoluteFilePath = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_bmiddle);

        File file = new File(absoluteFilePath);

        if (!file.exists() && !SettingUtility.isEnablePic()) {
            return null;
        }

        if (!file.exists()) {
            getBitmapFromNetWork(url, absoluteFilePath, downloadListener);
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
                if (decoder != null) {
                    Bitmap bitmap = decoder.decodeRegion(new Rect(startX, 0, startX + cutWidth, cutHeight), null);
                    if (bitmap.getHeight() < reqHeight && bitmap.getWidth() < reqWidth) {
                        Bitmap scale = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
                        if (scale != bitmap) {
                            bitmap.recycle();
                            bitmap = scale;
                        }
                    }
                    if (bitmap != null) {
                        Bitmap roundedCornerBitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
                        if (roundedCornerBitmap != bitmap) {
                            bitmap.recycle();
                            bitmap = roundedCornerBitmap;
                        }

                        return bitmap;
                    }
                }
            } catch (IOException ignored) {
                //do nothing
            }

        }


        return null;

    }


    public static Bitmap getBigAvatarWithRoundedCorner(String url) {

        if (!FileManager.isExternalStorageMounted()) {
            return null;
        }

        String absoluteFilePath = FileManager.getFilePathFromUrl(url, FileLocationMethod.avatar_large);
        absoluteFilePath = absoluteFilePath + ".jpg";

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath);

        if (bitmap == null && SettingUtility.isEnablePic()) {
            getBitmapFromNetWork(url, absoluteFilePath, null);
            bitmap = BitmapFactory.decodeFile(absoluteFilePath);
        }

        if (bitmap != null) {
            bitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
        }

        return bitmap;
    }


    public static Bitmap getSmallAvatarWithRoundedCorner(String url) {

        if (!FileManager.isExternalStorageMounted()) {
            return null;
        }

        String absoluteFilePath = FileManager.getFilePathFromUrl(url, FileLocationMethod.avatar_small);

        absoluteFilePath = absoluteFilePath + ".jpg";

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath);

        if (bitmap == null && !SettingUtility.isEnablePic()) {
            return null;
        }

        if (bitmap == null) {
            boolean result = getBitmapFromNetWork(url, absoluteFilePath, null);
            if (result) {
                bitmap = BitmapFactory.decodeFile(absoluteFilePath);
            } else {
                return null;
            }
        }
        if (bitmap != null) {
            Bitmap roundedBitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
            bitmap.recycle();
            return roundedBitmap;
        }
        return null;
    }


    public static Bitmap getRoundedCornerPic(String url, int reqWidth, int reqHeight, FileLocationMethod method) {

        if (!FileManager.isExternalStorageMounted()) {
            return null;
        }

        String absoluteFilePath = FileManager.getFilePathFromUrl(url, method);
        absoluteFilePath = absoluteFilePath + ".jpg";

        boolean fileExist = new File(absoluteFilePath).exists();

        if (!fileExist && !SettingUtility.isEnablePic()) {
            return null;
        }

        if (!fileExist) {
            boolean result = getBitmapFromNetWork(url, absoluteFilePath, null);
            if (!result)
                return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absoluteFilePath, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(absoluteFilePath, options);

        if (bitmap != null) {
            if (bitmap.getHeight() < reqHeight || bitmap.getWidth() < reqWidth) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);

                if (scaledBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = scaledBitmap;
                }

                Bitmap roundedBitmap = ImageEdit.getRoundedCornerBitmap(bitmap);
                if (roundedBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = roundedBitmap;
                }
                return bitmap;
            }
        }

        return bitmap;
    }


    public static Bitmap getMiddlePictureInBrowserMSGActivity(String url, FileDownloaderHttpHelper.DownloadListener downloadListener) {


        String absoluteFilePath = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_bmiddle);

        File file = new File(absoluteFilePath);

        if (!file.exists() && !SettingUtility.isEnablePic()) {
            return null;
        }

        if (!file.exists()) {
            getBitmapFromNetWork(url, absoluteFilePath, downloadListener);

        }
        file = new File(absoluteFilePath);
        if (file.exists()) {
            DisplayMetrics displayMetrics = GlobalContext.getInstance().getDisplayMetrics();

            Bitmap bitmap = decodeBitmapFromSDCard(absoluteFilePath, displayMetrics.widthPixels, 900);
            return bitmap;

        }
        return null;
    }


    public static String getLargePictureWithoutRoundedCorner(String url, FileDownloaderHttpHelper.DownloadListener downloadListener, FileLocationMethod fileLocationMethod) {


        String absoluteFilePath = FileManager.getFilePathFromUrl(url, fileLocationMethod);

        File file = new File(absoluteFilePath);

        if (file.exists()) {
            return absoluteFilePath;

        } else {
            getBitmapFromNetWork(url, absoluteFilePath, downloadListener);

            file = new File(absoluteFilePath);
            if (file.exists()) {
                return absoluteFilePath;
            } else {
                return "about:blank";
            }


        }

    }


    public static Bitmap decodeBitmapFromSDCard(String path,
                                                int reqWidth, int reqHeight) {


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);

    }


    private static boolean getBitmapFromNetWork(String url, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {
        for (int i = 0; i < 3; i++) {
            if (HttpUtility.getInstance().executeDownloadTask(url, path, downloadListener)) {
                return true;
            }
        }
        return false;
    }


    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (height > reqHeight && reqHeight != 0) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            }

            int tmp = 0;

            if (width > reqWidth && reqWidth != 0) {
                tmp = Math.round((float) width / (float) reqWidth);
            }

            if (tmp > inSampleSize)
                inSampleSize = tmp;

        }
        return inSampleSize;
    }


}


