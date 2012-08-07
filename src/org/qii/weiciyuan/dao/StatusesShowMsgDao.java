package org.qii.weiciyuan.dao;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-7
 * Time: 下午3:38
 */
public class StatusesShowMsgDao {

    private String access_token;
    private String id;

    public StatusesShowMsgDao(String access_token, String id) {
        this.access_token = access_token;
        this.id = id;
    }

    public WeiboMsgBean getMsg() {

        String url = URLHelper.getStatuses_Show();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("id", id);

        String json = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        Gson gson = new Gson();

        WeiboMsgBean value = null;
        try {
            value = gson.fromJson(json, WeiboMsgBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage().toString());
        }

        return value;

    }
}
