package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * hack to fix getFirstVisiblePosition
 * you cant use JavaReflectionUtility.setValue(this, "mFirstPosition", position), the ListView draw will become strange(sometimes the first
 * item will become invisible after setSelection)
 */
public class HackFirstVisiblePositionListView extends ListView {

    private int mCorrectFirstVisiblePosition = -1;
    private final ForwardingOnScrollListener mForwardingOnScrollListener
            = new ForwardingOnScrollListener();

    public HackFirstVisiblePositionListView(Context context) {
        super(context);
        init();
    }

    public HackFirstVisiblePositionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HackFirstVisiblePositionListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public HackFirstVisiblePositionListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        super.setOnScrollListener(mForwardingOnScrollListener);
        mForwardingOnScrollListener.selfListener = mOnScrollListener;
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        mCorrectFirstVisiblePosition = position;
    }

    @Override
    public void setSelectionAfterHeaderView() {
        super.setSelectionAfterHeaderView();
        mCorrectFirstVisiblePosition = 0;
    }

    @Override
    public void setSelectionFromTop(int position, int y) {
        super.setSelectionFromTop(position, y);
        mCorrectFirstVisiblePosition = position;
    }

    @Override
    public int getFirstVisiblePosition() {
        return mCorrectFirstVisiblePosition == -1 ? super.getFirstVisiblePosition() : mCorrectFirstVisiblePosition;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mForwardingOnScrollListener.clientListener.add(l);
    }

    private static class ForwardingOnScrollListener implements OnScrollListener {

        private OnScrollListener selfListener;

        private List<OnScrollListener> clientListener = new ArrayList<OnScrollListener>();

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {

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

        }

        @Override
        public void onScroll(AbsListView view, int firstVisiblePosition, int visibleItemCount,
                int totalItemCount) {
            mCorrectFirstVisiblePosition = HackFirstVisiblePositionListView.super.getFirstVisiblePosition();
        }
    };
}
