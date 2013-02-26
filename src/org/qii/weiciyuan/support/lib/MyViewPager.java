package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * User: qii
 * Date: 13-2-26
 */
public class MyViewPager extends ViewPager {

    private GestureDetector gestureDetector;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.gestureDetector != null)
            this.gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);

    }
}