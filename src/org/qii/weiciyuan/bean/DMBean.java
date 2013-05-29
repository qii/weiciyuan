package org.qii.weiciyuan.bean;

import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;
import org.qii.weiciyuan.support.utils.TimeTool;

/**
 * User: qii
 * Date: 12-11-11
 */
public class DMBean extends ItemBean {

    private long mills;
    private transient SpannableString listViewSpannableString;

    public String getId() {
        return idstr;
    }

    @Override
    public UserBean getUser() {
        return sender;
    }

    public void setId(String id) {
        this.idstr = id;
    }

    public long getIdLong() {
        return id;
    }

    public void setIdstr(String idstr) {
        this.idstr = idstr;
    }

    public String getCreated_at() {
        return created_at;
    }

    public long getMills() {
        return mills;
    }

    public void setMills(long mills) {
        this.mills = mills;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public SpannableString getListViewSpannableString() {
        if (!TextUtils.isEmpty(listViewSpannableString)) {
            return listViewSpannableString;
        } else {
            ListViewTool.addJustHighLightLinks(this);

            return listViewSpannableString;
        }
    }

    public String getListviewItemShowTime() {
        return TimeTool.getListTime(this);
    }

    public void setListViewSpannableString(SpannableString listViewSpannableString) {
        this.listViewSpannableString = listViewSpannableString;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getRecipient_id() {
        return recipient_id;
    }

    public void setRecipient_id(String recipient_id) {
        this.recipient_id = recipient_id;
    }

    public String getSender_screen_name() {
        return sender_screen_name;
    }

    public void setSender_screen_name(String sender_screen_name) {
        this.sender_screen_name = sender_screen_name;
    }

    public String getRecipient_screen_name() {
        return recipient_screen_name;
    }

    public void setRecipient_screen_name(String recipient_screen_name) {
        this.recipient_screen_name = recipient_screen_name;
    }

    public UserBean getSender() {
        return sender;
    }

    public void setSender(UserBean sender) {
        this.sender = sender;
    }

    public UserBean getRecipient() {
        return recipient;
    }

    public void setRecipient(UserBean recipient) {
        this.recipient = recipient;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    private long id;
    private String idstr;
    private String created_at;
    private String text;
    private String sender_id;
    private String recipient_id;
    private String sender_screen_name;
    private String recipient_screen_name;
    private UserBean sender;
    private UserBean recipient;
    private String mid;
    private String source;
    private String status_id;
    private String geo;

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
