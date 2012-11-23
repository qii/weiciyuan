package org.qii.weiciyuan.dao.search;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.SearchStatusListBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-9-8
 */
public class SearchDao {
    public UserListBean getUserList() {

        String url = URLHelper.USERS_SEARCH;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("count", count);
        map.put("page", page);
        map.put("q", q);


        String jsonData = null;
        try {
            jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();

        UserListBean value = null;
        try {
            value = gson.fromJson(jsonData, UserListBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }

        return value;
    }

    public SearchStatusListBean getStatusList() {

        String url = URLHelper.STATUSES_SEARCH;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("count", count);
        map.put("page", page);
        map.put("q", q);


        String jsonData = null;
        try {
            jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();

        SearchStatusListBean value = null;
        try {
            value = gson.fromJson(jsonData, SearchStatusListBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }

        return value;
    }


    private String access_token;
    private String q;
    private String count;
    private String page;


    public SearchDao(String access_token, String q) {

        this.access_token = access_token;
        this.q = q;
        this.count = String.valueOf(AppConfig.DEFAULT_MSG_NUMBERS);
    }


    public SearchDao setCount(String count) {
        this.count = count;
        return this;
    }

    public SearchDao setPage(String page) {
        this.page = page;
        return this;
    }


}
