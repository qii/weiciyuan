package org.qii.weiciyuan.support.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: qii
 * Date: 13-2-26
 */
public class MyViewPager extends ViewPager {

    private Activity activity;

    private GestureDetector gestureDetector;
    private boolean isDragging = false;
    private float[] firstPosition = new float[2];
    private View topView;

    private int operationItemPosition = -1;
    private static final int OFFSET = 5;

    private int max_motion_event_down_x_position;


    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGestureDetector(Activity activity, GestureDetector gestureDetector) {
        this.activity = activity;
        this.gestureDetector = gestureDetector;
        this.topView = ((View) (activity.findViewById(android.R.id.content).getParent()));
        this.max_motion_event_down_x_position = Utility.dip2px(25);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (operationItemPosition != 0) {
            return super.onTouchEvent(ev);
        }

        if (this.gestureDetector == null) {
            return super.onTouchEvent(ev);
        }

        if (this.gestureDetector != null)
            this.gestureDetector.onTouchEvent(ev);


        if ((ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL)
                && firstPosition[0] <= max_motion_event_down_x_position) {
            int x = (int) (ev.getRawX() - firstPosition[0]);
            firstPosition[0] = 0f;
            firstPosition[1] = 0f;
            isDragging = false;

            if (x > (Utility.getScreenWidth() / 2)) {
                activity.finish();
                activity.overridePendingTransition(R.anim.stay, R.anim.swipe_right_to_close);
                return true;
            } else if (getCurrentItem() == 0) {
                this.topView.animate().translationX(0)
                        .setDuration(300L).withLayer().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        topView.animate().setListener(null);
                    }
                });
            }
        }
        if (isDragging) {
            return true;
        }
        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float x = ev.getRawX();
            if ((x > firstPosition[0] + Utility.dip2px(OFFSET)) && firstPosition[0] <= max_motion_event_down_x_position) {
                AppLogger.e("begin swipe to right");
                isDragging = true;
                return true;
            }
        }

        return super.onTouchEvent(ev);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.gestureDetector == null) {
            return super.onTouchEvent(ev);
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                firstPosition[0] = ev.getRawX();
                firstPosition[1] = ev.getRawY();
                operationItemPosition = getCurrentItem();
                break;
        }
        if (this.gestureDetector != null && ev.getActionMasked() == MotionEvent.ACTION_DOWN)
            this.gestureDetector.onTouchEvent(ev);

        return super.dispatchTouchEvent(ev);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.gestureDetector == null) {
            return super.onTouchEvent(ev);
        }

        if (isDragging) {
            return true;
        }

        if (operationItemPosition == 0) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_MOVE:
                    float x = ev.getRawX();
                    if (x > firstPosition[0] && firstPosition[0] <= max_motion_event_down_x_position) {
                        return true;
                    }
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
}