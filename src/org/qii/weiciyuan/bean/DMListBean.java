package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-11
 */
public class DMListBean extends ListBean<DMBean, DMListBean> implements Parcelable {
    private List<DMBean> direct_messages = new ArrayList<DMBean>();


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(total_number);
        dest.writeString(previous_cursor);
        dest.writeString(next_cursor);

        dest.writeTypedList(direct_messages);
    }

    public static final Parcelable.Creator<DMListBean> CREATOR =
            new Parcelable.Creator<DMListBean>() {
                public DMListBean createFromParcel(Parcel in) {
                    DMListBean dmListBean = new DMListBean();

                    dmListBean.total_number = in.readInt();
                    dmListBean.previous_cursor = in.readString();
                    dmListBean.next_cursor = in.readString();

                    dmListBean.direct_messages = new ArrayList<DMBean>();
                    in.readTypedList(dmListBean.direct_messages, DMBean.CREATOR);

                    return dmListBean;
                }

                public DMListBean[] newArray(int size) {
                    return new DMListBean[size];
                }
            };


    public List<DMBean> getDirect_messages() {
        return direct_messages;
    }

    public void setDirect_messages(List<DMBean> direct_messages) {
        this.direct_messages = direct_messages;
    }

    @Override
    public int getSize() {
        return direct_messages.size();
    }


    @Override
    public DMBean getItem(int position) {
        return direct_messages.get(position);
    }

    @Override
    public List<DMBean> getItemList() {
        return direct_messages;
    }

    @Override
    public void addNewData(DMListBean newValue) {
        getItemList().clear();
        getItemList().addAll(newValue.getItemList());
        setTotal_number(newValue.getTotal_number());
    }

    @Override
    public void addOldData(DMListBean oldValue) {
        setTotal_number(oldValue.getTotal_number());
        getItemList().addAll(oldValue.getItemList());
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
