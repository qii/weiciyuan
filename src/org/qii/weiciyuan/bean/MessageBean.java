package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;
import org.qii.weiciyuan.support.utils.TimeTool;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: qii
 * Date: 12-7-29
 */

public class MessageBean extends ItemBean implements Parcelable {

    private String created_at;
    private long id;
    private String idstr;
    private String text;
    private String source;
    private boolean favorited;
    private String truncated;
    private String in_reply_to_status_id;
    private String in_reply_to_user_id;
    private String in_reply_to_screen_name;
    private String mid;
    private int reposts_count = 0;
    private int comments_count = 0;
    //    private Object annotations;

    private String thumbnail_pic;
    private String bmiddle_pic;
    private String original_pic;

    private String sourceString;

    private long mills;

    private MessageBean retweeted_status;
    private UserBean user;
    private GeoBean geo;


    private transient SpannableString listViewSpannableString;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(created_at);
        dest.writeLong(id);

        dest.writeString(idstr);
        dest.writeString(text);
        dest.writeString(source);
        dest.writeBooleanArray(new boolean[]{this.favorited});
        dest.writeString(truncated);
        dest.writeString(in_reply_to_status_id);
        dest.writeString(in_reply_to_user_id);
        dest.writeString(in_reply_to_screen_name);
        dest.writeString(mid);
        dest.writeInt(reposts_count);
        dest.writeInt(comments_count);

        dest.writeString(thumbnail_pic);
        dest.writeString(bmiddle_pic);
        dest.writeString(original_pic);

        dest.writeString(sourceString);

        dest.writeLong(mills);

        dest.writeParcelable(retweeted_status, flags);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(geo, flags);

    }

    public static final Parcelable.Creator<MessageBean> CREATOR =
            new Parcelable.Creator<MessageBean>() {
                public MessageBean createFromParcel(Parcel in) {
                    MessageBean messageBean = new MessageBean();
                    messageBean.created_at = in.readString();
                    messageBean.id = in.readLong();
                    messageBean.idstr = in.readString();
                    messageBean.text = in.readString();
                    messageBean.source = in.readString();

                    boolean[] booleans = new boolean[1];
                    in.readBooleanArray(booleans);
                    messageBean.favorited = booleans[0];


                    messageBean.truncated = in.readString();
                    messageBean.in_reply_to_status_id = in.readString();
                    messageBean.in_reply_to_user_id = in.readString();
                    messageBean.in_reply_to_screen_name = in.readString();
                    messageBean.mid = in.readString();


                    messageBean.reposts_count = in.readInt();
                    messageBean.comments_count = in.readInt();

                    messageBean.thumbnail_pic = in.readString();
                    messageBean.bmiddle_pic = in.readString();
                    messageBean.original_pic = in.readString();
                    messageBean.sourceString = in.readString();
                    messageBean.mills = in.readLong();

                    messageBean.retweeted_status = in.readParcelable(MessageBean.class.getClassLoader());
                    messageBean.user = in.readParcelable(UserBean.class.getClassLoader());
                    messageBean.geo = in.readParcelable(GeoBean.class.getClassLoader());

                    return messageBean;
                }

                public MessageBean[] newArray(int size) {
                    return new MessageBean[size];
                }
            };


    public String getCreated_at() {

        return created_at;
    }

    public String getTimeInFormat() {
        if (!TextUtils.isEmpty(created_at)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            return format.format(new Date(created_at));

        }
        return "";
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return idstr;
    }

    public void setId(String id) {
        this.idstr = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public String getTruncated() {
        return truncated;
    }

    public void setTruncated(String truncated) {
        this.truncated = truncated;
    }

    public String getIn_reply_to_status_id() {
        return in_reply_to_status_id;
    }

    public void setIn_reply_to_status_id(String in_reply_to_status_id) {
        this.in_reply_to_status_id = in_reply_to_status_id;
    }

    public String getIn_reply_to_user_id() {
        return in_reply_to_user_id;
    }

    public void setIn_reply_to_user_id(String in_reply_to_user_id) {
        this.in_reply_to_user_id = in_reply_to_user_id;
    }

    public String getIn_reply_to_screen_name() {
        return in_reply_to_screen_name;
    }

    public void setIn_reply_to_screen_name(String in_reply_to_screen_name) {
        this.in_reply_to_screen_name = in_reply_to_screen_name;
    }

    public GeoBean getGeo() {
        return geo;
    }

    public void setGeo(GeoBean geo) {
        this.geo = geo;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public int getReposts_count() {
        return reposts_count;
    }

    public void setReposts_count(int reposts_count) {
        this.reposts_count = reposts_count;
    }

    public int getComments_count() {
        return comments_count;
    }

    public void setComments_count(int comments_count) {
        this.comments_count = comments_count;
    }


    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public MessageBean getRetweeted_status() {
        return retweeted_status;
    }

    public void setRetweeted_status(MessageBean retweeted_status) {
        this.retweeted_status = retweeted_status;
    }

    public String getListviewItemShowTime() {
        return TimeTool.getListTime(this);
    }

    public long getIdLong() {
        return this.id;
    }


    public SpannableString getListViewSpannableString() {
        if (!TextUtils.isEmpty(listViewSpannableString)) {
            return listViewSpannableString;
        } else {
            ListViewTool.addJustHighLightLinks(this);
            return listViewSpannableString;
        }
    }

    public void setListViewSpannableString(SpannableString listViewSpannableString) {
        this.listViewSpannableString = listViewSpannableString;
    }

    public String getSourceString() {
        if (!TextUtils.isEmpty(sourceString)) {
            return sourceString;
        } else {
            if (!TextUtils.isEmpty(source))
                sourceString = Html.fromHtml(this.source).toString();
            return sourceString;
        }
    }

    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }


    public long getMills() {
        if (mills == 0L) {
            TimeTool.dealMills(this);
        }
        return mills;
    }

    public void setMills(long mills) {
        this.mills = mills;
    }

    public String getThumbnail_pic() {
        return thumbnail_pic;
    }

    public void setThumbnail_pic(String thumbnail_pic) {
        this.thumbnail_pic = thumbnail_pic;
    }

    public String getBmiddle_pic() {
        return bmiddle_pic;
    }

    public void setBmiddle_pic(String bmiddle_pic) {
        this.bmiddle_pic = bmiddle_pic;
    }

    public String getOriginal_pic() {
        return original_pic;
    }

    public void setOriginal_pic(String original_pic) {
        this.original_pic = original_pic;
    }


    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null) {
            return false;
        }

        if (!(otherObject instanceof MessageBean)) {
            return false;
        }

        MessageBean other = (MessageBean) otherObject;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }


}
