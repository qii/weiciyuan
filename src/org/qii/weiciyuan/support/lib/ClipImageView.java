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
        this.clipHorizontalPercent = value;
        invalidate();
    }


    public void setClipVertical(float value) {
        this.clipVerticalPercent = value;
        invalidate();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect = new Rect(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null || (clipVerticalPercent == 0 && clipHorizontalPercent == 0)) {
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

        clipRect.set(clipRect.left + clipH,
                clipRect.top + clipV,
                clipRect.right - clipH,
                clipRect.bottom - clipV);
        canvas.clipRect(clipRect);
        super.draw(canvas);
        canvas.restore();


    }

}
