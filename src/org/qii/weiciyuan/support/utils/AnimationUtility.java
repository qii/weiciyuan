package org.qii.weiciyuan.support.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * User: qii
 * Date: 14-1-24
 */
public class AnimationUtility {

    public static void translateFragmentY(Fragment fragment, int from, int to,
            Animator.AnimatorListener animatorListener) {
        final View fragmentView = fragment.getView();
        if (fragmentView == null) {
            return;
        }
        FragmentViewYWrapper wrapper = new FragmentViewYWrapper(fragmentView);
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(wrapper, "change", from, to);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        if (animatorListener != null) {
            objectAnimator.addListener(animatorListener);
        }
        objectAnimator.start();

    }

    public static void translateFragmentX(Fragment fragment, int from, int to) {
        final View fragmentView = fragment.getView();
        if (fragmentView == null) {
            return;
        }
        FragmentViewXWrapper wrapper = new FragmentViewXWrapper(fragmentView);
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(wrapper, "change", from, to);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();

    }

    private static class FragmentViewYWrapper {

        private View view;

        FragmentViewYWrapper(View view) {
            this.view = view;
        }

        public void setChange(int y) {
            view.scrollTo(0, y);
        }
    }

    private static class FragmentViewXWrapper {

        private View view;

        FragmentViewXWrapper(View view) {
            this.view = view;
        }

        public void setChange(int x) {
            view.scrollTo(x, 0);
        }
    }

    public static Rect getBitmapRectFromImageView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }

        Rect rect = new Rect();
        boolean result = imageView.getGlobalVisibleRect(rect);

        boolean checkWidth = rect.width() < imageView.getWidth();
        boolean checkHeight = rect.height() < imageView.getHeight();

        boolean clipped = !result || checkWidth || checkHeight;

        if (bitmap != null && !clipped) {

            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            int imageViewWidth = imageView.getWidth();
            int imageviewHeight = imageView.getHeight();

            float startScale;
            if ((float) imageViewWidth / bitmapWidth
                    > (float) imageviewHeight / bitmapHeight) {
                // Extend start bounds horizontally
                startScale = (float) imageviewHeight / bitmapHeight;

            } else {
                startScale = (float) imageViewWidth / bitmapWidth;

            }

            bitmapHeight = (int) (bitmapHeight * startScale);
            bitmapWidth = (int) (bitmapWidth * startScale);

            int deltaX = (imageViewWidth - bitmapWidth) / 2;
            int deltaY = (imageviewHeight - bitmapHeight) / 2;

            rect.set(rect.left + deltaX, rect.top + deltaY, rect.right - deltaX,
                    rect.bottom - deltaY);

            return rect;
        } else {
            return null;
        }


    }
}
