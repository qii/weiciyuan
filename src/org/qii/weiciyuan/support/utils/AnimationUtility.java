package org.qii.weiciyuan.support.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

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


}
