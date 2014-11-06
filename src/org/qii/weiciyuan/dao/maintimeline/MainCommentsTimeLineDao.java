package org.qii.weiciyuan.dao.maintimeline;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.dao.unread.ClearUnreadDao;
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
 * User: qii
 * Date: 12-7-29
 */
public class MainCommentsTimeLineDao implements ICommentsTimeLineDao {

    public void setSince_id(String since_id) {
        this.since_id = since_id;
    }

    public void setMax_id(String max_id) {
        this.max_id = max_id;
    }

    public MainCommentsTimeLineDao setCount(String count) {
        this.count = count;
        return this;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setFilter_by_author(String filter_by_author) {
        this.filter_by_author = filter_by_author;
    }

    protected String access_token;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String filter_by_author;
    private String filter_by_source;

    public MainCommentsTimeLineDao(String access_token) {

        this.access_token = access_token;
        this.count = SettingUtility.getMsgCount();
    }

    protected String getUrl() {
        return URLHelper.COMMENTS_TO_ME_TIMELINE;
    }

    public CommentListBean getGSONMsgListWithoutClearUnread() throws WeiboException {
        String url = getUrl();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("since_id", since_id);
        map.put("max_id", max_id);
        map.put("count", count);
        map.put("page", page);
        map.put("filter_by_author", filter_by_author);
        map.put("filter_by_source", filter_by_source);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        Gson gson = new Gson();

        CommentListBean value = null;
        try {
            value = gson.fromJson(jsonData, CommentListBean.class);
        } catch (JsonSyntaxException e) {
            AppLogger.e(e.getMessage());
        }

        if (value != null && value.getSize() > 0) {
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

    public CommentListBean getGSONMsgList() throws WeiboException {
        CommentListBean value = getGSONMsgListWithoutClearUnread();
        clearUnread();
        return value;
    }

    protected void clearUnread() {
        try {
            new ClearUnreadDao(access_token, ClearUnreadDao.CMT).clearUnread();
        } catch (WeiboException ignored) {

        }
    }
}
