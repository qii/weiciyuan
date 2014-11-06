package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.AnimationRect;
import org.qii.weiciyuan.support.lib.ClipImageView;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AnimationUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * User: qii
 * Date: 14-4-30
 */
public class GifPictureFragment extends Fragment {

    private static final int NAVIGATION_BAR_HEIGHT_DP_UNIT = 48;
    private static final int ANIMATION_DURATION = 300;

    private static final int IMAGEVIEW_SOFT_LAYER_MAX_WIDTH = 2000;
    private static final int IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT = 3000;

    private PhotoView gifImageView;

    public static GifPictureFragment newInstance(String path, AnimationRect rect,
            boolean animationIn) {
        GifPictureFragment fragment = new GifPictureFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putParcelable("rect", rect);
        bundle.putBoolean("animationIn", animationIn);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_gif_layout, container, false);

        gifImageView = (PhotoView) view.findViewById(R.id.gif);

        if (SettingUtility.allowClickToCloseGallery()) {
            gifImageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    getActivity().onBackPressed();
                }
            });
        }

        LongClickListener longClickListener = ((ContainerFragment) getParentFragment())
                .getLongClickListener();
        gifImageView.setOnLongClickListener(longClickListener);

        String path = getArguments().getString("path");
        boolean animateIn = getArguments().getBoolean("animationIn");
        final AnimationRect rect = getArguments().getParcelable("rect");

        File gifFile = new File(path);
        try {
            GifDrawable gifFromFile = new GifDrawable(gifFile);
            gifImageView.setImageDrawable(gifFromFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final ClipImageView photoView = (ClipImageView) view.findViewById(R.id.cover);

        Bitmap bitmap = ImageUtility
                .decodeBitmapFromSDCard(path, IMAGEVIEW_SOFT_LAYER_MAX_WIDTH,
                        IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT);

        photoView.setImageBitmap(bitmap);

        if (!animateIn) {
            photoView.setVisibility(View.INVISIBLE);
            return view;
        }

        gifImageView.setVisibility(View.INVISIBLE);

        final Runnable endAction = new Runnable() {
            @Override
            public void run() {
                Bundle bundle = getArguments();
                bundle.putBoolean("animationIn", false);
                photoView.setVisibility(View.INVISIBLE);
                gifImageView.setVisibility(View.VISIBLE);
            }
        };

        photoView.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {

                        if (rect == null) {
                            photoView.getViewTreeObserver().removeOnPreDrawListener(this);
                            endAction.run();
                            return true;
                        }

                        final Rect startBounds = new Rect(rect.scaledBitmapRect);
                        final Rect finalBounds = AnimationUtility
                                .getBitmapRectFromImageView(photoView);

                        if (finalBounds == null) {
                            photoView.getViewTreeObserver().removeOnPreDrawListener(this);
                            endAction.run();
                            return true;
                        }

                        float startScale = (float) finalBounds.width() / startBounds.width();

                        if (startScale * startBounds.height() > finalBounds.height()) {
                            startScale = (float) finalBounds.height() / startBounds.height();
                        }

                        int oriBitmapScaledWidth = (int) (finalBounds.width() / startScale);
                        int oriBitmapScaledHeight = (int) (finalBounds.height() / startScale);

                        int thumbnailAndOriDeltaRightSize = Math
                                .abs(rect.scaledBitmapRect.width() - oriBitmapScaledWidth);
                        int thumbnailAndOriDeltaBottomSize = Math
                                .abs(rect.scaledBitmapRect.height() - oriBitmapScaledHeight);

                        float thumbnailAndOriDeltaWidth =
                                (float) thumbnailAndOriDeltaRightSize
                                        / (float) oriBitmapScaledWidth;
                        float thumbnailAndOriDeltaHeight =
                                (float) thumbnailAndOriDeltaBottomSize
                                        / (float) oriBitmapScaledHeight;

                        int deltaTop = startBounds.top - finalBounds.top;
                        int deltaLeft = startBounds.left - finalBounds.left;

                        photoView.setPivotY(
                                (photoView.getHeight() - finalBounds.height()) / 2);
                        photoView.setPivotX((photoView.getWidth() - finalBounds.width()) / 2);

                        photoView.setScaleX(1 / startScale);
                        photoView.setScaleY(1 / startScale);

                        photoView.setTranslationX(deltaLeft);
                        photoView.setTranslationY(deltaTop);

                        photoView.animate().translationY(0).translationX(0)
                                .scaleY(1)
                                .scaleX(1).setDuration(ANIMATION_DURATION)
                                .setInterpolator(
                                        new AccelerateDecelerateInterpolator())
                                .withEndAction(endAction);

                        if (rect.type == AnimationRect.TYPE_EXTEND_V
                                || rect.type == AnimationRect.TYPE_EXTEND_H) {

                            AnimatorSet animationSet = new AnimatorSet();
                            animationSet.setDuration(ANIMATION_DURATION);
                            animationSet
                                    .setInterpolator(new AccelerateDecelerateInterpolator());

                            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                                    "clipBottom", thumbnailAndOriDeltaHeight, 0));
                            animationSet.start();
                        } else {

                            AnimatorSet animationSet = new AnimatorSet();
                            animationSet.setDuration(ANIMATION_DURATION);
                            animationSet
                                    .setInterpolator(new AccelerateDecelerateInterpolator());

                            float clipRectH =
                                    ((float) (oriBitmapScaledWidth
                                            - oriBitmapScaledWidth * thumbnailAndOriDeltaWidth
                                            - rect.widgetWidth)
                                            / 2) / (float) oriBitmapScaledWidth;
                            float clipRectV = ((float) (oriBitmapScaledHeight
                                    - oriBitmapScaledHeight * thumbnailAndOriDeltaHeight
                                    - rect.widgetHeight) / 2) / (float) oriBitmapScaledHeight;

                            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                                    "clipHorizontal", clipRectH, 0));
                            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                                    "clipVertical", clipRectV, 0));

                            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                                    "clipBottom", thumbnailAndOriDeltaHeight, 0));
                            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                                    "clipRight", thumbnailAndOriDeltaWidth, 0));

                            animationSet.start();
                        }

                        photoView.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });

        return view;
    }

    public void animationExit(ObjectAnimator backgroundAnimator) {

        if (Math.abs(gifImageView.getScale() - 1.0f) > 0.1f) {
            gifImageView.setScale(1, true);
            return;
        }

        getActivity().overridePendingTransition(0, 0);
        animateClose(backgroundAnimator);
    }

    private void animateClose(ObjectAnimator backgroundAnimator) {

        gifImageView.setVisibility(View.INVISIBLE);

        final ClipImageView photoView = (ClipImageView) getView().findViewById(R.id.cover);

        photoView.setVisibility(View.VISIBLE);

        AnimationRect rect = getArguments().getParcelable("rect");

        if (rect == null) {
            photoView.animate().alpha(0);
            backgroundAnimator.start();
            return;
        }

        final Rect startBounds = rect.scaledBitmapRect;
        final Rect finalBounds = AnimationUtility.getBitmapRectFromImageView(photoView);

        if (finalBounds == null) {
            photoView.animate().alpha(0);
            backgroundAnimator.start();
            return;
        }

        if (Utility.isDevicePort() != rect.isScreenPortrait) {
            photoView.animate().alpha(0);
            backgroundAnimator.start();
            return;
        }

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            startScale = (float) startBounds.height() / finalBounds.height();
        } else {
            startScale = (float) startBounds.width() / finalBounds.width();
        }

        final float startScaleFinal = startScale;

        int oriBitmapScaledWidth = (int) (finalBounds.width() * startScale);
        int oriBitmapScaledHeight = (int) (finalBounds.height() * startScale);

        //sina server may cut thumbnail's right or bottom
        int thumbnailAndOriDeltaRightSize = Math
                .abs(rect.scaledBitmapRect.width() - oriBitmapScaledWidth);
        int thumbnailAndOriDeltaBottomSize = Math
                .abs(rect.scaledBitmapRect.height() - oriBitmapScaledHeight);

        float serverClipThumbnailRightSizePercent = (float) thumbnailAndOriDeltaRightSize
                / (float) oriBitmapScaledWidth;
        float serverClipThumbnailBottomSizePercent = (float) thumbnailAndOriDeltaBottomSize
                / (float) oriBitmapScaledHeight;

        int deltaTop = startBounds.top - finalBounds.top;
        int deltaLeft = startBounds.left - finalBounds.left;

        photoView.setPivotY((photoView.getHeight() - finalBounds.height()) / 2);
        photoView.setPivotX((photoView.getWidth() - finalBounds.width()) / 2);

        photoView.animate().translationX(deltaLeft).translationY(deltaTop)
                .scaleY(startScaleFinal)
                .scaleX(startScaleFinal).setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {

                        photoView.animate().alpha(0.0f).setDuration(200).withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                    }
                });

        if (rect.type == AnimationRect.TYPE_EXTEND_V
                || rect.type == AnimationRect.TYPE_EXTEND_H) {
            AnimatorSet animationSet = new AnimatorSet();
            animationSet.setDuration(ANIMATION_DURATION);
            animationSet.setInterpolator(new AccelerateDecelerateInterpolator());

            animationSet.playTogether(backgroundAnimator);
            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                    "clipBottom", 0, serverClipThumbnailBottomSizePercent));
            animationSet.start();
        } else {

            AnimatorSet animationSet = new AnimatorSet();
            animationSet.setDuration(ANIMATION_DURATION);
            animationSet.setInterpolator(new AccelerateDecelerateInterpolator());

            animationSet.playTogether(backgroundAnimator);

            float clipRectH =
                    ((float) (oriBitmapScaledWidth
                            - oriBitmapScaledWidth * serverClipThumbnailRightSizePercent
                            - rect.widgetWidth)
                            / 2) / (float) oriBitmapScaledWidth;
            float clipRectV =
                    ((float) (oriBitmapScaledHeight
                            - oriBitmapScaledHeight * serverClipThumbnailBottomSizePercent
                            - rect.widgetHeight) / 2) / (float) oriBitmapScaledHeight;

            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                    "clipHorizontal", 0, clipRectH));
            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                    "clipVertical", 0, clipRectV));

            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                    "clipBottom", 0, serverClipThumbnailBottomSizePercent));
            animationSet.playTogether(ObjectAnimator.ofFloat(photoView,
                    "clipRight", 0, serverClipThumbnailRightSizePercent));

            animationSet.start();
        }
    }
}
