package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-9-8
 */
public class SearchUserBean implements Parcelable {
    private String screen_name;
    private String followers_count;
    private String uid;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(screen_name);
        dest.writeString(followers_count);
        dest.writeString(uid);
    }

    public static final Parcelable.Creator<SearchUserBean> CREATOR =
            new Parcelable.Creator<SearchUserBean>() {
                public SearchUserBean createFromParcel(Parcel in) {
                    SearchUserBean searchUserBean = new SearchUserBean();
                    searchUserBean.screen_name = in.readString();
                    searchUserBean.followers_count = in.readString();
                    searchUserBean.uid = in.readString();
                    return searchUserBean;
                }

                public SearchUserBean[] newArray(int size) {
                    return new SearchUserBean[size];
                }
            };


    public String getScreen_name() {
        return screen_name;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public String getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(String followers_count) {
        this.followers_count = followers_count;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
