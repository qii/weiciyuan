package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.bean.UserBean;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * User: qii
 * Date: 13-9-3
 */
public interface IWeiciyuanDrawable {

    public void setImageDrawable(Drawable drawable);

    public void setImageBitmap(Bitmap bm);

    public ImageView getImageView();

    public void setProgress(int value, int max);

    public ProgressBar getProgressBar();

    public void setGifFlag(boolean value);

    public void checkVerified(UserBean user);

    public void setPressesStateVisibility(boolean value);

    public void setVisibility(int visibility);

    public int getVisibility();

    public void setOnClickListener(View.OnClickListener onClickListener);

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener);

    public void setLayoutParams(ViewGroup.LayoutParams layoutParams);
}
