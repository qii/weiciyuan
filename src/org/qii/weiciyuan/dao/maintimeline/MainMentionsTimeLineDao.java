package org.qii.weiciyuan.dao.maintimeline;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: qii
 * Date: 12-7-28
 */
public class MainMentionsTimeLineDao {

    private String getMsgListJson() throws WeiboException {
        String url = URLHelper.getMentionsTimeLine();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("since_id", since_id);
        map.put("max_id", max_id);
        map.put("count", count);
        map.put("page", page);
        map.put("filter_by_author", filter_by_author);
        map.put("filter_by_source", filter_by_source);
        map.put("filter_by_type", filter_by_type);
        map.put("trim_user", trim_user);


        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        return jsonData;
    }

    public MessageListBean getGSONMsgList() throws WeiboException {

        String json = getMsgListJson();
        Gson gson = new Gson();

        MessageListBean value = null;
        try {
            value = gson.fromJson(json, MessageListBean.class);
        } catch (JsonSyntaxException e) {
            ActivityUtils.showTips("发生错误，请重刷");
            AppLogger.e(e.getMessage().toString());
        }

        /**
         * sometime sina weibo may delete message,so data don't have any user information
         */
        if (value != null) {
            List<MessageBean> msgList = value.getStatuses();

            Iterator<MessageBean> iterator = msgList.iterator();

            while (iterator.hasNext()) {

                MessageBean msg = iterator.next();
                if (msg.getUser() == null) {
                    iterator.remove();
                }
            }

        }

        return value;
    }


    private String access_token;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String filter_by_author;
    private String filter_by_source;
    private String filter_by_type;
    private String trim_user;

    public MainMentionsTimeLineDao(String access_token) {
        this.access_token = access_token;
    }

    public MainMentionsTimeLineDao setSince_id(String since_id) {
        this.since_id = since_id;
        return this;
    }

    public MainMentionsTimeLineDao setMax_id(String max_id) {
        this.max_id = max_id;
        return this;
    }

    public MainMentionsTimeLineDao setCount(String count) {
        this.count = count;
        return this;
    }

    public MainMentionsTimeLineDao setPage(String page) {
        this.page = page;
        return this;
    }

    public MainMentionsTimeLineDao setFilter_by_author(String filter_by_author) {
        this.filter_by_author = filter_by_author;
        return this;
    }

    public MainMentionsTimeLineDao setFilter_by_source(String filter_by_source) {
        this.filter_by_source = filter_by_source;
        return this;
    }

    public MainMentionsTimeLineDao setFilter_by_type(String filter_by_type) {
        this.filter_by_type = filter_by_type;
        return this;
    }

    public MainMentionsTimeLineDao setTrim_user(String trim_user) {
        this.trim_user = trim_user;
        return this;
    }
}
