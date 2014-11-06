package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import pl.droidsonroids.gif.GifImageView;

/**
 * User: qii
 * Date: 14-4-1
 */
public class ClipImageView extends GifImageView {

    private float clipHorizontalPercent;
    private float clipVerticalPercent;

    private float clipTopPercent;
    private float clipBottomPercent;
    private float clipLeftPercent;
    private float clipRightPercent;

    private Rect rect;
    private Paint paint;

    public ClipImageView(Context context) {
        this(context, null, -1);
    }

    public ClipImageView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ClipImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
        paint.setColor(Color.GREEN);
    }

    public void setClipHorizontal(float value) {
        if (this.clipHorizontalPercent != value) {
            this.clipHorizontalPercent = value;
            invalidate();
        }
    }

    public void setClipVertical(float value) {
        if (this.clipVerticalPercent != value) {
            this.clipVerticalPercent = value;
            invalidate();
        }
    }

    public void setClipTop(float value) {
        if (this.clipTopPercent != value) {
            this.clipTopPercent = value;
            invalidate();
        }
    }

    public void setClipBottom(float value) {
        if (this.clipBottomPercent != value) {
            this.clipBottomPercent = value;
            invalidate();
        }
    }

    public void setClipLeft(float value) {
        if (this.clipLeftPercent != value) {
            this.clipLeftPercent = value;
            invalidate();
        }
    }

    public void setClipRight(float value) {
        if (this.clipRightPercent != value) {
            this.clipRightPercent = value;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect = new Rect(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null || (
                clipVerticalPercent == 0
                        && clipHorizontalPercent == 0
                        && clipTopPercent == 0
                        && clipBottomPercent == 0
                        && clipLeftPercent == 0
                        && clipRightPercent == 0)) {
            super.draw(canvas);
            return;
        }

        if (!(drawable instanceof BitmapDrawable)) {
            super.draw(canvas);
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        if (bitmap == null) {
            super.draw(canvas);
            return;
        }

        Rect clipRect = new Rect(rect);
        int width = clipRect.width();
        int height = clipRect.height();

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        int imageViewWidth = width;
        int imageViewHeight = height;

        float startScale;

        int deltaX;

        int deltaY;

        if ((float) imageViewWidth / bitmapWidth
                > (float) imageViewHeight / bitmapHeight) {
            startScale = (float) imageViewHeight / bitmapHeight;
        } else {
            startScale = (float) imageViewWidth / bitmapWidth;
        }

        bitmapHeight = (int) (bitmapHeight * startScale);
        bitmapWidth = (int) (bitmapWidth * startScale);

        deltaX = (imageViewWidth - bitmapWidth) / 2;
        deltaY = (imageViewHeight - bitmapHeight) / 2;

        canvas.save();

        int clipV = (int) (this.clipVerticalPercent * bitmapHeight) + deltaY;
        int clipH = (int) (this.clipHorizontalPercent * bitmapWidth) + deltaX;

        int clipTop = (int) (this.clipTopPercent * bitmapHeight);
        int clipBottom = (int) (this.clipBottomPercent * bitmapHeight);

        int clipLeft = (int) (this.clipLeftPercent * bitmapWidth);
        int clipRight = (int) (this.clipRightPercent * bitmapWidth);

        clipRect.set(clipRect.left + clipH + clipLeft,
                clipRect.top + clipV + clipTop,
                clipRect.right - clipH - clipRight,
                clipRect.bottom - clipV - clipBottom);
        canvas.clipRect(clipRect);
        super.draw(canvas);
        canvas.restore();
    }
}
