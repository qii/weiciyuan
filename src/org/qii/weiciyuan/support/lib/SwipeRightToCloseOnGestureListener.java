package org.qii.weiciyuan.support.lib;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import org.qii.weiciyuan.support.utils.AppConfig;

/**
 * User: qii
 * Date: 13-2-26
 */
public class SwipeRightToCloseOnGestureListener extends GestureDetector.SimpleOnGestureListener {
    private Activity activity;
    private ViewPager viewPager;
    protected MotionEvent mLastOnDownEvent = null;

    public SwipeRightToCloseOnGestureListener(Activity activity, ViewPager viewPager) {
        this.activity = activity;
        this.viewPager = viewPager;
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
        if (Math.abs(e1.getRawX() - e2.getRawX()) > AppConfig.SWIPE_MIN_DISTANCE
                && this.viewPager.getCurrentItem() == 0) {
            this.activity.finish();
            return true;
        }
        return false;
    }
}
