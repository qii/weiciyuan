package org.qii.weiciyuan.dao;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 * Time: 下午2:47
 */
public class UserDao {

    public UserBean getUserInfo() {
        String url = URLHelper.getUser();
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("uid", uid);
        map.put("screen_name", screen_name);

        String jsonData = null;
        try {
            jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Gson gson = new Gson();

        UserBean value = null;
        try {
            value = gson.fromJson(jsonData, UserBean.class);
        } catch (JsonSyntaxException e) {
            ActivityUtils.showTips("发生错误，请重刷");
            AppLogger.e(e.getMessage().toString());
        }


        return value;

    }

    private String access_token;
    private String uid;
    private String screen_name;

    public UserDao(String access_token) {
        this.access_token = access_token;
    }

    public UserDao setScreen_name(String screen_name) {
        this.screen_name = screen_name;
        return this;
    }

    public UserDao setUid(String uid) {
        this.uid = uid;
        return this;
    }
}
