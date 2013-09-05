package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import org.qii.weiciyuan.support.utils.Utility;

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
        setPadding(Utility.dip2px(5), Utility.dip2px(5), Utility.dip2px(5), Utility.dip2px(5));

    }


}
