package org.qii.weiciyuan.dao.show;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class ShowUserDao {

    public UserBean getUserInfo() throws WeiboException {
        String url = (!TextUtils.isEmpty(domain) ? URLHelper.USER_DOMAIN_SHOW
                : URLHelper.USER_SHOW);
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("uid", uid);
        map.put("screen_name", screen_name);
        map.put("domain", domain);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        Gson gson = new Gson();

        UserBean value = null;
        try {
            value = gson.fromJson(jsonData, UserBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }

        return value;
    }

    private String access_token;
    private String uid;
    private String screen_name;
    private String domain;

    public ShowUserDao(String access_token) {

        this.access_token = access_token;
    }

    public ShowUserDao setScreen_name(String screen_name) {
        this.screen_name = screen_name;
        return this;
    }

    public ShowUserDao setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
