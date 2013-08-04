package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.drawable.ColorDrawable;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.ThemeUtility;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 12-9-5
 */
public class PictureBitmapDrawable extends ColorDrawable {
    private final WeakReference<IPictureWorker> bitmapDownloaderTaskReference;

    public PictureBitmapDrawable(IPictureWorker bitmapDownloaderTask) {
        super(ThemeUtility.getColor(R.attr.listview_pic_bg));
        bitmapDownloaderTaskReference =
                new WeakReference<IPictureWorker>(bitmapDownloaderTask);
    }

    public IPictureWorker getBitmapDownloaderTask() {
        return bitmapDownloaderTaskReference.get();
    }
}
