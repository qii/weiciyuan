package org.qii.weiciyuan.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午3:30
 */
public class CommentBean implements Serializable {
    private String created_at;
    private String id;
    private String text;
    private String source;
    private String mid;
    private UserBean user;
    private WeiboMsgBean status;

    public String getCreated_at() {
        if (!TextUtils.isEmpty(created_at)) {
            SimpleDateFormat format = new SimpleDateFormat("kk:mm");
            return format.format(new Date(created_at));
        }
        return "";
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

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public WeiboMsgBean getStatus() {
        return status;
    }

    public void setStatus(WeiboMsgBean status) {
        this.status = status;
    }
}
