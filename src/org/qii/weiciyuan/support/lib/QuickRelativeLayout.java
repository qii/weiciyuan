package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * User: qii
 * Date: 13-11-18
 */
public class QuickRelativeLayout extends ViewGroup {

    private static final int AVATAR_INDEX = 0;
    private static final int COUNT_INDEX = 1;
    private static final int USERNAME_INDEX = 2;
    private static final int TIME_INDEX = 3;
    private static final int SOURCE_INDEX = 4;
    private static final int CONTENT_INDEX = 5;
    private static final int REPOST_FLAG_INDEX = 6;
    private static final int REPOST_CONTENT_INDEX = 7;
    private static final int REPOST_CONTENT_PIC_INDEX = 8;
    private static final int REPOST_CONTENT_PIC_MULTI_INDEX = 9;

    public QuickRelativeLayout(Context context) {
        super(context);
    }

    public QuickRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuickRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.UNSPECIFIED) {
            throw new IllegalArgumentException(
                    "this viewgroup only can be used in ListView and its layout_width must be match_parent");
        }

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingRight = getPaddingRight();

        int avatarHeight = 0;
        int avatarWidth = 0;

        int countHeight = 0;
        int countWidth = 0;

        int usernameHeight = 0;
        int usernameWidth = 0;

        int timeHeight = 0;
        int timeWidth = 0;

        int sourceHeight = 0;
        int sourceWidth = 0;

        int contentMaxWidth = widthSize - paddingLeft - paddingRight;

        int childCount = getChildCount();

        int contentHeight = 0;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            final QuickRelativeLayout.LayoutParams lp = (QuickRelativeLayout.LayoutParams) child
                    .getLayoutParams();
            int defaultWidth = lp.width;
            int defaultHeight = lp.height;

            int heightSpec = 0;
            int widthSpec = 0;

            switch (i) {

                case AVATAR_INDEX:
                    widthSpec = MeasureSpec.makeMeasureSpec(defaultWidth, MeasureSpec.EXACTLY);
                    heightSpec = MeasureSpec.makeMeasureSpec(defaultHeight, MeasureSpec.EXACTLY);
                    child.measure(widthSpec, heightSpec);

                    avatarWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    avatarHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

                    lp.mLeft = paddingLeft + lp.leftMargin;
                    lp.mTop = paddingTop + lp.topMargin;
                    lp.mRight = lp.mLeft + child.getMeasuredWidth();
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();
                    break;
                case COUNT_INDEX:
                    widthSpec = MeasureSpec.makeMeasureSpec(contentMaxWidth, MeasureSpec.AT_MOST);
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    child.measure(widthSpec, heightSpec);

                    countWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    countHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

                    lp.mRight = widthSize - paddingRight - lp.rightMargin;
                    lp.mLeft = lp.mRight - child.getMeasuredWidth();
                    lp.mTop = paddingTop + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    break;
                case USERNAME_INDEX:
                    int childWidth = contentMaxWidth - avatarWidth - countWidth - lp.leftMargin
                            - lp.rightMargin;
                    widthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST);
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    child.measure(widthSpec, heightSpec);

                    usernameWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    usernameHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

                    lp.mLeft = paddingLeft + avatarWidth + lp.leftMargin;

                    lp.mRight = lp.mLeft + child.getMeasuredWidth();
                    lp.mTop = paddingTop + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    break;
                case TIME_INDEX:
                    int timeChildMaxWidth = contentMaxWidth - avatarWidth - lp.leftMargin
                            - lp.rightMargin;
                    widthSpec = MeasureSpec.makeMeasureSpec(timeChildMaxWidth, MeasureSpec.AT_MOST);
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    child.measure(widthSpec, heightSpec);

                    timeWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    timeHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

                    lp.mLeft = paddingLeft + avatarWidth + lp.leftMargin;
                    lp.mRight = lp.mLeft + child.getMeasuredWidth();

                    lp.mTop = paddingTop + usernameHeight + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    break;
                case SOURCE_INDEX:
                    int sourceChildMaxWidth = contentMaxWidth - avatarWidth - timeWidth
                            - lp.leftMargin - lp.rightMargin;
                    widthSpec = MeasureSpec
                            .makeMeasureSpec(sourceChildMaxWidth, MeasureSpec.AT_MOST);
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    child.measure(widthSpec, heightSpec);

                    sourceWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    sourceHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

                    lp.mLeft = paddingLeft + avatarWidth + timeWidth + lp.leftMargin;
                    lp.mRight = lp.mLeft + child.getMeasuredWidth();

                    lp.mTop = paddingTop + usernameHeight + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    break;
                case CONTENT_INDEX:
                    int contentChildMaxWidth = contentMaxWidth - lp.leftMargin - lp.rightMargin;
                    widthSpec = MeasureSpec
                            .makeMeasureSpec(contentChildMaxWidth, MeasureSpec.EXACTLY);
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    child.measure(widthSpec, heightSpec);

                    lp.mLeft = paddingLeft + lp.leftMargin;
                    lp.mRight = lp.mLeft + child.getMeasuredWidth();

                    lp.mTop = paddingTop + avatarHeight + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    contentHeight = lp.mBottom + lp.bottomMargin;

                    break;
                case REPOST_FLAG_INDEX:
                case REPOST_CONTENT_INDEX:
                    int repostContentChildMaxWidth = contentMaxWidth - lp.leftMargin
                            - lp.rightMargin;
                    widthSpec = MeasureSpec
                            .makeMeasureSpec(repostContentChildMaxWidth, MeasureSpec.EXACTLY);
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    child.measure(widthSpec, heightSpec);

                    lp.mLeft = paddingLeft + lp.leftMargin;
                    lp.mRight = lp.mLeft + child.getMeasuredWidth();

                    lp.mTop = contentHeight + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    contentHeight = lp.mBottom + lp.bottomMargin;
                    break;
                case REPOST_CONTENT_PIC_INDEX:
                    int repostContentPicChildMaxWidth = contentMaxWidth - lp.leftMargin
                            - lp.rightMargin;
                    widthSpec = MeasureSpec
                            .makeMeasureSpec(repostContentPicChildMaxWidth, MeasureSpec.EXACTLY);
                    heightSpec = MeasureSpec.makeMeasureSpec(defaultHeight, MeasureSpec.EXACTLY);
                    child.measure(widthSpec, heightSpec);

                    lp.mLeft = paddingLeft + lp.leftMargin;
                    lp.mRight = lp.mLeft + child.getMeasuredWidth();

                    lp.mTop = contentHeight + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    contentHeight = lp.mBottom + lp.bottomMargin;
                    break;
                case REPOST_CONTENT_PIC_MULTI_INDEX:
                    int repostContentMultiPicChildMaxWidth = contentMaxWidth - lp.leftMargin
                            - lp.rightMargin;
                    widthSpec = MeasureSpec.makeMeasureSpec(repostContentMultiPicChildMaxWidth,
                            MeasureSpec.EXACTLY);
                    heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    child.measure(widthSpec, heightSpec);

                    lp.mLeft = paddingLeft + lp.leftMargin;
                    lp.mRight = lp.mLeft + child.getMeasuredWidth();

                    lp.mTop = contentHeight + lp.topMargin;
                    lp.mBottom = lp.mTop + child.getMeasuredHeight();

                    contentHeight = lp.mBottom + lp.bottomMargin;
                    break;
            }
        }

        contentHeight += paddingBottom;

        setMeasuredDimension(widthSize, contentHeight);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public QuickRelativeLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new QuickRelativeLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected QuickRelativeLayout.LayoutParams generateDefaultLayoutParams() {
        return new QuickRelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {

            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.layout(lp.mLeft, lp.mTop, lp.mRight, lp.mBottom);
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        private int mLeft, mTop, mRight, mBottom;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
        }
    }
}
