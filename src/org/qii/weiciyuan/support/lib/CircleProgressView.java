package org.qii.weiciyuan.support.lib;

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

    public CircleProgressView(Context context) {
        super(context);
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
        RectF oval2 = new RectF((width - h) / 2, (height - h) / 2, h + (width - h) / 2, h + (height - h) / 2);
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
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }
}
