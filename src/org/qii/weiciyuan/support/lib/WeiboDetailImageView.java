package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 13-7-15
 */
public class WeiboDetailImageView extends FrameLayout {

    protected ImageView mImageView;
    protected ImageView mCover;
    private ProgressBar pb;
    private Button retry;

    public WeiboDetailImageView(Context context) {
        super(context);
    }

    public WeiboDetailImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeiboDetailImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.weibodetailimageview_layout, null);
        mImageView = (ImageView) v.findViewById(R.id.imageview);
        mCover = (ImageView) v.findViewById(R.id.imageview_cover);
        mImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        mCover.setImageDrawable(getResources().getDrawable(R.drawable.timelineimageview_cover));
        mCover.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mImageView.onTouchEvent(event);
                return false;
            }
        });
        pb = (ProgressBar) v.findViewById(R.id.imageview_pb);
        retry = (Button) v.findViewById(R.id.retry);
        v.setBackgroundColor(Color.TRANSPARENT);
        addView(v, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setCoverDrawable(Drawable drawable) {
        mCover.setImageDrawable(drawable);
    }

    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bm) {
        mImageView.setImageBitmap(bm);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mImageView.onTouchEvent(event);
        mCover.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    @Override
    public void setOnClickListener(OnClickListener onClicker) {
        mImageView.setOnClickListener(onClicker);
        mCover.setClickable(true);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        mImageView.setOnLongClickListener(onLongClickListener);
    }

    public void setProgress(int value, int max) {
        if (pb.getVisibility() != View.VISIBLE) {
            pb.setVisibility(View.VISIBLE);
        }
        if (pb.getMax() != max)
            pb.setMax(max);
        pb.setProgress(value);
    }

    public ProgressBar getProgressBar() {
        return pb;
    }

    public Button getRetryButton() {
        return retry;
    }
}



