package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-10-7
 */
public class AtUserBean implements Parcelable {
    private String uid;
    private String nickname;
    private String remark;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(nickname);
        dest.writeString(remark);
    }

    public static final Parcelable.Creator<AtUserBean> CREATOR =
            new Parcelable.Creator<AtUserBean>() {
                public AtUserBean createFromParcel(Parcel in) {
                    AtUserBean atUserBean = new AtUserBean();
                    atUserBean.uid = in.readString();
                    atUserBean.nickname = in.readString();
                    atUserBean.remark = in.readString();
                    return atUserBean;
                }

                public AtUserBean[] newArray(int size) {
                    return new AtUserBean[size];
                }
            };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
