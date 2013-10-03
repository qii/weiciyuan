package org.qii.weiciyuan.othercomponent;

import android.text.TextUtils;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: qii
 * Date: 12-10-25
 */
public class ClearCacheTask implements Runnable {

    private long now = System.currentTimeMillis();

    @Override
    public void run() {

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        AppLogger.d("clear cache task start");

        if (Utility.isWifi(GlobalContext.getInstance())) {

            List<String> pathList = FileManager.getCachePath();

            for (String path : pathList) {
                if (!TextUtils.isEmpty(path))
                    handleDir(new File(path));
            }

            AppLogger.d("clear cache task stop");
        } else {
            AppLogger.d("this device dont have wifi network, clear cache task stop");
        }
    }

    private void clearEmptyDir(File file) {
        File[] fileArray = file.listFiles();
        if (fileArray == null)
            return;

        if (fileArray.length == 0) {
            if (file.delete()) {
                clearEmptyDir(file.getParentFile());
            }
        }
    }

    private void handleDir(File file) {
        File[] fileArray = file.listFiles();
        if (fileArray != null) {
            if (fileArray.length != 0) {
                for (File fileSI : fileArray) {
                    if (fileSI.isDirectory()) {
                        handleDir(fileSI);
                    }

                    if (fileSI.isFile()) {
                        handleFile(fileSI);
                    }
                }
            } else {
                clearEmptyDir(file);
            }
        }
    }

    private void handleFile(File file) {
        long time = file.lastModified();
        long calcMills = now - time;
        long day = TimeUnit.MILLISECONDS.toDays(calcMills);
        if (day > AppConfig.SAVED_DAYS) {
            AppLogger.d(file.getAbsolutePath());
            if (file.delete())
                clearEmptyDir(file.getParentFile());
        }
    }
}
