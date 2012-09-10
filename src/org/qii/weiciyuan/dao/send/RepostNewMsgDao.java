package org.qii.weiciyuan.dao.send;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
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

    public MessageBean sendNewMsg() throws WeiboException {
        String url = URLHelper.new_Repost();
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("id", id);
        map.put("status", status);
        map.put("is_comment", is_comment);


        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);

        Gson gson = new Gson();

        MessageBean value = null;
        try {
            value = gson.fromJson(jsonData, MessageBean.class);
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

    public void setIs_comment(boolean enable) {
        if (enable)
            this.is_comment = "1";
        else
            this.is_comment = "0";
    }

    private String access_token;
    private String id;
    private String status;
    private String is_comment;
}
