package org.qii.weiciyuan.support.crashmanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * User: qii
 * Date: 13-3-21
 */
public class CrashManagerConstants {
    static String APP_VERSION = null;
    static String APP_PACKAGE = null;
    static String ANDROID_VERSION = null;
    static String PHONE_MODEL = null;
    static String PHONE_MANUFACTURER = null;


    public static void loadFromContext(Context context) {

        CrashManagerConstants.ANDROID_VERSION = android.os.Build.VERSION.RELEASE;
        CrashManagerConstants.PHONE_MODEL = android.os.Build.MODEL;
        CrashManagerConstants.PHONE_MANUFACTURER = android.os.Build.MANUFACTURER;

        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            CrashManagerConstants.APP_VERSION = "" + packageInfo.versionCode;
            CrashManagerConstants.APP_PACKAGE = packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
