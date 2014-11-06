package org.qii.weiciyuan.bean.android;

import org.qii.weiciyuan.support.error.WeiboException;

import android.os.Bundle;

/**
 * User: qii
 * Date: 13-4-16
 */
public class AsyncTaskLoaderResult<E> {
    public E data;
    public WeiboException exception;
    public Bundle args;
}
