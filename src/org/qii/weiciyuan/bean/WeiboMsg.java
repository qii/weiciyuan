package org.qii.weiciyuan.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午7:47
 * To change this template use File | Settings | File Templates.
 */
public class WeiboMsg {
    public String getCreated_at() {

        SimpleDateFormat format = new SimpleDateFormat("kk:mm");
        return format.format(new Date(created_at));

//        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFavorited() {
        return favorited;
    }

    public void setFavorited(String favorited) {
        this.favorited = favorited;
    }

    public String getTruncated() {
        return truncated;
    }

    public void setTruncated(String truncated) {
        this.truncated = truncated;
    }

    public String getIn_reply_to_status_id() {
        return in_reply_to_status_id;
    }

    public void setIn_reply_to_status_id(String in_reply_to_status_id) {
        this.in_reply_to_status_id = in_reply_to_status_id;
    }

    public String getIn_reply_to_user_id() {
        return in_reply_to_user_id;
    }

    public void setIn_reply_to_user_id(String in_reply_to_user_id) {
        this.in_reply_to_user_id = in_reply_to_user_id;
    }

    public String getIn_reply_to_screen_name() {
        return in_reply_to_screen_name;
    }

    public void setIn_reply_to_screen_name(String in_reply_to_screen_name) {
        this.in_reply_to_screen_name = in_reply_to_screen_name;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getReposts_count() {
        return reposts_count;
    }

    public void setReposts_count(String reposts_count) {
        this.reposts_count = reposts_count;
    }

    public String getComments_count() {
        return comments_count;
    }

    public void setComments_count(String comments_count) {
        this.comments_count = comments_count;
    }

//    public String getAnnotations() {
//        return annotations;
//    }
//
//    public void setAnnotations(String annotations) {
//        this.annotations = annotations;
//    }

    public WeiboUser getUser() {
        return user;
    }

    public void setUser(WeiboUser user) {
        this.user = user;
    }

    public WeiboMsg getRetweeted_status() {
        return retweeted_status;
    }

    public void setRetweeted_status(WeiboMsg retweeted_status) {
        this.retweeted_status = retweeted_status;
    }

    public String getListviewItemShowTime() {
        return listviewItemShowTime;
    }

    public void setListviewItemShowTime(String listviewItemShowTime) {
        this.listviewItemShowTime = listviewItemShowTime;
    }

    private String created_at;
    private String id;
    private String text;
    private String source;
    private String favorited;
    private String truncated;
    private String in_reply_to_status_id;
    private String in_reply_to_user_id;
    private String in_reply_to_screen_name;
    private String mid;
    private String reposts_count;
    private String comments_count;
//    private Object annotations;
    private WeiboUser user;
    private WeiboMsg retweeted_status;
    private Geo geo;

    private String listviewItemShowTime;
}
