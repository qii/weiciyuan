package org.qii.weiciyuan.bean;

import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.TimeTool;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserBean extends ItemBean {
    private UserBean user;
    private DMBean direct_message;
    private int unread_count;

    private long mills;
    private transient SpannableString listViewSpannableString;

    @Override
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

    public String getListviewItemShowTime() {
        return TimeTool.getListTime(this);
    }

    @Override
    public String getText() {
        return direct_message.getText();
    }

    @Override
    public String getCreated_at() {
        return direct_message.getCreated_at();
    }


    public long getMills() {
        return mills;
    }

    public void setMills(long mills) {
        this.mills = mills;
    }

    @Override
    public String getId() {
        return direct_message.getId();
    }

    @Override
    public long getIdLong() {
        return Long.valueOf(getId());
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public DMBean getDirect_message() {
        return direct_message;
    }

    public void setDirect_message(DMBean direct_message) {
        this.direct_message = direct_message;
    }

    public int getUnread_count() {
        return unread_count;
    }

    public void setUnread_count(int unread_count) {
        this.unread_count = unread_count;
    }
}
