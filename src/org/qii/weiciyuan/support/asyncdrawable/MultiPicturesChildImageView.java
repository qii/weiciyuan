package org.qii.weiciyuan.support.asyncdrawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.PerformanceImageView;

/**
 * User: qii
 * Date: 13-9-4
 */
public class MultiPicturesChildImageView extends PerformanceImageView {

    private Paint paint = new Paint();
    private boolean pressed = false;


    public MultiPicturesChildImageView(Context context) {
        super(context);
    }

    public MultiPicturesChildImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiPicturesChildImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bitmap;
        if (pressed) {
            canvas.drawColor(getResources().getColor(R.color.transparent_cover));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressed = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pressed = false;
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

}
