package org.qii.weiciyuan.support.lib;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 12-9-5
 */
public class AvatarBitmapDrawable extends ColorDrawable {
    private final WeakReference<AvatarBitmapWorkerTask> bitmapDownloaderTaskReference;

    public AvatarBitmapDrawable(AvatarBitmapWorkerTask bitmapDownloaderTask) {
        super(Color.BLACK);
        bitmapDownloaderTaskReference =
                new WeakReference<AvatarBitmapWorkerTask>(bitmapDownloaderTask);
    }

    public AvatarBitmapWorkerTask getBitmapDownloaderTask() {
        return bitmapDownloaderTaskReference.get();
    }
}
