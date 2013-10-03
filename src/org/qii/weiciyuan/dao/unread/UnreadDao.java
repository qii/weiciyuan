package org.qii.weiciyuan.dao.unread;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
public class UnreadDao {

    protected String getUrl() {
        return URLHelper.UNREAD_COUNT;
    }

    private String getMsgListJson() throws WeiboException {
        String url = getUrl();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("uid", uid);


        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);


        return jsonData;
    }

    public UnreadBean getCount() throws WeiboException {

        String json = getMsgListJson();
        Gson gson = new Gson();

        UnreadBean value = null;
        try {
            value = gson.fromJson(json, UnreadBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
            return null;
        }


        return value;
    }


    private String access_token;
    private String uid;


    public UnreadDao(String access_token, String uid) {

        this.access_token = access_token;
        this.uid = uid;
    }


}
