package org.qii.weiciyuan.support.lib;

import org.qii.weiciyuan.support.utils.Utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import java.util.Objects;

/**
 * User: qii
 * Date: 14-6-3
 */
public class LayerEnablingAnimatorListener extends AnimatorListenerAdapter {

    private final View mTargetView;
    private int mLayerType;
    private Animator.AnimatorListener mAdapter;

    public LayerEnablingAnimatorListener(View targetView, Animator.AnimatorListener adapter) {
        if (Utility.isKK()) {
            mTargetView = Objects.requireNonNull(targetView, "Target view cannot be null");
        } else {
            mTargetView = targetView;
        }

        this.mAdapter = adapter;
    }

    public View getTargetView() {
        return mTargetView;
    }

    @Override
    public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);
        if (mAdapter != null) {
            mAdapter.onAnimationStart(animation);
        }
        mLayerType = mTargetView.getLayerType();
        mTargetView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        AppLogger.d("View animation is started, enable hardware accelerated");
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        if (mAdapter != null) {
            mAdapter.onAnimationEnd(animation);
        }
        mTargetView.setLayerType(mLayerType, null);
//        AppLogger.d("View animation is finished, disable hardware accelerated");
    }
}
