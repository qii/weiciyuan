package org.qii.weiciyuan.support.database.draftbean;

import org.qii.weiciyuan.bean.MessageBean;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-10-21
 */
public class RepostDraftBean implements Serializable {
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

    private String content;
    private MessageBean messageBean;
    private String accountId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
