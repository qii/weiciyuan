package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 13-9-1
 */
public class ProfileTopAvatarImageView extends TimeLineAvatarImageView {

    public ProfileTopAvatarImageView(Context context) {
        super(context);
    }

    public ProfileTopAvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProfileTopAvatarImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void initLayout(Context context) {
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.profileimageview_avatar_layout, this, true);
        mImageView = (ImageView) v.findViewById(R.id.imageview);
        vImageView = (ImageView) v.findViewById(R.id.imageview_v);
        mImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.setForeground(getResources().getDrawable(R.drawable.timelineimageview_cover));
        this.setAddStatesFromChildren(true);
    }


}
