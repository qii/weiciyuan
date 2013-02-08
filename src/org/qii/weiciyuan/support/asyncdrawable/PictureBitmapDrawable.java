package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.drawable.ColorDrawable;
import org.qii.weiciyuan.support.debug.DebugColor;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 12-9-5
 */
public class PictureBitmapDrawable extends ColorDrawable {
    private final WeakReference<PictureBitmapWorkerTask> bitmapDownloaderTaskReference;

    public PictureBitmapDrawable(PictureBitmapWorkerTask bitmapDownloaderTask) {
        super(DebugColor.DOWNLOAD_START);
        bitmapDownloaderTaskReference =
                new WeakReference<PictureBitmapWorkerTask>(bitmapDownloaderTask);
    }

    public PictureBitmapWorkerTask getBitmapDownloaderTask() {
        return bitmapDownloaderTaskReference.get();
    }
}
