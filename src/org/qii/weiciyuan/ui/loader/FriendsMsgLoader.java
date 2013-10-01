package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.text.TextUtils;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.maintimeline.BilateralTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.FriendGroupTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainFriendsTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;
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

    private final int MAX_RETRY_COUNT = 6;  //1*50+6*49=344 new messages count

    public FriendsMsgLoader(Context context, String accountId, String token, String groupId, String sinceId, String maxId) {
        super(context);
        this.token = token;
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.accountId = accountId;
        this.currentGroupId = groupId;
    }


    public MessageListBean loadData() throws WeiboException {
        MessageListBean result = null;
        MessageListBean tmp = get(token, currentGroupId, sinceId, maxId);
        result = tmp;
        if (isLoadNewData() && Utility.isWifi(getContext()) && SettingUtility.isWifiUnlimitedMsgCount()) {
            int retryCount = 0;
            while (tmp.getReceivedCount() >= Integer.valueOf(SettingUtility.getMsgCount()) && retryCount < MAX_RETRY_COUNT) {
                String tmpMaxId = tmp.getItemList().get(tmp.getItemList().size() - 1).getId();
                tmp = get(token, currentGroupId, sinceId, tmpMaxId);
                result.addOldData(tmp);
                retryCount++;
            }
            if (tmp.getReceivedCount() >= Integer.valueOf(SettingUtility.getMsgCount())) {
                result.getItemList().add(null);
            }
        } else {
            return result;
        }

        return result;
    }

    private boolean isLoadNewData() {
        return !TextUtils.isEmpty(sinceId) && TextUtils.isEmpty(maxId);
    }

    private MessageListBean get(String token, String groupId, String sinceId, String maxId) throws WeiboException {
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


