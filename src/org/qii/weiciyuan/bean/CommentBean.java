package org.qii.weiciyuan.bean;

import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;
import org.qii.weiciyuan.support.utils.TimeTool;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class CommentBean extends ItemBean {
    private String created_at;
    private long id;
    private String idstr;
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

    //comment timeline show comment
    public SpannableString getListViewSpannableString() {
        if (!TextUtils.isEmpty(listViewSpannableString)) {
            return listViewSpannableString;
        } else {
            ListViewTool.addJustHighLightLinks(this);
            return listViewSpannableString;
        }
    }

    public void setListViewSpannableString(SpannableString listViewSpannableString) {
        this.listViewSpannableString = listViewSpannableString;
    }

    private long mills;

    public long getMills() {
        if (mills == 0L) {
            TimeTool.dealMills(this);
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
        return TimeTool.getListTime(this);
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
