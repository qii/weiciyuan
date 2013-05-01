package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.maintimeline.MainMentionsTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-4-14
 */
public class MentionsWeiboMsgLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<MessageListBean>> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String sinceId;
    private String maxId;
    private String accountId;

    public MentionsWeiboMsgLoader(Context context, String accountId, String token, String sinceId, String maxId) {
        super(context);
        this.token = token;
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.accountId = accountId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<MessageListBean> loadInBackground() {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
        dao.setSince_id(sinceId);
        dao.setMax_id(maxId);
        MessageListBean result = null;
        WeiboException exception = null;
        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } catch (WeiboException e) {
            exception = e;
        } finally {
            lock.unlock();
        }


        AsyncTaskLoaderResult<MessageListBean> data = new AsyncTaskLoaderResult<MessageListBean>();
        data.data = result;
        data.exception = exception;
        return data;
    }

}

