package org.qii.weiciyuan.bean;

import java.io.Serializable;

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
    private WeiboUserBean user;
    private WeiboMsgBean status;

    public String getCreated_at() {
        return created_at;
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

    public WeiboUserBean getUser() {
        return user;
    }

    public void setUser(WeiboUserBean user) {
        this.user = user;
    }

    public WeiboMsgBean getStatus() {
        return status;
    }

    public void setStatus(WeiboMsgBean status) {
        this.status = status;
    }
}
