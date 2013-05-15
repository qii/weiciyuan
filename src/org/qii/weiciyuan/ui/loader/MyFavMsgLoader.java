package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.FavListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.fav.FavListDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-15
 */
public class MyFavMsgLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<FavListBean>> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String page;

    public MyFavMsgLoader(Context context, String token, String page) {
        super(context);
        this.token = token;
        this.page = page;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<FavListBean> loadInBackground() {
        FavListDao dao = new FavListDao(token);
        dao.setPage(page);
        FavListBean result = null;
        WeiboException exception = null;
        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } catch (WeiboException e) {
            exception = e;
        } finally {
            lock.unlock();
        }


        AsyncTaskLoaderResult<FavListBean> data = new AsyncTaskLoaderResult<FavListBean>();
        data.data = result;
        data.exception = exception;
        return data;
    }

}

