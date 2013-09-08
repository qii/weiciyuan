package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-23
 */
public class SearchStatusListBean extends ListBean<MessageBean, SearchStatusListBean> implements Parcelable {
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

    public static final Parcelable.Creator<SearchStatusListBean> CREATOR =
            new Parcelable.Creator<SearchStatusListBean>() {
                public SearchStatusListBean createFromParcel(Parcel in) {
                    SearchStatusListBean searchStatusListBean = new SearchStatusListBean();

                    searchStatusListBean.total_number = in.readInt();
                    searchStatusListBean.previous_cursor = in.readString();
                    searchStatusListBean.next_cursor = in.readString();

                    searchStatusListBean.statuses = new ArrayList<MessageBean>();
                    in.readTypedList(searchStatusListBean.statuses, MessageBean.CREATOR);

                    return searchStatusListBean;
                }

                public SearchStatusListBean[] newArray(int size) {
                    return new SearchStatusListBean[size];
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
    public void addNewData(SearchStatusListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {

            this.getItemList().clear();
            this.getItemList().addAll(newValue.getItemList());
            this.setTotal_number(newValue.getTotal_number());


        }
    }

    @Override
    public void addOldData(SearchStatusListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 0) {
            getItemList().addAll(oldValue.getItemList());
            setTotal_number(oldValue.getTotal_number());

        }
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}