package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class UserListBean implements Parcelable {

    private List<UserBean> users = new ArrayList<UserBean>();
    private int previous_cursor = 0;
    private int next_cursor = 0;
    private int total_number = 0;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(total_number);
        dest.writeInt(previous_cursor);
        dest.writeInt(next_cursor);

        dest.writeTypedList(users);
    }

    public static final Parcelable.Creator<UserListBean> CREATOR =
            new Parcelable.Creator<UserListBean>() {
                public UserListBean createFromParcel(Parcel in) {
                    UserListBean userListBean = new UserListBean();

                    userListBean.total_number = in.readInt();
                    userListBean.previous_cursor = in.readInt();
                    userListBean.next_cursor = in.readInt();

                    userListBean.users = new ArrayList<UserBean>();
                    in.readTypedList(userListBean.users, UserBean.CREATOR);

                    return userListBean;
                }

                public UserListBean[] newArray(int size) {
                    return new UserListBean[size];
                }
            };

    public List<UserBean> getUsers() {
        return users;
    }

    public void setUsers(List<UserBean> users) {
        this.users = users;
    }

    public int getPrevious_cursor() {
        return previous_cursor;
    }

    public void setPrevious_cursor(int previous_cursor) {
        this.previous_cursor = previous_cursor;
    }

    public int getNext_cursor() {
        return next_cursor;
    }

    public void setNext_cursor(int next_cursor) {
        this.next_cursor = next_cursor;
    }

    public int getTotal_number() {
        return total_number;
    }

    public void setTotal_number(int total_number) {
        this.total_number = total_number;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
