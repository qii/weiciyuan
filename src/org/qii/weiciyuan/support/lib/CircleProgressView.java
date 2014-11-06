package org.qii.weiciyuan.support.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * User: qii
 * Date: 12-12-23
 */
public class CircleProgressView extends View {

    private Paint mPaint = new Paint();

    private int progress = 0;
    private int max = 100;

    private ValueAnimator valueAnimator;

    private boolean isInitValue = true;

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.parseColor("#33B5E5"));
        mPaint.setAntiAlias(true);
        mPaint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int h = Math.min(width, height);
        RectF oval2 = new RectF((width - h) / 2, (height - h) / 2, h + (width - h) / 2,
                h + (height - h) / 2);
        if (getProgress() < 360) {
            mPaint.setColor(Color.parseColor("#33B5E5"));
            canvas.drawArc(oval2, 180, getProgress(), true, mPaint);
        } else {
            mPaint.setColor(Color.TRANSPARENT);
            canvas.drawArc(oval2, 180, 360, true, mPaint);
        }
    }

    private int getProgress() {
        return 360 * progress / max;
    }

    public void setMax(int number) {
        this.max = number;
        invalidate();
    }

    public void setProgress(int progress) {
        if (progress == 0) {
            invalidate();
            return;
        }

        if (progress <= this.progress) {
            this.progress = progress;
            invalidate();
            return;
        }

        if (isInitValue) {
            isInitValue = false;
            this.progress = progress;
            invalidate();
            return;
        }

        int start = this.progress;

        if (valueAnimator != null && valueAnimator.isRunning()) {
            start = (Integer) valueAnimator.getAnimatedValue();
            valueAnimator.cancel();
        }

        valueAnimator = ValueAnimator.ofInt(start, progress);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                CircleProgressView.this.progress = value;
                postInvalidateOnAnimation();
            }
        });
        valueAnimator.start();
    }

    public void executeRunnableAfterAnimationFinish(final Runnable runnable) {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }
}
