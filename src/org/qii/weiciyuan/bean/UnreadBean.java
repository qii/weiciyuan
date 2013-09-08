package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-9-26
 */
public class UnreadBean implements Parcelable {
    private int status;
    private int follower;
    private int cmt;
    private int dm;
    private int mention_status;
    private int mention_cmt;
    private int group;
    private int notice;
    private int invite;
    private int badge;
    private int photo;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeInt(follower);
        dest.writeInt(cmt);
        dest.writeInt(dm);
        dest.writeInt(mention_status);
        dest.writeInt(mention_cmt);
        dest.writeInt(group);
        dest.writeInt(notice);
        dest.writeInt(invite);
        dest.writeInt(badge);
        dest.writeInt(photo);
    }

    public static final Parcelable.Creator<UnreadBean> CREATOR =
            new Parcelable.Creator<UnreadBean>() {
                public UnreadBean createFromParcel(Parcel in) {
                    UnreadBean unreadBean = new UnreadBean();
                    unreadBean.status = in.readInt();
                    unreadBean.follower = in.readInt();
                    unreadBean.cmt = in.readInt();
                    unreadBean.dm = in.readInt();
                    unreadBean.mention_status = in.readInt();
                    unreadBean.mention_cmt = in.readInt();
                    unreadBean.group = in.readInt();
                    unreadBean.notice = in.readInt();
                    unreadBean.invite = in.readInt();
                    unreadBean.badge = in.readInt();
                    unreadBean.photo = in.readInt();
                    return unreadBean;
                }

                public UnreadBean[] newArray(int size) {
                    return new UnreadBean[size];
                }
            };

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFollower() {
        return follower;
    }

    public void setFollower(int follower) {
        this.follower = follower;
    }

    public int getCmt() {
        return cmt;
    }

    public void setCmt(int cmt) {
        this.cmt = cmt;
    }

    public int getDm() {
        return dm;
    }

    public void setDm(int dm) {
        this.dm = dm;
    }

    public int getMention_status() {
        return mention_status;
    }

    public void setMention_status(int mention_status) {
        this.mention_status = mention_status;
    }

    public int getMention_cmt() {
        return mention_cmt;
    }

    public void setMention_cmt(int mention_cmt) {
        this.mention_cmt = mention_cmt;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getNotice() {
        return notice;
    }

    public void setNotice(int notice) {
        this.notice = notice;
    }

    public int getInvite() {
        return invite;
    }

    public void setInvite(int invite) {
        this.invite = invite;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
