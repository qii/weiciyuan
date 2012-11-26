package org.qii.weiciyuan.dao.topic;

import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-11-26
 */
public class TopicDao {
    private String access_token;

    public TopicDao(String token) {
        this.access_token = token;
    }

    public boolean follow(String trend_name) throws WeiboException {
        String url = URLHelper.TOPIC_FOLLOW;
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("trend_name", trend_name);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            Integer id = jsonObject.optInt("topicid", -1);
            if (id > 0) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {

        }
        return false;

    }

    public boolean destroy(String trend_id) throws WeiboException {
        String url = URLHelper.TOPIC_DESTROY;
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("trend_id", trend_id);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject.optBoolean("result", false);

        } catch (JSONException e) {

        }
        return false;

    }
}
