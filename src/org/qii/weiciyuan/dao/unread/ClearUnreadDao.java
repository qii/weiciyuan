package org.qii.weiciyuan.dao.unread;

import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-9-26
 */
public class ClearUnreadDao {

    public static final String STATUS = "app_message";
    public static final String FOLLOWER = "follower";
    public static final String CMT = "cmt";
    public static final String DM = "dm";
    public static final String MENTION_STATUS = "mention_status";
    public static final String MENTION_CMT = "mention_cmt";

    protected String getUrl() {
        return URLHelper.UNREAD_CLEAR;
    }


    public boolean clearUnread() throws WeiboException {

        String url = getUrl();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("type", type);


        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject.optBoolean("result", false);
        } catch (JSONException e) {
            AppLogger.e(e.getMessage());
        }

        return false;
    }

    /**
     * first check server unread status,if unread count is the same,reset unread count
     */
    public boolean clearMentionStatusUnread(UnreadBean unreadBean, String accountId) throws WeiboException {
        int count = unreadBean.getMention_status();
        UnreadBean currentCount = new UnreadDao(access_token, accountId).getCount();
        if (currentCount == null) {
            return false;
        }
        //already reset or have new unread message
        if (count != currentCount.getMention_status()) {
            return false;
        }
        return new ClearUnreadDao(access_token, ClearUnreadDao.MENTION_STATUS).clearUnread();
    }

    public boolean clearMentionCommentUnread(UnreadBean unreadBean, String accountId) throws WeiboException {
        int count = unreadBean.getMention_cmt();
        UnreadBean currentCount = new UnreadDao(access_token, accountId).getCount();
        if (currentCount == null) {
            return false;
        }
        //already reset or have new unread message
        if (count != currentCount.getMention_cmt()) {
            return false;
        }
        return new ClearUnreadDao(access_token, ClearUnreadDao.MENTION_CMT).clearUnread();
    }

    public boolean clearCommentUnread(UnreadBean unreadBean, String accountId) throws WeiboException {
        int count = unreadBean.getCmt();
        UnreadBean currentCount = new UnreadDao(access_token, accountId).getCount();
        if (currentCount == null) {
            return false;
        }
        //already reset or have new unread message
        if (count != currentCount.getCmt()) {
            return false;
        }
        return new ClearUnreadDao(access_token, ClearUnreadDao.CMT).clearUnread();
    }


    private String access_token;
    private String type;

    public ClearUnreadDao(String access_token) {

        this.access_token = access_token;
    }

    public ClearUnreadDao(String access_token, String type) {

        this.access_token = access_token;
        this.type = type;
    }


}
