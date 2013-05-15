package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.RepostListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.timeline.RepostsTimeLineByIdDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-15
 */
public class RepostByIdMsgLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<RepostListBean>> {

    private static Lock lock = new ReentrantLock();


    private String token;
    private String sinceId;
    private String maxId;
    private String id;

    public RepostByIdMsgLoader(Context context, String id, String token, String sinceId, String maxId) {
        super(context);
        this.token = token;
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.id = id;

    }


    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<RepostListBean> loadInBackground() {
        RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);


        dao.setSince_id(sinceId);
        dao.setMax_id(maxId);
        RepostListBean result = null;
        WeiboException exception = null;

        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } catch (WeiboException e) {
            exception = e;
        } finally {
            lock.unlock();
        }

        AsyncTaskLoaderResult<RepostListBean> data = new AsyncTaskLoaderResult<RepostListBean>();
        data.data = result;
        data.exception = exception;
        return data;
    }

}


