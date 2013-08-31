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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-12-18
 * todo
 * this class and its child class need to be refactored
 */
public class TimeLineImageView extends FrameLayout {

    protected ImageView mImageView;
    private ImageView gifFlag;
    private ProgressBar pb;

    public TimeLineImageView(Context context) {
        super(context);
    }

    public TimeLineImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    //todo need refactor
    public TimeLineImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout(context);
    }

    protected void initLayout(Context context) {
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.timelineimageview_layout, this, true);
        mImageView = (ImageView) v.findViewById(R.id.imageview);
        mImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        gifFlag = (ImageView) v.findViewById(R.id.gif_flag);

        pb = (ProgressBar) v.findViewById(R.id.imageview_pb);
        this.setForeground(getResources().getDrawable(R.drawable.timelineimageview_cover));
        this.setAddStatesFromChildren(true);
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
        return super.onTouchEvent(event);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    @Override
    public void setOnClickListener(OnClickListener onClicker) {
        mImageView.setOnClickListener(onClicker);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        mImageView.setOnLongClickListener(onLongClickListener);
    }

    public void setProgress(int value, int max) {
        pb.setVisibility(View.VISIBLE);
        pb.setMax(max);
        pb.setProgress(value);
    }

    public ProgressBar getProgressBar() {
        return pb;
    }

    public void setGifFlag(boolean value) {
        gifFlag.setVisibility(value ? VISIBLE : INVISIBLE);
    }
}


