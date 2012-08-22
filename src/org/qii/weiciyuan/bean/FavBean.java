package org.qii.weiciyuan.bean;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-8-18
 * Time: 上午10:32
 */
public class FavBean implements Serializable {
    private MessageBean status ;
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
}
