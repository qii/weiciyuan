package org.qii.weiciyuan.support.lib;

import org.qii.weiciyuan.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * User: qii
 * Date: 14-1-30
 */
public class LinearGradientCoverImageView extends ImageView {

    private LinearGradient linearGradient;
    private Paint paint = new Paint();

    public LinearGradientCoverImageView(Context context) {
        this(context, null);
    }

    public LinearGradientCoverImageView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LinearGradientCoverImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (linearGradient == null) {
            int colorLinear[] = {Color.TRANSPARENT, getResources().getColor(R.color.dark_gray)};
            linearGradient = new LinearGradient(0, 0, 0, getHeight(), colorLinear, null,
                    Shader.TileMode.REPEAT);
            paint.setShader(linearGradient);
        }
        canvas.drawPaint(paint);
    }
}
