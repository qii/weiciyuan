package org.qii.weiciyuan.support.database.draftbean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.bean.MessageBean;

/**
 * User: qii
 * Date: 12-10-21
 */
public class CommentDraftBean implements Parcelable {
    private String content;
    private String accountId;
    private MessageBean messageBean;
    private String id;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(accountId);
        dest.writeParcelable(messageBean, flags);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<CommentDraftBean> CREATOR =
            new Parcelable.Creator<CommentDraftBean>() {
                public CommentDraftBean createFromParcel(Parcel in) {
                    CommentDraftBean commentDraftBean = new CommentDraftBean();
                    commentDraftBean.content = in.readString();
                    commentDraftBean.accountId = in.readString();
                    commentDraftBean.messageBean = in.readParcelable(MessageBean.class.getClassLoader());
                    commentDraftBean.id = in.readString();
                    return commentDraftBean;
                }

                public CommentDraftBean[] newArray(int size) {
                    return new CommentDraftBean[size];
                }
            };

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public MessageBean getMessageBean() {
        return messageBean;
    }

    public void setMessageBean(MessageBean messageBean) {
        this.messageBean = messageBean;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
