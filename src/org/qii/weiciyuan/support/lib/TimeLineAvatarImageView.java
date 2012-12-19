package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-12-19
 */
public class TimeLineAvatarImageView extends TimeLineImageView {

    private ImageView vImageView;

    public TimeLineAvatarImageView(Context context) {
        super(context);
    }

    public TimeLineAvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineAvatarImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.timelineimageview_avatar_layout, null);
        mImageView = (ImageView) v.findViewById(R.id.imageview);
        mCover = (ImageView) v.findViewById(R.id.imageview_cover);
        vImageView = (ImageView) v.findViewById(R.id.imageview_v);
        mImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        mCover.setImageDrawable(getResources().getDrawable(R.drawable.timelineimageview_cover));
        mCover.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mImageView.onTouchEvent(event);
                return false;
            }
        });
        v.setBackgroundColor(Color.TRANSPARENT);
        addView(v, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void isVerified() {
        vImageView.setImageDrawable(getResources().getDrawable(R.drawable.portrait_v_yellow));
    }

    public void reset() {
        vImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
