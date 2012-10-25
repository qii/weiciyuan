package org.qii.weiciyuan.othercomponent;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;

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
        AppLogger.d("clear cache task start");
        ConnectivityManager cm = (ConnectivityManager)
                GlobalContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

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

    private void handleDir(File file) {
        File[] fileArray = file.listFiles();
        if (fileArray != null && fileArray.length != 0) {
            for (File fileSI : fileArray) {
                if (fileSI.isDirectory()) {
                    handleDir(fileSI);
                }

                if (fileSI.isFile()) {
                    handleFile(fileSI);
                }
            }
        }
    }

    private void handleFile(File file) {
        long time = file.lastModified();
        long calcMills = now - time;
        long day = TimeUnit.MILLISECONDS.toDays(calcMills);
        if (day > AppConfig.SAVED_DAYS) {
            file.delete();
        }
    }
}
