package org.qii.weiciyuan.support.database.draftbean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: qii
 * Date: 12-10-21
 */
public class DraftListViewItemBean implements Parcelable {
    private CommentDraftBean commentDraftBean;
    private ReplyDraftBean replyDraftBean;
    private RepostDraftBean repostDraftBean;
    private StatusDraftBean statusDraftBean;
    private int type;
    private String id;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(commentDraftBean, flags);
        dest.writeParcelable(replyDraftBean, flags);
        dest.writeParcelable(repostDraftBean, flags);
        dest.writeParcelable(statusDraftBean, flags);
        dest.writeInt(type);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<DraftListViewItemBean> CREATOR =
            new Parcelable.Creator<DraftListViewItemBean>() {
                public DraftListViewItemBean createFromParcel(Parcel in) {
                    DraftListViewItemBean draftListViewItemBean = new DraftListViewItemBean();
                    draftListViewItemBean.commentDraftBean = in.readParcelable(CommentDraftBean.class.getClassLoader());
                    draftListViewItemBean.replyDraftBean = in.readParcelable(ReplyDraftBean.class.getClassLoader());
                    draftListViewItemBean.repostDraftBean = in.readParcelable(RepostDraftBean.class.getClassLoader());
                    draftListViewItemBean.statusDraftBean = in.readParcelable(StatusDraftBean.class.getClassLoader());

                    draftListViewItemBean.type = in.readInt();
                    draftListViewItemBean.id = in.readString();
                    return draftListViewItemBean;
                }

                public DraftListViewItemBean[] newArray(int size) {
                    return new DraftListViewItemBean[size];
                }
            };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CommentDraftBean getCommentDraftBean() {
        return commentDraftBean;
    }

    public void setCommentDraftBean(CommentDraftBean commentDraftBean) {
        this.commentDraftBean = commentDraftBean;
    }

    public ReplyDraftBean getReplyDraftBean() {
        return replyDraftBean;
    }

    public void setReplyDraftBean(ReplyDraftBean replyDraftBean) {
        this.replyDraftBean = replyDraftBean;
    }

    public RepostDraftBean getRepostDraftBean() {
        return repostDraftBean;
    }

    public void setRepostDraftBean(RepostDraftBean repostDraftBean) {
        this.repostDraftBean = repostDraftBean;
    }

    public StatusDraftBean getStatusDraftBean() {
        return statusDraftBean;
    }

    public void setStatusDraftBean(StatusDraftBean statusDraftBean) {
        this.statusDraftBean = statusDraftBean;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
