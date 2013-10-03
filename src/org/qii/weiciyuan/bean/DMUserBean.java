package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.TextUtils;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;
import org.qii.weiciyuan.support.utils.TimeUtility;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserBean extends ItemBean implements Parcelable {
    private int unread_count;
    private long mills;

    private UserBean user;
    private DMBean direct_message;

    private transient SpannableString listViewSpannableString;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(unread_count);
        dest.writeLong(mills);

        dest.writeParcelable(user, flags);
        dest.writeParcelable(direct_message, flags);

    }

    public static final Parcelable.Creator<DMUserBean> CREATOR =
            new Parcelable.Creator<DMUserBean>() {
                public DMUserBean createFromParcel(Parcel in) {
                    DMUserBean dmUserBean = new DMUserBean();

                    dmUserBean.unread_count = in.readInt();
                    dmUserBean.mills = in.readLong();

                    dmUserBean.user = in.readParcelable(UserBean.class.getClassLoader());
                    dmUserBean.direct_message = in.readParcelable(DMBean.class.getClassLoader());

                    return dmUserBean;
                }

                public DMUserBean[] newArray(int size) {
                    return new DMUserBean[size];
                }
            };

    @Override
    public SpannableString getListViewSpannableString() {
        if (!TextUtils.isEmpty(listViewSpannableString)) {
            return listViewSpannableString;
        } else {
            TimeLineUtility.addJustHighLightLinks(this);

            return listViewSpannableString;
        }
    }

    public void setListViewSpannableString(SpannableString listViewSpannableString) {
        this.listViewSpannableString = listViewSpannableString;
    }

    public String getListviewItemShowTime() {
        return TimeUtility.getListTime(this);
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

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
