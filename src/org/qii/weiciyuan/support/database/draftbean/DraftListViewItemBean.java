package org.qii.weiciyuan.support.database.draftbean;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-10-21
 */
public class DraftListViewItemBean implements Serializable{
    private CommentDraftBean commentDraftBean;
    private ReplyDraftBean replyDraftBean;
    private RepostDraftBean repostDraftBean;
    private StatusDraftBean statusDraftBean;
    private int type;
    private String id;

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
