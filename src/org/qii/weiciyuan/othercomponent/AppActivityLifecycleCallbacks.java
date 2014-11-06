package org.qii.weiciyuan.othercomponent;

import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.lib.LogOnExceptionScheduledExecutor;
import org.qii.weiciyuan.support.utils.GlobalContext;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AppActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static final int SCHEDULE_DELAY_MILLS = 3000;
    private static final int FETCH_PERIOD_SECONDS = 30;

    private int visibleActivityCount = 0;

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private LogOnExceptionScheduledExecutor logOnExceptionScheduledExecutor;
    private ScheduledFuture scheduledFuture;

    public AppActivityLifecycleCallbacks() {
        logOnExceptionScheduledExecutor = new LogOnExceptionScheduledExecutor(1);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        AppLogger.i("Activity is created, class name : " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        AppLogger.i("Activity is started, class name : " + activity.getClass()
                .getSimpleName());
        if (visibleActivityCount == 0) {
            startFetchUnread();
        }
        visibleActivityCount++;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        AppLogger.i("Activity is stopped, class name : " + activity.getClass()
                .getSimpleName());
        visibleActivityCount--;
        if (visibleActivityCount == 0) {
            stopFetchUnread();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        AppLogger.i("Activity saved instance state, class name : " + activity.getClass()
                .getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        AppLogger.i("Activity is destroyed, class name : " + activity.getClass().getSimpleName());
    }

    private void startFetchUnread() {
        uiHandler.postDelayed(scheduleFetchUnreadRunnable, SCHEDULE_DELAY_MILLS);
        uiHandler.removeCallbacks(unScheduleFetchUnreadRunnable);
    }

    private void stopFetchUnread() {
        uiHandler.postDelayed(unScheduleFetchUnreadRunnable, SCHEDULE_DELAY_MILLS);
        uiHandler.removeCallbacks(scheduleFetchUnreadRunnable);
    }

    private Runnable scheduleFetchUnreadRunnable = new Runnable() {
        @Override
        public void run() {
            AppLogger.i("Schedule fetch unread message");
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
            scheduledFuture = logOnExceptionScheduledExecutor
                    .scheduleAtFixedRate(fetchRunnable, 0,
                            FETCH_PERIOD_SECONDS, TimeUnit.SECONDS);
        }
    };

    private Runnable unScheduleFetchUnreadRunnable = new Runnable() {
        @Override
        public void run() {
            AppLogger.i("Stop schedule fetch unread message");
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        }
    };

    private Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {
            AppLogger.i("Start fetch unread message service");
            GlobalContext.getInstance().startService(FetchNewMsgService.newIntentFromOpenApp());
        }
    };
}


