package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.error.WeiboException;

/**
 * User: qii
 * Date: 13-5-15
 */
public abstract class AbstractAsyncNetRequestTaskLoader<T> extends AsyncTaskLoader<AsyncTaskLoaderResult<T>> {

    private AsyncTaskLoaderResult<T> result;

    public AbstractAsyncNetRequestTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (result == null) {
            forceLoad();
        } else {
            deliverResult(result);
        }
    }

    @Override
    public AsyncTaskLoaderResult<T> loadInBackground() {

        T data = null;
        WeiboException exception = null;


        try {
            data = loadData();
        } catch (WeiboException e) {
            exception = e;
        }

        result = new AsyncTaskLoaderResult<T>();
        result.data = data;
        result.exception = exception;

        return result;
    }

    protected abstract T loadData() throws WeiboException;

}
