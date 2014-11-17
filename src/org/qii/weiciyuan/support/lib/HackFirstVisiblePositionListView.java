package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * hack to fix getFirstVisiblePosition
 * you cant use JavaReflectionUtility.setValue(this, "mFirstPosition", position), the ListView draw will become strange(sometimes the first
 * item will become invisible after setSelection)
 */
public class HackFirstVisiblePositionListView extends ListView {

    private int mCorrectFirstVisiblePosition = -1;

    public HackFirstVisiblePositionListView(Context context) {
        super(context);
    }

    public HackFirstVisiblePositionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HackFirstVisiblePositionListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HackFirstVisiblePositionListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        return mCorrectFirstVisiblePosition;
    }
}
