package org.qii.weiciyuan.dao.unread;

import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.AppLogger;

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

    private String getMsgListJson() throws WeiboException {
        String url = getUrl();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("type", type);


        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);


        return jsonData;
    }

    public boolean clearUnread() throws WeiboException {

        String json = getMsgListJson();

        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optBoolean("result", false);
        } catch (JSONException e) {
            AppLogger.e(e.getMessage());
        }

        return false;
    }


    private String access_token;
    private String type;


    public ClearUnreadDao(String access_token, String type) {

        this.access_token = access_token;
        this.type = type;
    }


}
