package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-9-26
 */
public class TopicResultListBean extends ListBean<MessageBean, TopicResultListBean> implements Parcelable {


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

    public static final Parcelable.Creator<TopicResultListBean> CREATOR =
            new Parcelable.Creator<TopicResultListBean>() {
                public TopicResultListBean createFromParcel(Parcel in) {
                    TopicResultListBean topicResultListBean = new TopicResultListBean();

                    topicResultListBean.total_number = in.readInt();
                    topicResultListBean.previous_cursor = in.readString();
                    topicResultListBean.next_cursor = in.readString();

                    topicResultListBean.statuses = new ArrayList<MessageBean>();
                    in.readTypedList(topicResultListBean.statuses, MessageBean.CREATOR);

                    return topicResultListBean;
                }

                public TopicResultListBean[] newArray(int size) {
                    return new TopicResultListBean[size];
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


    public List<MessageBean> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<MessageBean> statuses) {
        this.statuses = statuses;
    }

    @Override
    public void addNewData(TopicResultListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {

            this.getItemList().clear();
            this.getItemList().addAll(newValue.getItemList());
            this.setTotal_number(newValue.getTotal_number());


        }
    }

    @Override
    public void addOldData(TopicResultListBean oldValue) {
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
