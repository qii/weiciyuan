package org.qii.weiciyuan.bean;

import java.io.Serializable;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 * Time: 上午10:51
 */
public class WeiboAccountBean implements Serializable {
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsernick() {
        return usernick;
    }

    public void setUsernick(String usernick) {
        this.usernick = usernick;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    private String uid;

    private String username;

    private String usernick;

    private String avatar_url;

    private String portrait;

    private String access_token;

 }
