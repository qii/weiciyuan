package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.DMUserListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.dm.DMDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-15
 */
public class DMUserLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<DMUserListBean>> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String cursor;

    public DMUserLoader(Context context, String token, String cursor) {
        super(context);
        this.token = token;
        this.cursor = cursor;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<DMUserListBean> loadInBackground() {
        DMDao dao = new DMDao(token);
        dao.setCursor(cursor);

        DMUserListBean result = null;
        WeiboException exception = null;
        lock.lock();

        try {
            result = dao.getUserList();
        } catch (WeiboException e) {
            exception = e;
        } finally {
            lock.unlock();
        }

        AsyncTaskLoaderResult<DMUserListBean> data = new AsyncTaskLoaderResult<DMUserListBean>();
        data.data = result;
        data.exception = exception;
        return data;
    }

}

