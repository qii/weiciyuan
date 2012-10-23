package org.qii.weiciyuan.support.database.draftbean;

import org.qii.weiciyuan.bean.GeoBean;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-10-21
 */
public class StatusDraftBean implements Serializable {
    private String content;
    private String pic;
    private GeoBean gps;
    private String accountId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public GeoBean getGps() {
        return gps;
    }

    public void setGps(GeoBean gps) {
        this.gps = gps;
    }

    public String getAccountId() {

        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
