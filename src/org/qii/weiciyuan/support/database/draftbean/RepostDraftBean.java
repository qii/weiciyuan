package org.qii.weiciyuan.support.database.draftbean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.bean.MessageBean;

/**
 * User: qii
 * Date: 12-10-21
 */
public class RepostDraftBean implements Parcelable {

    private String content;
    private MessageBean messageBean;
    private String accountId;
    private String id;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeParcelable(messageBean, flags);
        dest.writeString(accountId);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<RepostDraftBean> CREATOR =
            new Parcelable.Creator<RepostDraftBean>() {
                public RepostDraftBean createFromParcel(Parcel in) {
                    RepostDraftBean repostDraftBean = new RepostDraftBean();
                    repostDraftBean.content = in.readString();
                    repostDraftBean.messageBean = in.readParcelable(MessageBean.class.getClassLoader());
                    repostDraftBean.accountId = in.readString();
                    repostDraftBean.id = in.readString();
                    return repostDraftBean;
                }

                public RepostDraftBean[] newArray(int size) {
                    return new RepostDraftBean[size];
                }
            };

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public MessageBean getMessageBean() {
        return messageBean;
    }

    public void setMessageBean(MessageBean messageBean) {
        this.messageBean = messageBean;
    }


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
