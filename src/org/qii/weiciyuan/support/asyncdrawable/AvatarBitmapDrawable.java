package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 12-9-5
 */
public class AvatarBitmapDrawable extends ColorDrawable {
    private final WeakReference<AvatarBitmapWorkerTask> bitmapDownloaderTaskReference;

    public AvatarBitmapDrawable(AvatarBitmapWorkerTask bitmapDownloaderTask) {
        super(Color.TRANSPARENT);
        bitmapDownloaderTaskReference =
                new WeakReference<AvatarBitmapWorkerTask>(bitmapDownloaderTask);
    }

    public AvatarBitmapWorkerTask getBitmapDownloaderTask() {
        return bitmapDownloaderTaskReference.get();
    }
}
