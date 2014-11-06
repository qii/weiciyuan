package org.qii.weiciyuan.support.lib;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.AnimationUtility;
import org.qii.weiciyuan.support.utils.ThemeUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * User: qii
 * Date: 13-10-15
 */
public class SwipeFrameLayout extends FrameLayout {

    private Activity activity;

    private boolean isDragging = false;
    private float[] initPointLocation = new float[2];

    private View topView;

    private static final int OFFSET = 5;

    private int max_motion_event_down_x_position;

    private GestureDetector gestureDetector;
    private OverScroller scroller;

    private Handler uiHandler = new Handler();

    public SwipeFrameLayout(Context context) {
        this(context, null);
    }

    public SwipeFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SwipeFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        scroller = new OverScroller(getContext(), new DecelerateInterpolator());
        setBackground(ThemeUtility.getDrawable(android.R.attr.windowBackground));
        this.activity = (Activity) getContext();
        this.topView = ((View) (activity.findViewById(android.R.id.content).getParent()));
        this.max_motion_event_down_x_position = Utility.dip2px(25);
        this.gestureDetector = new GestureDetector(getContext(),
                new SwipeRightToCloseOnGestureListener());
        this.setId(R.id.swipe_framelayout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (scroller.computeScrollOffset()) {
            scroller.abortAnimation();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                initPointLocation[0] = ev.getRawX();
                initPointLocation[1] = ev.getRawY();

                return false;
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    gestureDetector.onTouchEvent(ev);
                    return true;
                }

                float xx = ev.getRawX();
                if ((xx > initPointLocation[0] + Utility.dip2px(OFFSET))
                        && initPointLocation[0] <= max_motion_event_down_x_position) {

                    isDragging = true;
                    gestureDetector.onTouchEvent(ev);

                    return true;
                }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;

                if (initPointLocation[0] <= max_motion_event_down_x_position) {
                    int x = (int) (ev.getRawX() - initPointLocation[0]);
                    initPointLocation[0] = 0f;
                    initPointLocation[1] = 0f;

                    if (x > (Utility.getScreenWidth() / 2)) {
                        closeActivity();
                        return true;
                    } else {
                        restoreActivity();
                    }
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    private boolean translucent;

    private Runnable forceConvertActivityFromTranslucentRunnable;

    private void forceConvertActivityFromTranslucent() {

        if (forceConvertActivityFromTranslucentRunnable != null) {
            uiHandler.removeCallbacks(forceConvertActivityFromTranslucentRunnable);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing()) {
                    return;
                }
                AnimationUtility.forceConvertActivityFromTranslucent(activity);
                forceConvertActivityFromTranslucentRunnable = null;
            }
        };

        forceConvertActivityFromTranslucentRunnable = runnable;
        uiHandler.postDelayed(forceConvertActivityFromTranslucentRunnable, 3000);
    }

    private void forceConvertActivityToTranslucent() {
        if (forceConvertActivityFromTranslucentRunnable != null) {
            uiHandler.removeCallbacks(forceConvertActivityFromTranslucentRunnable);
        } else {
            AnimationUtility.forceConvertActivityToTranslucent(activity);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (!translucent && isDownEventInsideSwipeRegion(ev)) {
            forceConvertActivityToTranslucent();
            translucent = true;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                initPointLocation[0] = ev.getRawX();
                initPointLocation[1] = ev.getRawY();
                this.gestureDetector.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!isDragging && translucent) {
                    forceConvertActivityFromTranslucent();

                    translucent = false;
                }

                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (isDragging) {
            return true;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (canSwipe(ev)) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isDownEventInsideSwipeRegion(MotionEvent ev) {
        float x = ev.getRawX();
        return x <= max_motion_event_down_x_position;
    }

    private boolean canSwipe(MotionEvent ev) {
        float x = ev.getRawX();
        return x > initPointLocation[0]
                && initPointLocation[0] <= max_motion_event_down_x_position;
    }

    private void closeActivity() {
        if (translucent) {
            forceConvertActivityFromTranslucent();

            translucent = false;
        }
        activity.finish();
        activity.overridePendingTransition(R.anim.stay, R.anim.swipe_right_to_close);
    }

    private void restoreActivity() {
        if (lastScrollRunnable != null) {
            removeCallbacks(lastScrollRunnable);
        }

        scroller.startScroll(topView.getScrollX(), 0, -topView.getScrollX(), 0);
        lastScrollRunnable = new ScrollRunnable();
        post(lastScrollRunnable);
    }

    private ScrollRunnable lastScrollRunnable;

    private class ScrollRunnable implements Runnable {

        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                int currentValue = scroller.getCurrX();
                topView.scrollTo(currentValue, 0);
                topView.invalidate();
                post(this);
            } else if (translucent && !isDragging) {
                forceConvertActivityFromTranslucent();

                translucent = false;
                if (lastScrollRunnable == this) {
                    lastScrollRunnable = null;
                }
            }
        }
    }

    private class SwipeRightToCloseOnGestureListener
            extends GestureDetector.SimpleOnGestureListener {

        protected MotionEvent mLastOnDownEvent = null;

        private float[] initPointLocation = new float[2];

        @Override
        public boolean onDown(MotionEvent e) {
            mLastOnDownEvent = e;
            initPointLocation[0] = e.getRawX();
            initPointLocation[1] = e.getRawY();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (e2.getRawX() < initPointLocation[0]) {
                float y = topView.getScrollX();
                if (y != 0f) {
                    restoreActivity();
                    return super.onScroll(e1, e2, distanceX, distanceY);
                } else {
                    return false;
                }
            }
            final int MAX_MOTION_EVENT_DOWN__X_POSITION = Utility.dip2px(25);
            float s = e2.getRawX() - initPointLocation[0];
            if (initPointLocation[0] <= MAX_MOTION_EVENT_DOWN__X_POSITION) {
                topView.scrollTo((int) -s, 0);
                topView.invalidate();
                return true;
            } else {
                return false;
            }
        }
    }
}
