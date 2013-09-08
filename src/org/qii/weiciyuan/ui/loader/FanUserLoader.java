package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.user.FanListDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-12
 */
public class FanUserLoader extends AbstractAsyncNetRequestTaskLoader<UserListBean> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String uid;
    private String cursor;


    public FanUserLoader(Context context, String token, String uid, String cursor) {
        super(context);
        this.token = token;
        this.uid = uid;
        this.cursor = cursor;
    }


    public UserListBean loadData() throws WeiboException {
        FanListDao dao = new FanListDao(token, uid);
        dao.setCursor(cursor);

        UserListBean result = null;
        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } finally {
            lock.unlock();
        }


        return result;
    }

}

