package org.qii.weiciyuan.support.database.draftbean;

import org.qii.weiciyuan.bean.CommentBean;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-10-21
 */
public class ReplyDraftBean implements Serializable {
    private String content;

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

    private String accountId;
    private CommentBean commentBean;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
