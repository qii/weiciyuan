package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.io.Serializable;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 */
public class AccountBean implements Serializable {

    public String getUid() {
        return (info != null ? info.getId() : "");
    }

    public String getUsernick() {
        return (info != null ? info.getScreen_name() : "");
    }

    public String getAvatar_url() {
        return (info != null ? info.getProfile_image_url() : "");
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public UserBean getInfo() {
        return info;
    }

    public void setInfo(UserBean info) {
        this.info = info;
    }

    public boolean isBlack_magic() {
        return black_magic;
    }

    public void setBlack_magic(boolean black_magic) {
        this.black_magic = black_magic;
    }

    private String access_token;

    private UserBean info;

    private boolean black_magic;

    private int navigationPosition;

    public int getNavigationPosition() {
        return navigationPosition;
    }

    public void setNavigationPosition(int navigationPosition) {
        this.navigationPosition = navigationPosition;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
