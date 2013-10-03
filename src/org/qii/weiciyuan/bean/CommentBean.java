package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;
import org.qii.weiciyuan.support.utils.TimeUtility;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class CommentBean extends ItemBean implements Parcelable {
    private String created_at;
    private long id;
    private String idstr;
    private String text;
    private String source;
    private String mid;
    private long mills;

    private UserBean user;
    private MessageBean status;
    private CommentBean reply_comment;

    private String sourceString;

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
        dest.writeString(mid);
        dest.writeLong(mills);


        dest.writeParcelable(user, flags);
        dest.writeParcelable(status, flags);
        dest.writeParcelable(reply_comment, flags);

        dest.writeString(sourceString);


    }

    public static final Parcelable.Creator<CommentBean> CREATOR =
            new Parcelable.Creator<CommentBean>() {
                public CommentBean createFromParcel(Parcel in) {
                    CommentBean commentBean = new CommentBean();
                    commentBean.created_at = in.readString();
                    commentBean.id = in.readLong();
                    commentBean.idstr = in.readString();
                    commentBean.text = in.readString();
                    commentBean.source = in.readString();
                    commentBean.mid = in.readString();

                    commentBean.mills = in.readLong();

                    commentBean.user = in.readParcelable(UserBean.class.getClassLoader());
                    commentBean.status = in.readParcelable(MessageBean.class.getClassLoader());
                    commentBean.reply_comment = in.readParcelable(CommentBean.class.getClassLoader());

                    commentBean.sourceString = in.readString();
                    return commentBean;
                }

                public CommentBean[] newArray(int size) {
                    return new CommentBean[size];
                }
            };


    public CommentBean getReply_comment() {
        return reply_comment;
    }

    public void setReply_comment(CommentBean reply_comment) {
        this.reply_comment = reply_comment;
    }


    //comment timeline show comment
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


    public long getMills() {
        if (mills == 0L) {
            TimeUtility.dealMills(this);
        }
        return mills;
    }

    public void setMills(long mills) {
        this.mills = mills;
    }

    public String getCreated_at() {

        return created_at;
    }

    public String getListviewItemShowTime() {
        return TimeUtility.getListTime(this);
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

    public long getIdLong() {
        return this.id;
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


    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public MessageBean getStatus() {
        return status;
    }

    public void setStatus(MessageBean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
