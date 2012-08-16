package org.qii.weiciyuan.dao.user;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 * Time: 下午3:04
 */
public class FriendListDao {

    public UserListBean getGSONMsgList() {

        String url = URLHelper.getFriendListById();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("uid", uid);
        map.put("cursor", cursor);
        map.put("trim_status", trim_status);
        map.put("count", count);
        map.put("screen_name", screen_name);

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
            ActivityUtils.showTips("发生错误，请重刷");
            AppLogger.e(e.getMessage());
        }

        return value;
    }


    public FriendListDao(String token, String uid) {
        this.access_token = token;
        this.uid = uid;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public void setTrim_status(String trim_status) {
        this.trim_status = trim_status;
    }

    private String access_token;
    private String uid;
    private String screen_name;
    private String count;
    private String cursor;
    private String trim_status;
}
