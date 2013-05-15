package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.maintimeline.BilateralTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.FriendGroupTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainFriendsTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-4-18
 */
public class FriendsMsgLoader extends AbstractAsyncNetRequestTaskLoader<MessageListBean> {

    private static Lock lock = new ReentrantLock();


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


    public MessageListBean loadData() throws WeiboException {
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

        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } finally {
            lock.unlock();
        }

        return result;
    }

}


