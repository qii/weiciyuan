package org.qii.weiciyuan.bean;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午7:42
 * To change this template use File | Settings | File Templates.
 */
public class UserBean implements Serializable {

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

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
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

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getAllow_all_comment() {
        return allow_all_comment;
    }

    public void setAllow_all_comment(String allow_all_comment) {
        this.allow_all_comment = allow_all_comment;
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

    public String getFollow_me() {
        return follow_me;
    }

    public void setFollow_me(String follow_me) {
        this.follow_me = follow_me;
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
    private String domain;
    private String gender;
    private String  statuses_count;
    private String favourites_count;
    private String created_at;
    private String following;
    private String allow_all_act_msg;
    private String remark;
    private String geo_enabled;
    private String verified;
    private String allow_all_comment;
    private String avatar_large;
    private String verified_reason;
    private String follow_me;
    private String online_status;
    private String bi_followers_count;

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

    private String followers_count;
    private String friends_count;

}
