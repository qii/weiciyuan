package org.qii.weiciyuan.support.lib;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import org.qii.weiciyuan.support.utils.AppConfig;

/**
 * User: qii
 * Date: 13-2-26
 */
public class SwipeRightToCloseOnGestureListener extends GestureDetector.SimpleOnGestureListener {
    private Activity activity;
    private ViewPager viewPager;
    protected MotionEvent mLastOnDownEvent = null;
    private int scaledMinimumFlingVelocity;

    public SwipeRightToCloseOnGestureListener(Activity activity, ViewPager viewPager) {
        this.activity = activity;
        this.viewPager = viewPager;
        this.scaledMinimumFlingVelocity = ViewConfiguration.get(activity).getScaledMinimumFlingVelocity();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mLastOnDownEvent = e;
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null)
            e1 = mLastOnDownEvent;
        if (e1 == null || e2 == null)
            return false;
        if (e2.getRawX() - e1.getRawX() > AppConfig.SWIPE_MIN_DISTANCE
                && this.viewPager.getCurrentItem() == 0 && Math.abs(velocityX) > scaledMinimumFlingVelocity) {
            this.activity.finish();
            return true;
        }
        return false;
    }
}
