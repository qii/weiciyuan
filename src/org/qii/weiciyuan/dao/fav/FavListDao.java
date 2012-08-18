package org.qii.weiciyuan.dao.fav;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.FavListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-8-18
 */
public class FavListDao {
    private String getMsgListJson() {
        String url = URLHelper.getFavList();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("count", count);
        map.put("page", page);


        String jsonData = null;
        try {
            jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();
        }

        return jsonData;
    }

    public FavListBean getGSONMsgList() {

        String json = getMsgListJson();
        Gson gson = new Gson();

        FavListBean value = null;
        try {
            value = gson.fromJson(json, FavListBean.class);
        } catch (JsonSyntaxException e) {
            ActivityUtils.showTips("发生错误，请重刷");
            AppLogger.e(e.getMessage());
        }

        return value;
    }


    private String access_token;
    private String count;
    private String page;


    public FavListDao(String access_token) {

        this.access_token = access_token;
        this.count = String.valueOf(AppConfig.DEFAULT_MSG_NUMBERS);
    }


    public FavListDao setCount(String count) {
        this.count = count;
        return this;
    }

    public FavListDao setPage(String page) {
        this.page = page;
        return this;
    }


}
