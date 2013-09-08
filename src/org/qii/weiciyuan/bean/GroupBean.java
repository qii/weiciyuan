package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-10-17
 */
public class GroupBean implements Parcelable {

    private String id;
    private String idstr;
    private String name;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(idstr);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<GroupBean> CREATOR =
            new Parcelable.Creator<GroupBean>() {
                public GroupBean createFromParcel(Parcel in) {
                    GroupBean groupBean = new GroupBean();
                    groupBean.id = in.readString();
                    groupBean.idstr = in.readString();
                    groupBean.name = in.readString();
                    return groupBean;
                }

                public GroupBean[] newArray(int size) {
                    return new GroupBean[size];
                }
            };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdstr() {
        return idstr;
    }

    public void setIdstr(String idstr) {
        this.idstr = idstr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
