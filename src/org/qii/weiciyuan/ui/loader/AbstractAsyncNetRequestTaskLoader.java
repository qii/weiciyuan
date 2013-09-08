package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.error.WeiboException;

/**
 * User: qii
 * Date: 13-5-15
 */
public abstract class AbstractAsyncNetRequestTaskLoader<T> extends AsyncTaskLoader<AsyncTaskLoaderResult<T>> {

    private AsyncTaskLoaderResult<T> result;
    private Bundle args;

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
        result.args = this.args;

        return result;
    }

    protected abstract T loadData() throws WeiboException;

    public void setArgs(Bundle args) {
        if (result != null) {
            throw new IllegalArgumentException("can't setArgs after loader executes");
        }
        this.args = args;
    }

}
