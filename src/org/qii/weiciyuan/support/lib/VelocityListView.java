package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-3-31
 * modify to add
 * int currentItemCount = totalItemCount;
 * int v = currentItemCount - mLastItemCount;
 * mFirstVisiblePosition += v;
 * mLastVisiblePosition += v;
 * so, when notifyDataSetChanged adapter to show new item, the Velocity is also correct.
 * <p/>
 * and modify setOnScrollListener method, so that ListView can own more than just one OnScrollListener
 */
public class VelocityListView extends AutoScrollListView {

    /**
     * A callback to be notified the velocity has changed.
     *
     * @author Cyril Mottier
     */
    public interface OnVelocityListViewListener {
        void onVelocityChanged(int velocity);
    }

    public interface OnVelocityEqualZeroListener {
        void onZero();
    }

    private static final long INVALID_TIME = -1;

    /**
     * This value is really necessary to avoid weird velocity values. Indeed, in
     * fly-wheel mode, onScroll is called twice per-frame which results in
     * having a delta divided by a value close to zero. onScroll is usually
     * being called 60 times per seconds (i.e. every 16ms) so 10ms is a good
     * threshold.
     */
    private static final long MINIMUM_TIME_DELTA = 10L;

    private final ForwardingOnScrollListener mForwardingOnScrollListener = new ForwardingOnScrollListener();

    private OnVelocityListViewListener mOnVelocityListViewListener;

    private OnVelocityEqualZeroListener onVelocityEqualZeroListener;

    private long mTime = INVALID_TIME;
    private int mVelocity;

    private int mFirstVisiblePosition;
    private int mFirstVisibleViewTop;
    private int mLastVisiblePosition;
    private int mLastVisibleViewTop;
    private int mLastItemCount;

    public static final int TOWARDS_BOTTOM = 0;
    public static final int TOWARDS_TOP = 1;

    private int towardsOrientation = TOWARDS_BOTTOM;

    public VelocityListView(Context context) {
        super(context);
        init();
    }

    public VelocityListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VelocityListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.setOnScrollListener(mForwardingOnScrollListener);
        mForwardingOnScrollListener.selfListener = mOnScrollListener;
    }

    public void setOnVelocityEqualZeroListener(OnVelocityEqualZeroListener l) {
        onVelocityEqualZeroListener = l;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mForwardingOnScrollListener.clientListener.add(l);
    }

    public void setOnVelocityListener(OnVelocityListViewListener l) {
        mOnVelocityListViewListener = l;
    }

    /**
     * Return an approximative value of the ListView's current velocity on the
     * Y-axis. A negative value indicates the ListView is currently being
     * scrolled towards the bottom (i.e items are moving from bottom to top)
     * while a positive value indicates it is currently being scrolled towards
     * the top (i.e. items are moving from top to bottom).
     *
     * @return An approximative value of the ListView's velocity on the Y-axis
     */
    public int getVelocity() {
        return mVelocity;
    }

    private void setVelocity(int velocity) {
        if (mVelocity != velocity) {
            mVelocity = velocity;
            if (mOnVelocityListViewListener != null) {
                mOnVelocityListViewListener.onVelocityChanged(velocity);
            }

            if (onVelocityEqualZeroListener != null && (mTime == INVALID_TIME)) {
                onVelocityEqualZeroListener.onZero();
            }
            if (velocity < 0) {
                towardsOrientation = TOWARDS_BOTTOM;
            } else if (velocity > 0) {
                towardsOrientation = TOWARDS_TOP;

            }
        }
    }

    public int getTowardsOrientation() {
        return towardsOrientation;
    }

    /**
     * @author Cyril Mottier
     */
    private static class ForwardingOnScrollListener implements OnScrollListener {

        private OnScrollListener selfListener;
        private List<OnScrollListener> clientListener = new ArrayList<OnScrollListener>();

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (selfListener != null) {
                selfListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            for (OnScrollListener l : clientListener) {
                l.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (selfListener != null) {
                selfListener.onScrollStateChanged(view, scrollState);
            }
            for (OnScrollListener l : clientListener) {
                l.onScrollStateChanged(view, scrollState);
            }
        }
    }

    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case SCROLL_STATE_IDLE:
                    mTime = INVALID_TIME;
                    setVelocity(0);
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisiblePosition, int visibleItemCount, int totalItemCount) {

            final long now = AnimationUtils.currentAnimationTimeMillis();
            final int lastVisiblePosition = firstVisiblePosition + visibleItemCount - 1;

            //calc position,because ListView may has new item because adapter has something new
            int currentItemCount = totalItemCount;
            int v = currentItemCount - mLastItemCount;

            mFirstVisiblePosition += v;
            mLastVisiblePosition += v;

            if (mTime != INVALID_TIME) {

                final long delta = now - mTime;
                if (now - mTime > MINIMUM_TIME_DELTA) {
                    int distance = 0;
                    //@formatter:off
                    if (mFirstVisiblePosition >= firstVisiblePosition
                            && mFirstVisiblePosition <= lastVisiblePosition) {
                        distance = getChildAt(mFirstVisiblePosition - firstVisiblePosition).getTop() - mFirstVisibleViewTop;

                    } else if (mLastVisiblePosition >= firstVisiblePosition
                            && mLastVisiblePosition <= lastVisiblePosition) {
                        distance = getChildAt(mLastVisiblePosition - firstVisiblePosition).getTop() - mLastVisibleViewTop;
                        //@formatter:on
                    } else {
                        // We're in a case were the item we were previously
                        // referencing has moved out of the visible window.
                        // Let's compute an approximative distance
                        int heightSum = 0;
                        for (int i = 0; i < visibleItemCount; i++) {
                            heightSum += getChildAt(i).getHeight();
                        }

                        distance = heightSum / visibleItemCount * (mFirstVisiblePosition - firstVisiblePosition);
                    }

                    setVelocity((int) (1000d * distance / delta));
                }
            }

            mFirstVisiblePosition = firstVisiblePosition;
            mFirstVisibleViewTop = getChildAt(0).getTop();
            mLastVisiblePosition = lastVisiblePosition;
            mLastVisibleViewTop = getChildAt(visibleItemCount - 1).getTop();
            mLastItemCount = totalItemCount;
            mTime = now;
        }
    };

}