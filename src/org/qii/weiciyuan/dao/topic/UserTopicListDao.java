package org.qii.weiciyuan.dao.topic;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: qii
 * Date: 12-11-18
 */
public class UserTopicListDao {

    private String getMsgListJson() throws WeiboException {
        String url = URLHelper.TOPIC_USER_LIST;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("count", count);
        map.put("page", page);
        map.put("uid", uid);


        String jsonData = null;

        jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);


        return jsonData;
    }

    public ArrayList<String> getGSONMsgList() throws WeiboException {

        String json = getMsgListJson();
        Gson gson = new Gson();

        ArrayList<TopicBean> value = null;
        try {
            value = gson.fromJson(json, new TypeToken<List<TopicBean>>() {
            }.getType());
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }

        if (value != null) {
            ArrayList<String> msgList = new ArrayList<String>();
            for (TopicBean b : value) {
                msgList.add(b.hotword);
            }
            return msgList;
        }

        return new ArrayList<String>();
    }


    private String access_token;
    private String uid;
    private String count;
    private String page;


    public UserTopicListDao(String access_token, String uid) {

        this.access_token = access_token;
        this.count = SettingUtility.getMsgCount();
        this.uid = uid;
    }


    public UserTopicListDao setCount(String count) {
        this.count = count;
        return this;
    }

    public UserTopicListDao setPage(String page) {
        this.page = page;
        return this;
    }


    private static class TopicBean {
        private String num;
        private String trend_id;
        private String hotword;
    }
}
