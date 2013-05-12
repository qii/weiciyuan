package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.search.SearchDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-12
 */
public class SearchUserLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<UserListBean>> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String searchWord;
    private String page;

    public SearchUserLoader(Context context, String token, String searchWord, String page) {
        super(context);
        this.token = token;
        this.searchWord = searchWord;
        this.page = page;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<UserListBean> loadInBackground() {
        SearchDao dao = new SearchDao(token, searchWord);
        dao.setPage(page);

        UserListBean result = null;
        WeiboException exception = null;
        lock.lock();

        try {
            result = dao.getUserList();
        } catch (WeiboException e) {
            exception = e;
        } finally {
            lock.unlock();
        }


        AsyncTaskLoaderResult<UserListBean> data = new AsyncTaskLoaderResult<UserListBean>();
        data.data = result;
        data.exception = exception;
        return data;
    }

}
