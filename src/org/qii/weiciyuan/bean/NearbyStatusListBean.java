package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-3-8
 */
public class NearbyStatusListBean extends ListBean<MessageBean, NearbyStatusListBean> implements Parcelable {
    private List<MessageBean> statuses = new ArrayList<MessageBean>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(total_number);
        dest.writeString(previous_cursor);
        dest.writeString(next_cursor);

        dest.writeTypedList(statuses);
    }

    public static final Parcelable.Creator<NearbyStatusListBean> CREATOR =
            new Parcelable.Creator<NearbyStatusListBean>() {
                public NearbyStatusListBean createFromParcel(Parcel in) {
                    NearbyStatusListBean nearbyStatusListBean = new NearbyStatusListBean();

                    nearbyStatusListBean.total_number = in.readInt();
                    nearbyStatusListBean.previous_cursor = in.readString();
                    nearbyStatusListBean.next_cursor = in.readString();

                    nearbyStatusListBean.statuses = new ArrayList<MessageBean>();
                    in.readTypedList(nearbyStatusListBean.statuses, MessageBean.CREATOR);

                    return nearbyStatusListBean;
                }

                public NearbyStatusListBean[] newArray(int size) {
                    return new NearbyStatusListBean[size];
                }
            };

    @Override
    public int getSize() {
        return statuses.size();
    }

    @Override
    public MessageBean getItem(int position) {
        return statuses.get(position);
    }

    @Override
    public List<MessageBean> getItemList() {
        return statuses;
    }


    @Override
    public void addNewData(NearbyStatusListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {

            this.getItemList().clear();
            this.getItemList().addAll(newValue.getItemList());
            this.setTotal_number(newValue.getTotal_number());

            this.statuses.clear();
            this.statuses.addAll(newValue.getItemList());
        }
    }

    @Override
    public void addOldData(NearbyStatusListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 0) {
            getItemList().addAll(oldValue.getItemList());
            setTotal_number(oldValue.getTotal_number());
            this.statuses.addAll(oldValue.getItemList());
        }
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}

