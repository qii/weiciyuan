package org.qii.weiciyuan.support.file;

import android.os.Environment;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.io.File;
import java.io.IOException;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 上午10:06
 */
public class FileManager {
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String APP_NAME = "weiciyuan";
    private static final String AVATAR_CACHE = "avatar";
    private static final String PICTURE_CACHE = "picture";


    private static boolean isExternalStorageMounted() {

        boolean canRead = Environment.getExternalStorageDirectory().canRead();
        boolean onlyRead = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean unMounted = Environment.getExternalStorageState().equals(
                Environment.MEDIA_UNMOUNTED);

        if (!canRead || onlyRead || unMounted) {
            return false;
        }

        return true;
    }

    private static String getFileAbsolutePathFromRelativePath(String relativePath) {
        String result = SDCARD_PATH + File.separator + APP_NAME + relativePath;
        AppLogger.d(result);
        return result;
    }

    public static String getFileAbsolutePathFromUrl(String url, FileLocationMethod method) {
        String oldRelativePath = getFileRelativePathFromUrl(url);
        String newRelativePath = "";
        switch (method) {
            case avatar:
                newRelativePath = File.separator + AVATAR_CACHE + oldRelativePath;
                break;
            case picture:
                newRelativePath = File.separator + PICTURE_CACHE + oldRelativePath;
                break;
        }

        String absolutePath = getFileAbsolutePathFromRelativePath(newRelativePath);

        AppLogger.d(absolutePath);

        return absolutePath;
    }

    private static String getFileRelativePathFromUrl(String url) {

        int index = url.indexOf("//");

        String s = url.substring(index + 2);

        String result = s.substring(s.indexOf("/"));

        AppLogger.d(result);

        return result;
    }


    public static File creatNewFileInSdcard(String absoluatePath) {
        if (!isExternalStorageMounted()) {
            AppLogger.e("sdcard unavailiable");
            return null;
        }

        String absoluteFilePath = absoluatePath;
        String absoluteFileDirPath = absoluteFilePath.substring(0, absoluteFilePath.length() - 1);
        File file = new File(absoluteFilePath + ".jpg");
        if (file.exists()) {
            return file;
        } else {

            File dirFile = new File(absoluteFileDirPath);
            if (dirFile.mkdirs()) {

                try {
                    file.createNewFile();
                    return file;
                } catch (IOException e) {
                    return null;
                }
            }

        }
        return null;

    }
}
