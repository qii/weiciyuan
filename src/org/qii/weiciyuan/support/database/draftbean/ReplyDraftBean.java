package org.qii.weiciyuan.support.database.draftbean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.bean.CommentBean;

/**
 * User: qii
 * Date: 12-10-21
 */
public class ReplyDraftBean implements Parcelable {
    private String content;
    private String accountId;
    private CommentBean commentBean;
    private String id;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(accountId);
        dest.writeParcelable(commentBean, flags);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<ReplyDraftBean> CREATOR =
            new Parcelable.Creator<ReplyDraftBean>() {
                public ReplyDraftBean createFromParcel(Parcel in) {
                    ReplyDraftBean replyDraftBean = new ReplyDraftBean();
                    replyDraftBean.content = in.readString();
                    replyDraftBean.accountId = in.readString();
                    replyDraftBean.commentBean = in.readParcelable(CommentBean.class.getClassLoader());
                    replyDraftBean.id = in.readString();
                    return replyDraftBean;
                }

                public ReplyDraftBean[] newArray(int size) {
                    return new ReplyDraftBean[size];
                }
            };


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommentBean getCommentBean() {
        return commentBean;
    }

    public void setCommentBean(CommentBean commentBean) {
        this.commentBean = commentBean;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
