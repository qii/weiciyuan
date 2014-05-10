package org.qii.weiciyuan.support.lib;

import org.qii.weiciyuan.support.utils.Utility;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

/**
 * User: qii
 * Date: 14-4-1
 */
public class AnimationRect implements Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(scaledBitmapRect, flags);
        dest.writeFloat(clipRectH);
        dest.writeFloat(clipRectV);
        dest.writeParcelable(imageViewRect, flags);
        dest.writeInt(type);
        dest.writeBooleanArray(new boolean[]{clipped});
        dest.writeBooleanArray(new boolean[]{isScreenPortrait});
        dest.writeFloat(thumbnailWidthHeightRatio);
        dest.writeInt(thumbnailWidth);
        dest.writeInt(thumbnailHeight);
        dest.writeInt(widgetWidth);
        dest.writeInt(widgetHeight);
    }

    public static final Parcelable.Creator<AnimationRect> CREATOR =
            new Parcelable.Creator<AnimationRect>() {
                public AnimationRect createFromParcel(Parcel in) {
                    AnimationRect rect = new AnimationRect();
                    rect.scaledBitmapRect = in.readParcelable(Rect.class.getClassLoader());
                    rect.clipRectH = in.readFloat();
                    rect.clipRectV = in.readFloat();
                    rect.imageViewRect = in.readParcelable(Rect.class.getClassLoader());
                    rect.type = in.readInt();

                    boolean[] booleans = new boolean[1];
                    in.readBooleanArray(booleans);
                    rect.clipped = booleans[0];

                    boolean[] isScreenPortraitArray = new boolean[1];
                    in.readBooleanArray(isScreenPortraitArray);
                    rect.isScreenPortrait = isScreenPortraitArray[0];

                    rect.thumbnailWidthHeightRatio = in.readFloat();
                    rect.thumbnailWidth = in.readInt();
                    rect.thumbnailHeight = in.readInt();

                    rect.widgetWidth = in.readInt();
                    rect.widgetHeight = in.readInt();

                    return rect;
                }

                public AnimationRect[] newArray(int size) {
                    return new AnimationRect[size];
                }
            };


    public static final int TYPE_CLIP_V = 0;

    public static final int TYPE_CLIP_H = 1;

    public static final int TYPE_EXTEND_V = 2;

    public static final int TYPE_EXTEND_H = 3;

    public Rect scaledBitmapRect;

    public float clipRectH;

    public float clipRectV;


    public Rect imageViewRect;

    public int type = -1;

    public boolean clipped;

    public boolean isScreenPortrait;

    public float thumbnailWidthHeightRatio;

    public int thumbnailWidth;

    public int thumbnailHeight;

    public int widgetWidth;

    public int widgetHeight;

    public static AnimationRect buildFromImageView(ImageView imageView) {
        AnimationRect rect = new AnimationRect();

        rect.isScreenPortrait = Utility.isDevicePort();

        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }

        if (bitmap == null) {
            return null;
        }

        rect.imageViewRect = new Rect();

        rect.widgetWidth = imageView.getWidth();

        rect.widgetHeight = imageView.getHeight();

        rect.thumbnailWidthHeightRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();

        rect.thumbnailWidth = bitmap.getWidth();

        rect.thumbnailHeight = bitmap.getHeight();

        boolean result = imageView.getGlobalVisibleRect(rect.imageViewRect);

        boolean checkWidth = rect.imageViewRect.width() < imageView.getWidth();
        boolean checkHeight = rect.imageViewRect.height() < imageView.getHeight();

        rect.clipped = !result || checkWidth || checkHeight;

        ImageView.ScaleType scaledType = imageView.getScaleType();

        Rect scaledBitmapRect = new Rect(rect.imageViewRect);

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        int imageViewWidth = imageView.getWidth();
        int imageViewHeight = imageView.getHeight();

        float startScale;

        int deltaX;

        int deltaY;

        switch (scaledType) {
            case CENTER_CROP:

                if ((float) imageViewWidth / bitmapWidth
                        > (float) imageViewHeight / bitmapHeight) {

                    startScale = (float) imageViewWidth / bitmapWidth;
                    rect.type = TYPE_CLIP_V;

                } else {
                    startScale = (float) imageViewHeight / bitmapHeight;
                    rect.type = TYPE_CLIP_H;
                }

                bitmapHeight = (int) (bitmapHeight * startScale);
                bitmapWidth = (int) (bitmapWidth * startScale);

                deltaX = (imageViewWidth - bitmapWidth) / 2;
                deltaY = (imageViewHeight - bitmapHeight) / 2;

                scaledBitmapRect.set(scaledBitmapRect.left + deltaX, scaledBitmapRect.top + deltaY,
                        scaledBitmapRect.right - deltaX,
                        scaledBitmapRect.bottom - deltaY);

                if (rect.type == TYPE_CLIP_H) {
                    rect.clipRectH = Math.abs((float) deltaX / (float) bitmapWidth);
                } else if (rect.type == TYPE_CLIP_V) {
                    rect.clipRectV = Math.abs((float) deltaY / (float) bitmapHeight);
                }

                break;

            case FIT_CENTER:

                if ((float) imageViewWidth / bitmapWidth
                        > (float) imageViewHeight / bitmapHeight) {
                    // Extend start bounds horizontally
                    startScale = (float) imageViewHeight / bitmapHeight;

                    rect.type = TYPE_EXTEND_V;

                } else {
                    startScale = (float) imageViewWidth / bitmapWidth;
                    rect.type = TYPE_EXTEND_H;
                }

                bitmapHeight = (int) (bitmapHeight * startScale);
                bitmapWidth = (int) (bitmapWidth * startScale);

                deltaX = (imageViewWidth - bitmapWidth) / 2;
                deltaY = (imageViewHeight - bitmapHeight) / 2;

                scaledBitmapRect.set(scaledBitmapRect.left + deltaX, scaledBitmapRect.top + deltaY,
                        scaledBitmapRect.right - deltaX,
                        scaledBitmapRect.bottom - deltaY);

                break;
        }

        rect.scaledBitmapRect = scaledBitmapRect;

        return rect;
    }

}
