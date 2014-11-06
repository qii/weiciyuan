package org.qii.weiciyuan.support.lib;

import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: qii
 * Date: 14-7-26
 * ScheduledThreadPoolExecutor will stop when exception occur, and it wont notify us, it stop
 * silently
 * here we catch exception, write to log, then stop
 */
public class LogOnExceptionScheduledExecutor extends ScheduledThreadPoolExecutor {

    public LogOnExceptionScheduledExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    public ScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period,
            TimeUnit unit) {
        return super.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
            TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit);
    }

    private Runnable wrapRunnable(Runnable command) {
        return new LogOnExceptionRunnable(command);
    }

    private class LogOnExceptionRunnable implements Runnable {

        private Runnable runnable;

        public LogOnExceptionRunnable(Runnable runnable) {
            super();
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Exception e) {

                AppLogger.e(
                        "error in executing: " + runnable + ". It will no longer be run!");
                e.printStackTrace();

                throw new RuntimeException(e);
            }
        }
    }
}