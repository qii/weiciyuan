package org.qii.weiciyuan.dao.send;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-8-13
 * Time: 下午10:50
 */
public class RepostNewMsgDao {

    public WeiboMsgBean sendNewMsg() {
        String url = URLHelper.new_Repost();
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("id", id);
        map.put("status", status);
        map.put("is_comment", is_comment);

        String jsonData = null;
        try {
            jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Gson gson = new Gson();

        WeiboMsgBean value = null;
        try {
            value = gson.fromJson(jsonData, WeiboMsgBean.class);
        } catch (JsonSyntaxException e) {
            ActivityUtils.showTips("发生错误，请重刷");
            AppLogger.e(e.getMessage().toString());
        }


        return value;

    }

    public RepostNewMsgDao(String token, String id) {
        this.access_token = token;
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIs_comment(String is_comment) {
        this.is_comment = is_comment;
    }

    private String access_token;
    private String id;
    private String status;
    private String is_comment;
}
