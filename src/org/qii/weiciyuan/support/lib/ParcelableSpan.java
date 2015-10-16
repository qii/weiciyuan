package org.qii.weiciyuan.support.lib;

import android.os.Parcelable;

/**
 * {@link android.text.ParcelableSpan} 已经不推荐应用层使用了, 故整个给 app 中用的接口~~
 * Created by ipcjs on 2015/10/14.
 */
public interface ParcelableSpan extends Parcelable {
    int getSpanTypeId();
}