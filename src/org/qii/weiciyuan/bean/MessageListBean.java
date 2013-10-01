package org.qii.weiciyuan.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: qii
 * Date: 12-7-29
 */
public class MessageListBean extends ListBean<MessageBean, MessageListBean> implements Parcelable {

    private List<MessageBean> statuses = new ArrayList<MessageBean>();
    private int removedCount = 0;

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
        dest.writeInt(removedCount);
    }

    public static final Parcelable.Creator<MessageListBean> CREATOR =
            new Parcelable.Creator<MessageListBean>() {
                public MessageListBean createFromParcel(Parcel in) {
                    MessageListBean messageListBean = new MessageListBean();

                    messageListBean.total_number = in.readInt();
                    messageListBean.previous_cursor = in.readString();
                    messageListBean.next_cursor = in.readString();

                    messageListBean.statuses = new ArrayList<MessageBean>();
                    in.readTypedList(messageListBean.statuses, MessageBean.CREATOR);

                    messageListBean.removedCount = in.readInt();

                    return messageListBean;
                }

                public MessageListBean[] newArray(int size) {
                    return new MessageListBean[size];
                }
            };


    private List<MessageBean> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<MessageBean> statuses) {
        this.statuses = statuses;
    }


    @Override
    public int getSize() {
        return statuses.size();
    }

    @Override
    public MessageBean getItem(int position) {
        return getStatuses().get(position);
    }

    @Override
    public List<MessageBean> getItemList() {
        return getStatuses();
    }


    public int getReceivedCount() {
        return getSize() + removedCount;
    }

    public void removedCountPlus() {
        removedCount++;
    }

    @Override
    public void addNewData(MessageListBean newValue) {

        if (newValue == null || newValue.getSize() == 0) {
            return;
        }

        boolean receivedCountBelowRequestCount = newValue.getReceivedCount() < Integer.valueOf(SettingUtility.getMsgCount());
        boolean receivedCountEqualRequestCount = newValue.getReceivedCount() >= Integer.valueOf(SettingUtility.getMsgCount());
        if (receivedCountEqualRequestCount && this.getSize() > 0) {
            newValue.getItemList().add(null);
        }
        this.getItemList().addAll(0, newValue.getItemList());
        this.setTotal_number(newValue.getTotal_number());
    }

    @Override
    public void addOldData(MessageListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 1) {
            getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
            setTotal_number(oldValue.getTotal_number());

        }
    }

    public void addMiddleData(int position, MessageListBean middleValue, boolean towardsBottom) {
        if (middleValue == null)
            return;

        if (middleValue.getSize() == 0 || middleValue.getSize() == 1) {
            getItemList().remove(position);
            return;
        }

        List<MessageBean> middleData = middleValue.getItemList().subList(1, middleValue.getSize());

        String beginId = getItem(position + 1).getId();
        String endId = getItem(position - 1).getId();
        Iterator<MessageBean> iterator = middleData.iterator();
        while (iterator.hasNext()) {
            MessageBean msg = iterator.next();
            boolean notNull = !TextUtils.isEmpty(msg.getId());
            if (notNull) {
                if (msg.getId().equals(beginId) || msg.getId().equals(endId)) {
                    iterator.remove();
                }
            }
        }

        getItemList().addAll(position, middleData);

    }

    public void replaceData(MessageListBean value) {
        if (value == null)
            return;
        getItemList().clear();
        getItemList().addAll(value.getItemList());
        setTotal_number(value.getTotal_number());
    }

    public MessageListBean copy() {
        MessageListBean object = new MessageListBean();
        object.replaceData(MessageListBean.this);
        return object;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }


}