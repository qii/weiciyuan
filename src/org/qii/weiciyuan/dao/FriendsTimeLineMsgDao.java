package org.qii.weiciyuan.dao;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.TimeLineMsgListBean;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-28
 * Time: 下午9:55
 * To change this template use File | Settings | File Templates.
 */
public class FriendsTimeLineMsgDao {

    private String getMsgListJson() {
        String url = URLHelper.getFriendsTimeLine();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("since_id", since_id);
        map.put("max_id", max_id);
        map.put("count", count);
        map.put("page", page);
        map.put("base_app", base_app);
        map.put("feature", feature);
        map.put("trim_user", trim_user);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        return jsonData;
    }

    public TimeLineMsgListBean getGSONMsgList() {

        String json = getMsgListJson();
        Gson gson = new Gson();

        TimeLineMsgListBean value = null;
        try {
            value = gson.fromJson(json, TimeLineMsgListBean.class);
        } catch (JsonSyntaxException e) {
            ActivityUtils.showTips("发生错误，请重刷");
            AppLogger.e(e.getMessage().toString());
        }

        return value;
    }


    private String access_token;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String base_app;
    private String feature;
    private String trim_user;

    public FriendsTimeLineMsgDao(String access_token) {
        if (TextUtils.isEmpty(access_token))
            throw new IllegalArgumentException();
        this.access_token = access_token;
        this.count= String.valueOf(AppConfig.DEFAULT_MSG_NUMBERS);
    }

    public FriendsTimeLineMsgDao setSince_id(String since_id) {
        this.since_id = since_id;
        return this;
    }

    public FriendsTimeLineMsgDao setMax_id(String max_id) {
        this.max_id = max_id;
        return this;
    }

    public FriendsTimeLineMsgDao setCount(String count) {
        this.count = count;
        return this;
    }

    public FriendsTimeLineMsgDao setPage(String page) {
        this.page = page;
        return this;
    }

    public FriendsTimeLineMsgDao setBase_app(String base_app) {
        this.base_app = base_app;
        return this;
    }

    public FriendsTimeLineMsgDao setFeature(String feature) {
        this.feature = feature;
        return this;
    }

    public FriendsTimeLineMsgDao setTrim_user(String trim_user) {
        this.trim_user = trim_user;
        return this;
    }


}
