package org.qii.weiciyuan.bean;

import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.TimeTool;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class CommentBean extends ItemBean {
    private String created_at;
    private String id;
    private String text;
    private String source;
    private String mid;
    private UserBean user;
    private MessageBean status;
    private CommentBean reply_comment;

    public CommentBean getReply_comment() {
        return reply_comment;
    }

    public void setReply_comment(CommentBean reply_comment) {
        this.reply_comment = reply_comment;
    }

    private transient SpannableString listViewSpannableString;

    private transient SpannableString listViewReplySpannableString;

    //comment timeline show the comment content which is replied to
    public SpannableString getListViewReplySpannableString() {
        if (!TextUtils.isEmpty(listViewReplySpannableString)) {
            return listViewReplySpannableString;
        } else {
            ListViewTool.addJustHighLightLinksOnlyReplyComment(this);

            return listViewReplySpannableString;
        }
    }

    public void setListViewReplySpannableString(SpannableString listViewReplySpannableString) {
        this.listViewReplySpannableString = listViewReplySpannableString;
    }

    //comment timeline show comment
    public SpannableString getListViewSpannableString() {
        if (!TextUtils.isEmpty(listViewSpannableString)) {
            return listViewSpannableString;
        } else {
            ListViewTool.addJustHighLightLinks(this);
            if (reply_comment != null)
                reply_comment.getListViewReplySpannableString();
            return listViewSpannableString;
        }
    }

    public void setListViewSpannableString(SpannableString listViewSpannableString) {
        this.listViewSpannableString = listViewSpannableString;
    }

    private long mills;

    public long getMills() {
        return mills;
    }

    public void setMills(long mills) {
        this.mills = mills;
    }

    public String getCreated_at() {

        return created_at;
    }

    public String getListviewItemShowTime() {
        return TimeTool.getListTime(this);
    }


    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
