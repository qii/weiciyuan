package org.qii.weiciyuan.support.utils;

import org.qii.weiciyuan.support.lib.LayerEnablingAnimatorListener;
import org.qii.weiciyuan.ui.userinfo.UserInfoFragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.lang.reflect.Method;

/**
 * User: qii
 * Date: 14-1-24
 */
public class AnimationUtility {

    public static void translateFragmentY(UserInfoFragment fragment, int from, int to,
            Animator.AnimatorListener animatorListener) {
        final View fragmentView = fragment.getView();
        if (fragmentView == null) {
            return;
        }

        View view = fragment.header;

        FragmentViewYWrapper wrapper = new FragmentViewYWrapper(fragmentView);
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(wrapper, "change", from, to);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        if (animatorListener != null) {
            objectAnimator.addListener(new LayerEnablingAnimatorListener(view, animatorListener));
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

    public static void forceConvertActivityFromTranslucent(Activity activity) {
        try {
            Method method = Activity.class.getDeclaredMethod("convertFromTranslucent", null);
            method.setAccessible(true);
            method.invoke(activity, null);
        } catch (Throwable ignored) {
        }
    }

    public static void forceConvertActivityToTranslucent(Activity activity) {

        if (Utility.isL()) {
            try {
                //                Class listener = Class
                //                        .forName("android.app.Activity$TranslucentConversionListener");

                Method[] methods = Activity.class.getDeclaredMethods();
                Method requireMethod = null;
                for (Method method : methods) {
                    if (method.getName().equals("convertToTranslucent")) {
                        requireMethod = method;
                    }
                }

                if (requireMethod != null) {
                    requireMethod.setAccessible(true);
                    requireMethod.invoke(activity, new Object[]{null, null});
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {

            try {
                Class<?>[] declaredClasses = Activity.class.getDeclaredClasses();
                Class<?> listener = null;
                for (Class clazz : declaredClasses) {
                    if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                        listener = clazz;
                    }
                }
                Method method = Activity.class.getDeclaredMethod("convertToTranslucent",
                        listener);
                method.setAccessible(true);
                method.invoke(activity, new Object[]{null});
            } catch (Throwable ignored) {
                ignored.printStackTrace();
            }
        }
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
        boolean isVisible = imageView.getGlobalVisibleRect(rect);
        if (!isVisible) {
            int[] location = new int[2];
            imageView.getLocationOnScreen(location);

            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + imageView.getWidth();
            rect.bottom = rect.top + imageView.getHeight();
        }

        if (bitmap != null) {

            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            int imageViewWidth = imageView.getWidth() - imageView.getPaddingLeft() - imageView
                    .getPaddingRight();
            int imageviewHeight = imageView.getHeight() - imageView.getPaddingTop() - imageView
                    .getPaddingBottom();

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

    public static View getAppContentView(Activity activity) {
        final View appView = activity.findViewById(android.R.id.content);
        return appView;
    }
}
