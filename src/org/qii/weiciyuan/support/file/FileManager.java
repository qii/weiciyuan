package org.qii.weiciyuan.support.file;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-8-3
 */
public class FileManager {

    private static final String AVATAR_SMAll = "avatar_small";
    private static final String AVATAR_LARGE = "avatar_large";
    private static final String PICTURE_THUMBNAIL = "picture_thumbnail";
    private static final String PICTURE_BMIDDLE = "picture_bmiddle";
    private static final String PICTURE_LARGE = "picture_large";
    private static final String MAP = "map";
    private static final String COVER = "cover";
    private static final String EMOTION = "emotion";
    private static final String TXT2PIC = "txt2pic";
    private static final String WEBVIEW_FAVICON = "favicon";
    private static final String LOG = "log";

    /**
     * install weiciyuan, open app and login in, Android system will create cache dir.
     * then open cache dir (/sdcard dir/Android/data/org.qii.weiciyuan) with Root Explorer,
     * uninstall weiciyuan and reinstall it, the new weiciyuan app will have the bug it can't
     * read cache dir again, so I have to tell user to delete that cache dir
     */
    private static volatile boolean cantReadBecauseOfAndroidBugPermissionProblem = false;

    private static String getSdCardPath() {
        if (isExternalStorageMounted()) {
            File path = GlobalContext.getInstance().getExternalCacheDir();
            if (path != null)
                return path.getAbsolutePath();
            else {
                if (!cantReadBecauseOfAndroidBugPermissionProblem) {
                    cantReadBecauseOfAndroidBugPermissionProblem = true;
                    GlobalContext.getInstance().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(GlobalContext.getInstance().getActivity())
                                    .setTitle(R.string.something_error)
                                    .setMessage(R.string.please_deleted_cache_dir)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

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

        if (!isExternalStorageMounted())
            return "";
        else
            return getSdCardPath() + File.separator + "upload.jpg";
    }

    public static String getLogDir() {
        if (!isExternalStorageMounted())
            return "";
        else {
            String path = getSdCardPath() + File.separator + LOG;
            if (!new File(path).exists()) {
                new File(path).mkdirs();
            }
            return path;
        }
    }

    public static String getFilePathFromUrl(String url, FileLocationMethod method) {

        if (!isExternalStorageMounted())
            return "";

        if (TextUtils.isEmpty(url))
            return "";

        int index = url.indexOf("//");

        String s = url.substring(index + 2);

        String oldRelativePath = s.substring(s.indexOf("/"));

        String newRelativePath = "";
        switch (method) {
            case avatar_small:
                newRelativePath = AVATAR_SMAll + oldRelativePath;
                break;
            case avatar_large:
                newRelativePath = AVATAR_LARGE + oldRelativePath;
                break;
            case picture_thumbnail:
                newRelativePath = PICTURE_THUMBNAIL + oldRelativePath;
                break;
            case picture_bmiddle:
                newRelativePath = PICTURE_BMIDDLE + oldRelativePath;
                break;
            case picture_large:
                newRelativePath = PICTURE_LARGE + oldRelativePath;
                break;
            case emotion:
                String name = new File(oldRelativePath).getName();
                newRelativePath = EMOTION + File.separator + name;
                break;
            case cover:
                newRelativePath = COVER + oldRelativePath;
                break;
            case map:
                newRelativePath = MAP + oldRelativePath;
                break;
        }

        String result = getSdCardPath() + File.separator + newRelativePath;
        if (!result.endsWith(".jpg") && !result.endsWith(".gif") && !result.endsWith(".png"))
            result = result + ".jpg";

        return result;
    }

    public static String getTxt2picPath() {
        if (!isExternalStorageMounted())
            return "";

        String path = getSdCardPath() + File.separator + TXT2PIC;
        File file = new File(path);
        if (file.exists())
            file.mkdirs();
        return path;
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
            String thumbnailPath = getSdCardPath() + File.separator + PICTURE_THUMBNAIL;
            String middlePath = getSdCardPath() + File.separator + PICTURE_BMIDDLE;
            String oriPath = getSdCardPath() + File.separator + PICTURE_LARGE;
            String largeAvatarPath = getSdCardPath() + File.separator + AVATAR_LARGE;

            path.add(thumbnailPath);
            path.add(middlePath);
            path.add(oriPath);
            path.add(largeAvatarPath);
        }
        return path;
    }

    public static String getPictureCacheSize() {
        long size = 0L;
        if (isExternalStorageMounted()) {
            String thumbnailPath = getSdCardPath() + File.separator + PICTURE_THUMBNAIL;
            String middlePath = getSdCardPath() + File.separator + PICTURE_BMIDDLE;
            String oriPath = getSdCardPath() + File.separator + PICTURE_LARGE;
            size += new FileSize(new File(thumbnailPath)).getLongSize();
            size += new FileSize(new File(middlePath)).getLongSize();
            size += new FileSize(new File(oriPath)).getLongSize();

        }
        return FileSize.convertSizeToString(size);
    }

    public static boolean deleteCache() {
        String path = getSdCardPath() + File.separator;
        return deleteDirectory(new File(path));
    }

    public static boolean deletePictureCache() {
        String thumbnailPath = getSdCardPath() + File.separator + PICTURE_THUMBNAIL;
        String middlePath = getSdCardPath() + File.separator + PICTURE_BMIDDLE;
        String oriPath = getSdCardPath() + File.separator + PICTURE_LARGE;

        deleteDirectory(new File(thumbnailPath));
        deleteDirectory(new File(middlePath));
        deleteDirectory(new File(oriPath));

        return true;
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

    public static boolean saveToPicDir(String path) {
        if (!isExternalStorageMounted())
            return false;

        File file = new File(path);
        String name = file.getName();
        String newPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + name;
        try {
            FileManager.createNewFileInSDCard(newPath);
            copyFile(file, new File(newPath));
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
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }
}
