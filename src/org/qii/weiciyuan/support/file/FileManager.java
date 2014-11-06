package org.qii.weiciyuan.support.file;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.database.DownloadPicturesDBTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-8-3
 */
public class FileManager {

    private static final String PICTURE_CACHE = "picture_cache";
    private static final String TXT2PIC = "txt2pic";
    private static final String WEBVIEW_FAVICON = "favicon";
    private static final String LOG = "log";
    private static final String WEICIYUAN = "weiciyuan";

    /**
     * install weiciyuan, open app and login in, Android system will create cache dir.
     * then open cache dir (/sdcard dir/Android/data/org.qii.weiciyuan) with Root Explorer,
     * uninstall weiciyuan and reinstall it, the new weiciyuan app will have the bug it can't
     * read cache dir again, so I have to tell user to delete that cache dir
     */
    private static volatile boolean cantReadBecauseOfAndroidBugPermissionProblem = false;

    public static String getSdCardPath() {
        if (isExternalStorageMounted()) {
            File path = GlobalContext.getInstance().getExternalCacheDir();
            if (path != null) {
                return path.getAbsolutePath();
            } else {
                if (!cantReadBecauseOfAndroidBugPermissionProblem) {
                    cantReadBecauseOfAndroidBugPermissionProblem = true;
                    final Activity activity = GlobalContext.getInstance().getActivity();
                    if (activity == null || activity.isFinishing()) {
                        GlobalContext.getInstance().getUIHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GlobalContext.getInstance(),
                                        R.string.please_deleted_cache_dir, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

                        return "";
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(activity)
                                    .setTitle(R.string.something_error)
                                    .setMessage(R.string.please_deleted_cache_dir)
                                    .setPositiveButton(R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {

                                                }
                                            })
                                    .show();
                        }
                    });
                }
            }
        } else {
            return "";
        }

        return "";
    }

    public File getAlbumStorageDir(String albumName) {

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            AppLogger.e("Directory not created");
        }
        return file;
    }

    public static boolean isExternalStorageMounted() {

        boolean canRead = Environment.getExternalStorageDirectory().canRead();
        boolean onlyRead = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean unMounted = Environment.getExternalStorageState().equals(
                Environment.MEDIA_UNMOUNTED);

        return !(!canRead || onlyRead || unMounted);
    }

    public static String getUploadPicTempFile() {

        if (!isExternalStorageMounted()) {
            return "";
        } else {
            return getSdCardPath() + File.separator + "upload.jpg";
        }
    }

    public static String getKKConvertPicTempFile() {

        if (!isExternalStorageMounted()) {
            return "";
        } else {
            return getSdCardPath() + File.separator + "kk_convert" + System.currentTimeMillis()
                    + ".jpg";
        }
    }

    public static String getLogDir() {
        if (!isExternalStorageMounted()) {
            return "";
        } else {
            String path = getSdCardPath() + File.separator + LOG;
            if (!new File(path).exists()) {
                new File(path).mkdirs();
            }
            return path;
        }
    }

    public static String getFilePathFromUrl(String url, FileLocationMethod method) {

        if (!isExternalStorageMounted()) {
            return "";
        }

        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return DownloadPicturesDBTask.get(url);
    }

    public static String generateDownloadFileName(String url) {

        if (!isExternalStorageMounted()) {
            return "";
        }

        if (TextUtils.isEmpty(url)) {
            return "";
        }

        String path = String.valueOf(url.hashCode());
        String result = getSdCardPath() + File.separator + PICTURE_CACHE + File.separator + path;
        if (url.endsWith(".jpg")) {
            result += ".jpg";
        } else if (url.endsWith(".gif")) {
            result += ".gif";
        }
        if (!result.endsWith(".jpg") && !result.endsWith(".gif") && !result.endsWith(".png")) {
            result = result + ".jpg";
        }

        return result;
    }

    public static String getTxt2picPath() {
        if (!isExternalStorageMounted()) {
            return "";
        }

        String path = getSdCardPath() + File.separator + TXT2PIC;
        File file = new File(path);
        if (file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static File createNewFileInSDCard(String absolutePath) {
        if (!isExternalStorageMounted()) {
            AppLogger.e("sdcard unavailiable");
            return null;
        }

        if (TextUtils.isEmpty(absolutePath)) {
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

    public static String getWebViewFaviconDirPath() {
        if (!TextUtils.isEmpty(getSdCardPath())) {
            String path = getSdCardPath() + File.separator + WEBVIEW_FAVICON + File.separator;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            return path;
        }
        return "";
    }

    public static String getCacheSize() {
        if (isExternalStorageMounted()) {
            String path = getSdCardPath() + File.separator;
            FileSize size = new FileSize(new File(path));
            return size.toString();
        }
        return "0MB";
    }

    public static List<String> getCachePath() {
        List<String> path = new ArrayList<String>();
        if (isExternalStorageMounted()) {
            String thumbnailPath = getSdCardPath() + File.separator + PICTURE_CACHE;

            path.add(thumbnailPath);
        }
        return path;
    }

    public static String getPictureCacheSize() {
        long size = 0L;
        if (isExternalStorageMounted()) {
            String thumbnailPath = getSdCardPath() + File.separator + PICTURE_CACHE;

            size += new FileSize(new File(thumbnailPath)).getLongSize();
        }
        return FileSize.convertSizeToString(size);
    }

    public static boolean deleteCache() {
        String path = getSdCardPath() + File.separator;
        return deleteDirectory(new File(path));
    }

    public static boolean deletePictureCache() {
        String thumbnailPath = getSdCardPath() + File.separator + PICTURE_CACHE;

        deleteDirectory(new File(thumbnailPath));
        DownloadPicturesDBTask.clearAll();

        return true;
    }

    public static boolean deleteDirectory(File path) {
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

    public static boolean saveToPicDir(String path) {
        if (!isExternalStorageMounted()) {
            return false;
        }

        File file = new File(path);
        String name = file.getName();
        String newPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + WEICIYUAN
                + File.separator + name;
        try {
            FileManager.createNewFileInSDCard(newPath);
            copyFile(file, new File(newPath));
            Utility.forceRefreshSystemAlbum(newPath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            outBuff.flush();
        } finally {
            if (inBuff != null) {
                inBuff.close();
            }
            if (outBuff != null) {
                outBuff.close();
            }
        }
    }
}
