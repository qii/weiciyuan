package org.qii.weiciyuan.dao;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午1:17
 * To change this template use File | Settings | File Templates.
 */
public class CommentsTimeLineMsgDao {


    public void setSince_id(String since_id) {
        this.since_id = since_id;
    }

    public void setMax_id(String max_id) {
        this.max_id = max_id;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setFilter_by_author(String filter_by_author) {
        this.filter_by_author = filter_by_author;
    }

    private String access_token;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String filter_by_author;
    private String filter_by_source;

    public CommentsTimeLineMsgDao(String access_token) {
        if (TextUtils.isEmpty(access_token))
            throw new IllegalArgumentException();
        this.access_token = access_token;
    }

    public CommentListBean getGSONMsgList() {

        String url = URLHelper.getCommentList();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("since_id", since_id);
        map.put("max_id", max_id);
        map.put("count", count);
        map.put("page", page);
        map.put("filter_by_author", filter_by_author);
        map.put("filter_by_source", filter_by_source);


        String jsonData = null;
        try {
            jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        Gson gson = new Gson();

        CommentListBean value = null;
        try {
            value = gson.fromJson(jsonData, CommentListBean.class);
        } catch (JsonSyntaxException e) {
            ActivityUtils.showTips("发生错误，请重刷");
            AppLogger.e(e.getMessage().toString());
        }


        return value;
    }
}
