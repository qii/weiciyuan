package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-7-29
 */
public class UserBean implements Parcelable {

    public static final int V_TYPE_NONE = -1;
    public static final int V_TYPE_PERSONAL = 0;
    public static final int V_TYPE_ENTERPRISE = 1;

    public boolean isEnterpriseV() {
        return verified_type == V_TYPE_ENTERPRISE;
    }

    public boolean isPersonalV() {
        return verified_type == V_TYPE_PERSONAL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatuses_count() {
        return statuses_count;
    }

    public void setStatuses_count(String statuses_count) {
        this.statuses_count = statuses_count;
    }

    public String getFavourites_count() {
        return favourites_count;
    }

    public void setFavourites_count(String favourites_count) {
        this.favourites_count = favourites_count;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFollow_me() {
        return follow_me;
    }

    public void setFollow_me(boolean follow_me) {
        this.follow_me = follow_me;
    }

    public String getAllow_all_act_msg() {
        return allow_all_act_msg;
    }

    public void setAllow_all_act_msg(String allow_all_act_msg) {
        this.allow_all_act_msg = allow_all_act_msg;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGeo_enabled() {
        return geo_enabled;
    }

    public void setGeo_enabled(String geo_enabled) {
        this.geo_enabled = geo_enabled;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getAllow_all_comment() {
        return allow_all_comment;
    }

    public void setAllow_all_comment(String allow_all_comment) {
        this.allow_all_comment = allow_all_comment;
    }

    public String getCover_image() {
        return cover_image;
    }

    public void setCover_image(String cover_image) {
        this.cover_image = cover_image;
    }

    public String getAvatar_large() {
        return avatar_large;
    }

    public void setAvatar_large(String avatar_large) {
        this.avatar_large = avatar_large;
    }

    public String getVerified_reason() {
        return verified_reason;
    }

    public void setVerified_reason(String verified_reason) {
        this.verified_reason = verified_reason;
    }

    public int getVerified_type() {
        return verified_type;
    }

    public void setVerified_type(int verified_type) {
        this.verified_type = verified_type;
    }

    public String getOnline_status() {
        return online_status;
    }

    public void setOnline_status(String online_status) {
        this.online_status = online_status;
    }

    public String getBi_followers_count() {
        return bi_followers_count;
    }

    public void setBi_followers_count(String bi_followers_count) {
        this.bi_followers_count = bi_followers_count;
    }

    private String id;
    private String screen_name;
    private String name;
    private String province;
    private String city;
    private String location;
    private String description;
    private String url;
    private String profile_image_url;
    private String cover_image;
    private String domain;
    private String gender;
    private String statuses_count = "0";
    private String favourites_count = "0";
    private String created_at;
    private boolean following;
    private String allow_all_act_msg;
    private String remark;
    private String geo_enabled;
    private boolean verified;
    private String allow_all_comment;
    private String avatar_large;
    private String verified_reason;
    private int verified_type;
    private boolean follow_me;
    private String online_status;
    private String bi_followers_count;
    private String followers_count = "0";
    private String friends_count = "0";

    public String getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(String followers_count) {
        this.followers_count = followers_count;
    }

    public String getFriends_count() {
        return friends_count;
    }

    public void setFriends_count(String friends_count) {
        this.friends_count = friends_count;
    }


    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(screen_name);
        dest.writeString(name);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(location);
        dest.writeString(description);
        dest.writeString(url);
        dest.writeString(profile_image_url);
        dest.writeString(cover_image);
        dest.writeString(domain);
        dest.writeString(gender);
        dest.writeString(statuses_count);
        dest.writeString(favourites_count);
        dest.writeString(created_at);
        dest.writeString(allow_all_act_msg);
        dest.writeString(remark);
        dest.writeString(geo_enabled);
        dest.writeString(allow_all_comment);
        dest.writeString(avatar_large);
        dest.writeString(verified_reason);
        dest.writeInt(verified_type);
        dest.writeString(online_status);
        dest.writeString(bi_followers_count);
        dest.writeString(followers_count);
        dest.writeString(friends_count);

        dest.writeBooleanArray(new boolean[]{this.following, this.follow_me, this.verified});
    }

    public static final Parcelable.Creator<UserBean> CREATOR =
            new Parcelable.Creator<UserBean>() {
                public UserBean createFromParcel(Parcel in) {
                    UserBean userBean = new UserBean();
                    userBean.id = in.readString();
                    userBean.screen_name = in.readString();
                    userBean.name = in.readString();
                    userBean.province = in.readString();
                    userBean.city = in.readString();
                    userBean.location = in.readString();
                    userBean.description = in.readString();
                    userBean.url = in.readString();
                    userBean.profile_image_url = in.readString();
                    userBean.cover_image = in.readString();
                    userBean.domain = in.readString();
                    userBean.gender = in.readString();
                    userBean.statuses_count = in.readString();
                    userBean.favourites_count = in.readString();
                    userBean.created_at = in.readString();
                    userBean.allow_all_act_msg = in.readString();
                    userBean.remark = in.readString();
                    userBean.geo_enabled = in.readString();
                    userBean.allow_all_comment = in.readString();
                    userBean.avatar_large = in.readString();
                    userBean.verified_reason = in.readString();
                    userBean.verified_type = in.readInt();
                    userBean.online_status = in.readString();
                    userBean.bi_followers_count = in.readString();
                    userBean.followers_count = in.readString();
                    userBean.friends_count = in.readString();

                    boolean[] booleans = new boolean[3];
                    in.readBooleanArray(booleans);
                    userBean.following = booleans[0];
                    userBean.follow_me = booleans[1];
                    userBean.verified = booleans[2];

                    return userBean;
                }

                public UserBean[] newArray(int size) {
                    return new UserBean[size];
                }
            };
}
