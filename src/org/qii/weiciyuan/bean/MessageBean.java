package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;
import org.qii.weiciyuan.support.utils.TimeUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private ArrayList<PicUrls> pic_urls = new ArrayList<PicUrls>();
    private ArrayList<String> pic_ids = new ArrayList<String>();


    private transient SpannableString listViewSpannableString;


    public static class PicUrls implements Parcelable {
        public String thumbnail_pic;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(thumbnail_pic);
        }

        public static final Parcelable.Creator<PicUrls> CREATOR =
                new Parcelable.Creator<PicUrls>() {
                    public PicUrls createFromParcel(Parcel in) {
                        PicUrls picUrls = new PicUrls();
                        picUrls.thumbnail_pic = in.readString();
                        return picUrls;
                    }

                    public PicUrls[] newArray(int size) {
                        return new PicUrls[size];
                    }
                };

    }

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

        dest.writeTypedList(pic_urls);
        dest.writeStringList(pic_ids);

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

                    messageBean.pic_urls = new ArrayList<PicUrls>();
                    in.readTypedList(messageBean.pic_urls, PicUrls.CREATOR);

                    messageBean.pic_ids = new ArrayList<String>();
                    in.readStringList(messageBean.pic_ids);
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
        return TimeUtility.getListTime(this);
    }

    public long getIdLong() {
        return this.id;
    }


    public SpannableString getListViewSpannableString() {
        if (!TextUtils.isEmpty(listViewSpannableString)) {
            return listViewSpannableString;
        } else {
            TimeLineUtility.addJustHighLightLinks(this);
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
            TimeUtility.dealMills(this);
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

    private ArrayList<String> thumbnaiUrls = new ArrayList<String>();
    private ArrayList<String> middleUrls = new ArrayList<String>();
    private ArrayList<String> highUrls = new ArrayList<String>();


    public ArrayList<String> getThumbnailPicUrls() {
        if (thumbnaiUrls.size() > 0)
            return thumbnaiUrls;

        for (PicUrls url : pic_urls) {
            thumbnaiUrls.add(url.thumbnail_pic);
        }

        if (thumbnaiUrls.size() == 0) {
            String prefStr = "http://ww4.sinaimg.cn/thumbnail/";
            for (String url : pic_ids) {
                thumbnaiUrls.add(prefStr + url + ".jpg");
            }
        }

        return thumbnaiUrls;
    }

    public ArrayList<String> getMiddlePicUrls() {
        if (middleUrls.size() > 0)
            return middleUrls;

        for (PicUrls url : pic_urls) {
            middleUrls.add(url.thumbnail_pic.replace("thumbnail", "bmiddle"));
        }

        if (middleUrls.size() == 0) {
            String prefStr = "http://ww4.sinaimg.cn/bmiddle/";
            for (String url : pic_ids) {
                middleUrls.add(prefStr + url + ".jpg");
            }
        }

        return middleUrls;
    }


    public ArrayList<String> getHighPicUrls() {
        if (highUrls.size() > 0)
            return highUrls;

        for (PicUrls url : pic_urls) {
            highUrls.add(url.thumbnail_pic.replace("thumbnail", "large"));
        }

        if (highUrls.size() == 0) {
            String prefStr = "http://ww4.sinaimg.cn/large/";
            for (String url : pic_ids) {
                highUrls.add(prefStr + url + ".jpg");
            }
        }

        return highUrls;
    }

    public boolean isMultiPics() {
        return pic_urls.size() > 1 || pic_ids.size() > 1;
    }

    public int getPicCount() {
        return pic_urls.size() > 1 ? pic_urls.size() : pic_ids.size();
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
