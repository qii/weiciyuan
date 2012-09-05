package org.qii.weiciyuan.support.lib;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 12-9-5
 */
public class PictureBitmapDrawable extends ColorDrawable {
    private final WeakReference<PictureBitmapWorkerTask> bitmapDownloaderTaskReference;

    public PictureBitmapDrawable(PictureBitmapWorkerTask bitmapDownloaderTask) {
        super(Color.BLACK);
        bitmapDownloaderTaskReference =
                new WeakReference<PictureBitmapWorkerTask>(bitmapDownloaderTask);
    }

    public PictureBitmapWorkerTask getBitmapDownloaderTask() {
        return bitmapDownloaderTaskReference.get();
    }
}
