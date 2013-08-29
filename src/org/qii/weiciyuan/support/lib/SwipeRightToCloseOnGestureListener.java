package org.qii.weiciyuan.support.lib;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: qii
 * Date: 13-2-26
 */
public class SwipeRightToCloseOnGestureListener extends GestureDetector.SimpleOnGestureListener {
    private ViewPager viewPager;
    protected MotionEvent mLastOnDownEvent = null;
    private float[] firstPosition = new float[2];
    private View topView;


    public SwipeRightToCloseOnGestureListener(Activity activity, ViewPager viewPager) {
        this.viewPager = viewPager;
        this.topView = ((View) (activity.findViewById(android.R.id.content).getParent()));
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mLastOnDownEvent = e;
        firstPosition[0] = e.getRawX();
        firstPosition[1] = e.getRawY();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e2.getRawX() < firstPosition[0]) {
            float y = this.topView.getTranslationX();
            if (y != 0f) {
                this.topView.setTranslationX(0);
                return super.onScroll(e1, e2, distanceX, distanceY);
            } else {
                return false;
            }
        }
        final int MAX_MOTION_EVENT_DOWN__X_POSITION = Utility.dip2px(25);
        float s = e2.getRawX() - firstPosition[0];
        if ((this.viewPager == null || this.viewPager.getCurrentItem() == 0) && firstPosition[0] <= MAX_MOTION_EVENT_DOWN__X_POSITION) {
            this.topView.setTranslationX(s);
            return super.onScroll(e1, e2, distanceX, distanceY);
        } else {
            return false;
        }
    }

}
