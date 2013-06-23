package org.qii.weiciyuan.support.database.draftbean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.bean.GeoBean;

/**
 * User: qii
 * Date: 12-10-21
 */
public class StatusDraftBean implements Parcelable {
    private String content;
    private String pic;
    private GeoBean gps;
    private String accountId;
    private String id;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(pic);
        dest.writeParcelable(gps, flags);
        dest.writeString(accountId);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<StatusDraftBean> CREATOR =
            new Parcelable.Creator<StatusDraftBean>() {
                public StatusDraftBean createFromParcel(Parcel in) {
                    StatusDraftBean statusDraftBean = new StatusDraftBean();
                    statusDraftBean.content = in.readString();
                    statusDraftBean.pic = in.readString();
                    statusDraftBean.gps = in.readParcelable(GeoBean.class.getClassLoader());
                    statusDraftBean.accountId = in.readString();
                    statusDraftBean.id = in.readString();
                    return statusDraftBean;
                }

                public StatusDraftBean[] newArray(int size) {
                    return new StatusDraftBean[size];
                }
            };


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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
