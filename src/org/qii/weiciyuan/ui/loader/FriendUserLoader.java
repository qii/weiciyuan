package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.user.FriendListDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-12
 */
public class FriendUserLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<UserListBean>> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String uid;
    private String page;
    private String cursor;


    public FriendUserLoader(Context context, String token, String uid, String cursor) {
        super(context);
        this.token = token;
        this.uid = uid;
        this.cursor = cursor;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<UserListBean> loadInBackground() {
        FriendListDao dao = new FriendListDao(token, uid);
        dao.setCursor(cursor);

        UserListBean result = null;
        WeiboException exception = null;
        lock.lock();

        try {
            result = dao.getGSONMsgList();
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

