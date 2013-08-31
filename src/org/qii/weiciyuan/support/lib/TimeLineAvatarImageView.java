package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;

/**
 * User: qii
 * Date: 12-12-19
 */
public class TimeLineAvatarImageView extends TimeLineImageView {

    protected ImageView vImageView;

    public TimeLineAvatarImageView(Context context) {
        super(context);
    }

    public TimeLineAvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineAvatarImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void initLayout(Context context) {
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.timelineimageview_avatar_layout, this, true);
        mImageView = (ImageView) v.findViewById(R.id.imageview);
        vImageView = (ImageView) v.findViewById(R.id.imageview_v);
        mImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.setForeground(getResources().getDrawable(R.drawable.timelineimageview_cover));
        this.setAddStatesFromChildren(true);
    }

    public void checkVerified(UserBean user) {
        if (user.isVerified() && !TextUtils.isEmpty(user.getVerified_reason())) {
            if (user.getVerified_type() == 0) {
                isVerifiedPersonal();
            } else {
                isVerifiedEnterprise();
            }
        } else {
            reset();
        }
    }

    public void isVerifiedPersonal() {
        vImageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_vip));
    }

    public void isVerifiedEnterprise() {
        vImageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_enterprise_vip));
    }

    public void reset() {
        vImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
