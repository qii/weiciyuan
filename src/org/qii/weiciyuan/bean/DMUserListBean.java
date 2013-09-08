package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListBean extends ListBean<DMUserBean, DMUserListBean> implements Parcelable {
    private List<DMUserBean> user_list = new ArrayList<DMUserBean>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(total_number);
        dest.writeString(previous_cursor);
        dest.writeString(next_cursor);

        dest.writeTypedList(user_list);
    }

    public static final Parcelable.Creator<DMUserListBean> CREATOR =
            new Parcelable.Creator<DMUserListBean>() {
                public DMUserListBean createFromParcel(Parcel in) {
                    DMUserListBean dmUserListBean = new DMUserListBean();

                    dmUserListBean.total_number = in.readInt();
                    dmUserListBean.previous_cursor = in.readString();
                    dmUserListBean.next_cursor = in.readString();

                    dmUserListBean.user_list = new ArrayList<DMUserBean>();
                    in.readTypedList(dmUserListBean.user_list, DMUserBean.CREATOR);

                    return dmUserListBean;
                }

                public DMUserListBean[] newArray(int size) {
                    return new DMUserListBean[size];
                }
            };


    @Override
    public int getSize() {
        return user_list.size();
    }

    @Override
    public DMUserBean getItem(int position) {
        return user_list.get(position);
    }

    @Override
    public List<DMUserBean> getItemList() {
        return user_list;
    }

    @Override
    public void addNewData(DMUserListBean newValue) {
        getItemList().clear();
        getItemList().addAll(newValue.getItemList());
        this.setTotal_number(newValue.getTotal_number());
        this.setNext_cursor(newValue.getNext_cursor());
        this.setPrevious_cursor(newValue.getPrevious_cursor());
    }

    @Override
    public void addOldData(DMUserListBean oldValue) {
        getItemList().addAll(oldValue.getItemList());
        this.setTotal_number(oldValue.getTotal_number());
        this.setNext_cursor(oldValue.getNext_cursor());
        this.setPrevious_cursor(oldValue.getPrevious_cursor());

    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}

