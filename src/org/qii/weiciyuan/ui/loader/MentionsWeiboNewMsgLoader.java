package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.maintimeline.MainMentionsTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;

/**
 * User: qii
 * Date: 13-4-14
 */
public class MentionsWeiboNewMsgLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<MessageListBean>> {

    private String token;
    private String id;
    private String accountId;

    public MentionsWeiboNewMsgLoader(Context context, String accountId, String token, String id) {
        super(context);
        this.token = token;
        this.id = id;
        this.accountId = accountId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<MessageListBean> loadInBackground() {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
        dao.setSince_id(id);
        MessageListBean result = null;
        WeiboException exception = null;

        try {
            result = dao.getGSONMsgList();
        } catch (WeiboException e) {
            exception = e;
        }

        AsyncTaskLoaderResult<MessageListBean> data = new AsyncTaskLoaderResult<MessageListBean>();
        data.data = result;
        data.exception = exception;
        return data;
    }

}
