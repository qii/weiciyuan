package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-8-18
 */
public class FavBean implements Parcelable {
    private MessageBean status;
    private String favorited_time;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeParcelable(status, flags);
        dest.writeString(favorited_time);

    }

    public static final Parcelable.Creator<FavBean> CREATOR =
            new Parcelable.Creator<FavBean>() {
                public FavBean createFromParcel(Parcel in) {
                    FavBean favBean = new FavBean();

                    favBean.status = in.readParcelable(MessageBean.class.getClassLoader());
                    favBean.favorited_time = in.readString();

                    return favBean;
                }

                public FavBean[] newArray(int size) {
                    return new FavBean[size];
                }
            };


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
