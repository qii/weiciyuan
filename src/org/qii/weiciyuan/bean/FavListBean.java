package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-8-18
 */
public class FavListBean extends ListBean<MessageBean, FavListBean> implements Parcelable {
    private List<FavBean> favorites = new ArrayList<FavBean>();
    private List<MessageBean> actualStore = null;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(total_number);
        dest.writeString(previous_cursor);
        dest.writeString(next_cursor);

        dest.writeTypedList(favorites);
        dest.writeTypedList(actualStore);
    }

    public static final Parcelable.Creator<FavListBean> CREATOR =
            new Parcelable.Creator<FavListBean>() {
                public FavListBean createFromParcel(Parcel in) {
                    FavListBean favListBean = new FavListBean();

                    favListBean.total_number = in.readInt();
                    favListBean.previous_cursor = in.readString();
                    favListBean.next_cursor = in.readString();

                    favListBean.favorites = new ArrayList<FavBean>();
                    in.readTypedList(favListBean.favorites, FavBean.CREATOR);

                    favListBean.actualStore = new ArrayList<MessageBean>();
                    in.readTypedList(favListBean.actualStore, MessageBean.CREATOR);

                    return favListBean;
                }

                public FavListBean[] newArray(int size) {
                    return new FavListBean[size];
                }
            };


    public List<FavBean> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavBean> favorites) {
        this.favorites = favorites;
    }

    @Override
    public int getSize() {
        return favorites.size();
    }


    @Override
    public MessageBean getItem(int position) {
        return favorites.get(position).getStatus();
    }

    @Override
    public List<MessageBean> getItemList() {
        if (actualStore == null) {
            actualStore = new ArrayList<MessageBean>();
            for (FavBean b : favorites) {
                actualStore.add(b.getStatus());
            }
        }
        return actualStore;
    }

    public void replaceData(FavListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {

            this.getItemList().clear();
            this.getItemList().addAll(newValue.getItemList());
            this.setTotal_number(newValue.getTotal_number());

            this.favorites.clear();
            this.favorites.addAll(newValue.getFavorites());
        }
    }

    @Override
    public void addNewData(FavListBean newValue) {
        replaceData(newValue);
    }

    @Override
    public void addOldData(FavListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 0) {
            getItemList().addAll(oldValue.getItemList());
            setTotal_number(oldValue.getTotal_number());
            this.favorites.addAll(oldValue.getFavorites());
        }
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
