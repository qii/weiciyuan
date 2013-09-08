package org.qii.weiciyuan.bean.android;

import android.os.Bundle;
import org.qii.weiciyuan.support.error.WeiboException;

/**
 * User: qii
 * Date: 13-4-16
 */
public class AsyncTaskLoaderResult<E> {
    public E data;
    public WeiboException exception;
    public Bundle args;
}
