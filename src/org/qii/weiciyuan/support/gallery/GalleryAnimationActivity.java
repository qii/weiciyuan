package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.utils.AnimationUtility;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * User: qii
 * Date: 14-3-21
 */
public class GalleryAnimationActivity extends Activity {

    private static final int ANIMATION_DURATION = 300;

    private ColorDrawable backgroundColor;

    private static final int IMAGEVIEW_SOFT_LAYER_MAX_WIDTH = 2000;

    private static final int IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT = 3000;

    private Rect rect;

    private ArrayList<String> urls = new ArrayList<String>();

    private PhotoView animation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity_animation_layout);

        rect = getIntent().getParcelableExtra("rect");
        MessageBean msg = getIntent().getParcelableExtra("msg");
        ArrayList<String> tmp = msg.getThumbnailPicUrls();
        for (int i = 0; i < tmp.size(); i++) {
            urls.add(tmp.get(i).replace("thumbnail", "large"));
        }

        animation = (PhotoView) findViewById(R.id.animation);
        View background = findViewById(R.id.background);
        backgroundColor = new ColorDrawable(Color.BLACK);
        background.setBackground(backgroundColor);

        String path = FileManager.getFilePathFromUrl(urls.get(0), FileLocationMethod.picture_large);

        Bitmap bitmap = ImageUtility
                .decodeBitmapFromSDCard(path, IMAGEVIEW_SOFT_LAYER_MAX_WIDTH,
                        IMAGEVIEW_SOFT_LAYER_MAX_HEIGHT);

        animation.setImageBitmap(bitmap);
        animation.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {

                        final Rect startBounds = new Rect(rect);
                        final Rect finalBounds = AnimationUtility
                                .getBitmapRectFromImageView(animation);

                        if (rect == null || finalBounds == null) {
                            return true;
                        }

                        float startScale = (float) finalBounds.width() / startBounds.width();

                        if (startScale * startBounds.height() > finalBounds.height()) {
                            startScale = (float) finalBounds.height() / startBounds.height();
                        }

                        int deltaTop = startBounds.top - finalBounds.top;
                        int deltaLeft = startBounds.left - finalBounds.left;

                        AppLogger.e("deltaTop=" + deltaTop + ",deltaLeft=" + deltaLeft
                                + ",scale=" + startScale);

                        animation.setPivotY((animation.getHeight() - finalBounds.height()) / 2);
                        animation.setPivotX((animation.getWidth() - finalBounds.width()) / 2);

                        animation.setScaleX(1 / startScale);
                        animation.setScaleY(1 / startScale);

                        animation.setTranslationX(deltaLeft);
                        animation.setTranslationY(deltaTop);

                        animation.animate().translationY(0).translationX(0)
                                .scaleY(1)
                                .scaleX(1).setDuration(ANIMATION_DURATION)
                                .setInterpolator(new AccelerateDecelerateInterpolator());

                        ObjectAnimator bgAnim = ObjectAnimator
                                .ofInt(backgroundColor, "alpha", 0, 255);
                        bgAnim.setDuration(ANIMATION_DURATION);
                        bgAnim.start();
                        animation.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
    }

    private void animateClose(PhotoView imageView) {

        final Rect startBounds = rect;
        final Rect finalBounds = AnimationUtility.getBitmapRectFromImageView(animation);

        if (rect == null || finalBounds == null) {
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

        int deltaTop = startBounds.top - finalBounds.top;
        int deltaLeft = startBounds.left - finalBounds.left;

        animation.setPivotY((animation.getHeight() - finalBounds.height()) / 2);
        animation.setPivotX((animation.getWidth() - finalBounds.width()) / 2);

        animation.animate().translationX(deltaLeft).translationY(deltaTop).scaleY(startScaleFinal)
                .scaleX(startScaleFinal).setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        GalleryAnimationActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                });
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(backgroundColor, "alpha", 0);
        bgAnim.setDuration(ANIMATION_DURATION);
        bgAnim.start();
    }

    @Override
    public void onBackPressed() {

        if (Math.abs(animation.getScale() - 1.0f) > 0.1f) {
            animation.setScale(1, true);
            return;
        }

        overridePendingTransition(0, 0);
        animateClose(null);

    }


}
