package org.qii.weiciyuan.bean;

/**
 * User: qii
 * Date: 13-2-14
 */
public class MessageReCmtCountBean {

    private String id;
    private int comments;
    private int reposts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getReposts() {
        return reposts;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }
}
