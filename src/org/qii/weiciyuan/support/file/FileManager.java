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
    private static String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static String APP_NAME = "weiciyuan";
    private static String AVATAR_CACHE = "avatar";


    private static boolean isExternalStorageMounted() {
        if (!Environment.getExternalStorageDirectory().canRead()
                || Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED_READ_ONLY)
                || Environment.getExternalStorageState().equals(
                Environment.MEDIA_UNMOUNTED)) {
            return false;
        }

        return true;
    }

    public static String getFileAbsolutePathFromRelativePath(String relativePath) {
        String result = SDCARD_PATH + File.separator + APP_NAME + relativePath;
        AppLogger.d(result);
        return result;
    }

    public static String getFileAbsolutePathFromUrl(String url) {
        String relativePath = getFileRelativePathFromUrl(url);
        String result = getFileAbsolutePathFromRelativePath(relativePath);
        AppLogger.d(result);

        return result;
    }

    public static String getFileRelativePathFromUrl(String url) {

        int index = url.indexOf("//");

        String s = url.substring(index + 2);

        String result = s.substring(s.indexOf("/"));

        AppLogger.d(result);

        return result;
    }


    public static File creatNewFileInSdcard(String relativePath) {
        if (!isExternalStorageMounted()) {
            AppLogger.e("sdcard unavailiable");
            return null;
        }

        String absoluteFilePath = getFileAbsolutePathFromRelativePath(relativePath);
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
