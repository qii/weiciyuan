package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-8-18
 */
public class FavBean implements Serializable {
    private MessageBean status;
    private String favorited_time;

    public MessageBean getStatus() {
        return status;
    }

    public void setStatus(MessageBean status) {
        this.status = status;
    }

    public String getFavorited_time() {
        return favorited_time;
    }

    public void setFavorited_time(String favorited_time) {
        this.favorited_time = favorited_time;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
