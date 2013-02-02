package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Utility {

    private Utility() {
        // Forbidden being instantiated.
    }

    public static String encodeUrl(Map<String, String> param) {
        if (param == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        Set<String> keys = param.keySet();
        boolean first = true;

        for (String key : keys) {
            String value = param.get(key);
            if (!TextUtils.isEmpty(value)) {
                if (first)
                    first = false;
                else
                    sb.append("&");
                try {
                    sb.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(param.get(key), "UTF-8"));
                } catch (UnsupportedEncodingException e) {

                }
            }


        }

        return sb.toString();
    }

    public static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String array[] = s.split("&");
            for (String parameter : array) {
                String v[] = parameter.split("=");
                try {
                    params.putString(URLDecoder.decode(v[0], "UTF-8"), URLDecoder.decode(v[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        }
        return params;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (IOException ignored) {

            }
    }

    /**
     * Parse a URL query and fragment parameters into a key-value bundle.
     */
    public static Bundle parseUrl(String url) {
        // hack to prevent MalformedURLException
        url = url.replace("weiboconnect", "http");
        try {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    public static void cancelTasks(MyAsyncTask... tasks) {
        for (MyAsyncTask task : tasks) {
            if (task != null)
                task.cancel(true);
        }
    }

    public static boolean isTaskStopped(MyAsyncTask task) {
        return task == null || task.getStatus() == MyAsyncTask.Status.FINISHED;
    }

    public static void stopListViewScrollingAndScrollToTop(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            listView.setSelection(0);
            listView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));

        } else {
            listView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
            listView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            listView.setSelection(0);
        }
    }

    public static int dip2px(int dipValue) {
        float reSize = GlobalContext.getInstance().getResources().getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }

    public static int px2dip(int pxValue) {
        float reSize = GlobalContext.getInstance().getResources().getDisplayMetrics().density;
        return (int) ((pxValue / reSize) + 0.5);
    }

    public static float sp2px(int spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, GlobalContext.getInstance().getResources().getDisplayMetrics());
    }

    public static int length(String paramString) {
        int i = 0;
        for (int j = 0; j < paramString.length(); j++) {
            if (paramString.substring(j, j + 1).matches("[Α-￥]"))
                i += 2;
            else
                i++;
        }

        if (i % 2 > 0) {
            i = 1 + i / 2;
        } else {
            i = i / 2;
        }

        return i;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public static int getNetType(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return networkInfo.getType();
        }
        return -1;
    }

    public static boolean isGprs(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSystemRinger(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }


    public static void configVibrateLedRingTone(Notification.Builder builder) {
        configRingTone(builder);
        configLed(builder);
        configVibrate(builder);
    }

    private static void configVibrate(Notification.Builder builder) {
        if (SettingUtility.allowVibrate()) {
            long[] pattern = {0, 200, 500};
            builder.setVibrate(pattern);
        }
    }

    private static void configRingTone(Notification.Builder builder) {
        Uri uri = null;

        if (!TextUtils.isEmpty(SettingUtility.getRingtone())) {
            uri = Uri.parse(SettingUtility.getRingtone());
        }

        if (uri != null && isSystemRinger(GlobalContext.getInstance())) {
            builder.setSound(uri);
        }
    }

    private static void configLed(Notification.Builder builder) {
        if (SettingUtility.allowLed()) {
            builder.setLights(Color.WHITE, 300, 1000);
        }

    }

    public static String getPicPathFromUri(Uri uri, Activity activity) {
        String value = uri.getPath();

        if (value.startsWith("/external")) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return value;
        }
    }

    public static boolean isAllNotNull(Object... obs) {
        for (int i = 0; i < obs.length; i++) {
            if (obs[i] == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGPSLocationCorrect(GeoBean geoBean) {
        double latitude = geoBean.getLat();
        double longitude = geoBean.getLon();
        if (latitude < -90.0 || latitude > 90.0) {
            return false;
        }
        if (longitude < -180.0 || longitude > 180.0) {
            return false;
        }
        return true;
    }

    public static boolean isIntentSafe(Activity activity, Uri uri) {
        Intent mapCall = new Intent(Intent.ACTION_VIEW, uri);
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);
        return activities.size() > 0;
    }

    public static boolean isIntentSafe(Activity activity, Intent intent) {
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }


    public static boolean isGooglePlaySafe(Activity activity) {
        Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms");
        Intent mapCall = new Intent(Intent.ACTION_VIEW, uri);
        mapCall.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        mapCall.setPackage("com.android.vending");
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);
        return activities.size() > 0;
    }

    public static String buildTabText(int number) {

        if (number == 0) {
            return null;
        }

        String num;
        if (number < 99) {
            num = "(" + number + ")";
        } else {
            num = "(99+)";
        }
        return num;

    }

    public static boolean isJB() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static int getScreenWidth() {
        Activity activity = GlobalContext.getInstance().getActivity();
        if (activity != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            return metrics.widthPixels;
        }

        return 480;
    }

    public static int getScreenHeight() {
        Activity activity = GlobalContext.getInstance().getActivity();
        if (activity != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            return metrics.heightPixels;
        }
        return 800;
    }

    public static Rect locateView(View v) {
        int[] location = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(location);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect locationRect = new Rect();
        locationRect.left = location[0];
        locationRect.top = location[1];
        locationRect.right = locationRect.left + v.getWidth();
        locationRect.bottom = locationRect.top + v.getHeight();
        return locationRect;
    }
}

