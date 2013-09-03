package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.asyncdrawable.IWeiciyuanDrawable;

/**
 * User: qii
 * Date: 12-12-18
 * todo
 * this class and its child class need to be refactored
 */
public class TimeLineImageView extends FrameLayout implements IWeiciyuanDrawable {

    protected ImageView mImageView;
    private ImageView gifFlag;
    private ProgressBar pb;
    private boolean parentPressState = true;

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


    public ImageView getImageView() {
        return mImageView;
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

    @Override
    public void checkVerified(UserBean user) {

    }

    @Override
    public void setPressesStateVisibility(boolean value) {
        if (parentPressState == value)
            return;
        setForeground(value ? getResources().getDrawable(R.drawable.timelineimageview_cover) : null);
        parentPressState = value;
    }
}


