package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import org.qii.weiciyuan.bean.ShareListBean;
import org.qii.weiciyuan.dao.shorturl.ShareShortUrlTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-15
 */
public class BrowserShareMsgLoader extends AbstractAsyncNetRequestTaskLoader<ShareListBean> {

    private static Lock lock = new ReentrantLock();


    private String token;
    private String maxId;
    private String url;


    public BrowserShareMsgLoader(Context context, String token, String url, String maxId) {
        super(context);
        this.token = token;
        this.maxId = maxId;
        this.url = url;

    }

    public ShareListBean loadData() throws WeiboException {
        ShareShortUrlTimeLineDao dao = new ShareShortUrlTimeLineDao(token, url);
        dao.setMaxId(maxId);
        ShareListBean result = null;

        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } finally {
            lock.unlock();
        }

        return result;
    }

}



