package org.qii.weiciyuan.dao.timeline;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.TimeUtility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-13
 */
public class CommentsTimeLineByIdDao {

    public CommentListBean getGSONMsgList() throws WeiboException {

        String url = URLHelper.COMMENTS_TIMELINE_BY_MSGID;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("id", id);
        map.put("since_id", since_id);
        map.put("max_id", max_id);
        map.put("count", count);
        map.put("page", page);
        map.put("filter_by_author", filter_by_author);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        Gson gson = new Gson();

        CommentListBean value = null;
        try {
            value = gson.fromJson(jsonData, CommentListBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }

        if (value != null && value.getItemList().size() > 0) {
            List<CommentBean> msgList = value.getItemList();
            Iterator<CommentBean> iterator = msgList.iterator();
            while (iterator.hasNext()) {

                CommentBean msg = iterator.next();
                if (msg.getUser() == null) {
                    iterator.remove();
                } else {
                    msg.getListViewSpannableString();
                    TimeUtility.dealMills(msg);
                }
            }
        }

        return value;
    }

    public CommentsTimeLineByIdDao(String token, String id) {

        this.access_token = token;
        this.id = id;
        this.count = SettingUtility.getMsgCount();
    }

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
    private String id;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String filter_by_author;
}
