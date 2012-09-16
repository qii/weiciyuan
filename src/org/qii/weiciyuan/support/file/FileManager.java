package org.qii.weiciyuan.support.file;

import android.os.Environment;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.File;
import java.io.IOException;

/**
 * User: qii
 * Date: 12-8-3
 */
public class FileManager {

    private static final String SDCARD_PATH = GlobalContext.getInstance().getExternalCacheDir().getAbsolutePath();
    private static final String AVATAR_SMAll = "avatar_small";
    private static final String AVATAR_LARGE = "avatar_large";
    private static final String PICTURE_THUMBNAIL = "picture_thumbnail";
    private static final String PICTURE_BMIDDLE = "picture_bmiddle";
    private static final String PICTURE_LARGE = "picture_large";


    private static boolean isExternalStorageMounted() {

        boolean canRead = Environment.getExternalStorageDirectory().canRead();
        boolean onlyRead = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean unMounted = Environment.getExternalStorageState().equals(
                Environment.MEDIA_UNMOUNTED);

        return !(!canRead || onlyRead || unMounted);
    }

    private static String getFileAbsolutePathFromRelativePath(String relativePath) {
        String result = SDCARD_PATH + File.separator + relativePath;

        return result;
    }

    public static String getFileAbsolutePathFromUrl(String url, FileLocationMethod method) {
        String oldRelativePath = getFileRelativePathFromUrl(url);
        String newRelativePath = "";
        switch (method) {
            case avatar_small:
                newRelativePath = File.separator + AVATAR_SMAll + oldRelativePath;
                break;
            case avatar_large:
                newRelativePath = File.separator + AVATAR_LARGE + oldRelativePath;
                break;
            case picture_thumbnail:
                newRelativePath = File.separator + PICTURE_THUMBNAIL + oldRelativePath;
                break;
            case picture_bmiddle:
                newRelativePath = File.separator + PICTURE_BMIDDLE + oldRelativePath;
                break;
            case picture_large:
                newRelativePath = File.separator + PICTURE_LARGE + oldRelativePath;
                break;

        }

        String absolutePath = getFileAbsolutePathFromRelativePath(newRelativePath);

        //AppLogger.d(absolutePath);

        return absolutePath;
    }

    private static String getFileRelativePathFromUrl(String url) {
        //AppLogger.d(url);
        int index = url.indexOf("//");

        String s = url.substring(index + 2);

        String result = s.substring(s.indexOf("/"));


        return result;
    }


    public static File createNewFileInSDCard(String absolutePath) {
        if (!isExternalStorageMounted()) {
            AppLogger.e("sdcard unavailiable");
            return null;
        }

        File file = new File(absolutePath);
        if (file.exists()) {
            return file;
        } else {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }


            try {
                if (file.createNewFile()) {
                    return file;
                }
            } catch (IOException e) {
                AppLogger.d(e.getMessage());
                return null;

            }

        }
        return null;

    }

    public static String getCacheSize() {
        if (isExternalStorageMounted()) {
            String path = SDCARD_PATH + File.separator;
            FileSize size = new FileSize(new File(path));
            return size.toString();
        }
        return "0MB";
    }

    public static boolean deleteCache() {
        String path = SDCARD_PATH + File.separator;
        return deleteDirectory(new File(path));
    }

    public static boolean deletePictureCache() {
        String path = SDCARD_PATH + File.separator + PICTURE_THUMBNAIL;
        return deleteDirectory(new File(path));
    }

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
