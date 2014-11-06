package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-7
 */
public class RepostListBean extends ListBean<MessageBean, RepostListBean> implements Parcelable {

    private List<MessageBean> reposts = new ArrayList<MessageBean>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(total_number);
        dest.writeString(previous_cursor);
        dest.writeString(next_cursor);

        dest.writeTypedList(reposts);
    }

    public static final Parcelable.Creator<RepostListBean> CREATOR =
            new Parcelable.Creator<RepostListBean>() {
                public RepostListBean createFromParcel(Parcel in) {
                    RepostListBean repostListBean = new RepostListBean();

                    repostListBean.total_number = in.readInt();
                    repostListBean.previous_cursor = in.readString();
                    repostListBean.next_cursor = in.readString();

                    repostListBean.reposts = new ArrayList<MessageBean>();
                    in.readTypedList(repostListBean.reposts, MessageBean.CREATOR);

                    return repostListBean;
                }

                public RepostListBean[] newArray(int size) {
                    return new RepostListBean[size];
                }
            };

    private List<MessageBean> getReposts() {
        return reposts;
    }

    public void setReposts(List<MessageBean> reposts) {
        this.reposts = reposts;
    }

    @Override
    public int getSize() {
        return getReposts().size();
    }

    @Override
    public MessageBean getItem(int position) {
        return getReposts().get(position);
    }

    @Override
    public List<MessageBean> getItemList() {
        return getReposts();
    }

    @Override
    public void addNewData(RepostListBean newValue) {
        throw new UnsupportedOperationException("use replaceAll instead");
    }

    @Override
    public void addOldData(RepostListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 1) {
            getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
            setTotal_number(oldValue.getTotal_number());
        }
    }

    public void replaceAll(RepostListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            setTotal_number(newValue.getTotal_number());
            getItemList().clear();
            getItemList().addAll(newValue.getItemList());
        }
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}