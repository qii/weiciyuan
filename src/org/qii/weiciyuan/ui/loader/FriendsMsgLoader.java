package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.maintimeline.BilateralTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.FriendGroupTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainFriendsTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;

/**
 * User: qii
 * Date: 13-4-18
 */
public class FriendsMsgLoader extends AsyncTaskLoader<AsyncTaskLoaderResult<MessageListBean>> {


    private String token;
    private String sinceId;
    private String maxId;
    private String accountId;
    private String currentGroupId;

    public FriendsMsgLoader(Context context, String accountId, String token, String groupId, String sinceId, String maxId) {
        super(context);
        this.token = token;
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.accountId = accountId;
        this.currentGroupId = groupId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public AsyncTaskLoaderResult<MessageListBean> loadInBackground() {
        MainFriendsTimeLineDao dao;
        if (currentGroupId.equals(FriendsTimeLineFragment.BILATERAL_GROUP_ID)) {
            dao = new BilateralTimeLineDao(token);
        } else if (currentGroupId.equals(FriendsTimeLineFragment.ALL_GROUP_ID)) {
            dao = new MainFriendsTimeLineDao(token);
        } else {
            dao = new FriendGroupTimeLineDao(token, currentGroupId);
        }

        dao.setSince_id(sinceId);
        dao.setMax_id(maxId);
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


