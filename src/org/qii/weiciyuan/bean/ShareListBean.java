package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-2-27
 */
public class ShareListBean extends ListBean<MessageBean, ShareListBean> implements Parcelable {
    private String url_long;
    private String url_short;
    private List<MessageBean> share_statuses = new ArrayList<MessageBean>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(total_number);
        dest.writeString(previous_cursor);
        dest.writeString(next_cursor);

        dest.writeString(url_long);
        dest.writeString(url_short);

        dest.writeTypedList(share_statuses);
    }

    public static final Parcelable.Creator<ShareListBean> CREATOR =
            new Parcelable.Creator<ShareListBean>() {
                public ShareListBean createFromParcel(Parcel in) {
                    ShareListBean shareListBean = new ShareListBean();

                    shareListBean.total_number = in.readInt();
                    shareListBean.previous_cursor = in.readString();
                    shareListBean.next_cursor = in.readString();

                    shareListBean.url_long = in.readString();
                    shareListBean.url_short = in.readString();

                    shareListBean.share_statuses = new ArrayList<MessageBean>();
                    in.readTypedList(shareListBean.share_statuses, MessageBean.CREATOR);

                    return shareListBean;
                }

                public ShareListBean[] newArray(int size) {
                    return new ShareListBean[size];
                }
            };


    public String getUrl_long() {
        return url_long;
    }

    public void setUrl_long(String url_long) {
        this.url_long = url_long;
    }

    public String getUrl_short() {
        return url_short;
    }

    public void setUrl_short(String url_short) {
        this.url_short = url_short;
    }

    public List<MessageBean> getShare_statuses() {
        return share_statuses;
    }

    public void setShare_statuses(List<MessageBean> share_statuses) {
        this.share_statuses = share_statuses;
    }

    @Override
    public int getSize() {
        return share_statuses.size();
    }

    @Override
    public MessageBean getItem(int position) {
        return share_statuses.get(position);
    }

    @Override
    public List<MessageBean> getItemList() {
        return share_statuses;
    }

    @Override
    public void addNewData(ShareListBean newValue) {
        if (newValue == null)
            return;
        getItemList().clear();
        getItemList().addAll(newValue.getItemList());
        setTotal_number(newValue.getTotal_number());
    }

    @Override
    public void addOldData(ShareListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 1) {
            getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
            setTotal_number(oldValue.getTotal_number());
        }
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
