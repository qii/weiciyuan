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
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class CommentListBean extends ListBean<CommentBean, CommentListBean> implements Parcelable {

    private List<CommentBean> comments = new ArrayList<CommentBean>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(total_number);
        dest.writeString(previous_cursor);
        dest.writeString(next_cursor);

        dest.writeTypedList(comments);
    }

    public static final Parcelable.Creator<CommentListBean> CREATOR =
            new Parcelable.Creator<CommentListBean>() {
                public CommentListBean createFromParcel(Parcel in) {
                    CommentListBean commentListBean = new CommentListBean();

                    commentListBean.total_number = in.readInt();
                    commentListBean.previous_cursor = in.readString();
                    commentListBean.next_cursor = in.readString();

                    commentListBean.comments = new ArrayList<CommentBean>();
                    in.readTypedList(commentListBean.comments, CommentBean.CREATOR);

                    return commentListBean;
                }

                public CommentListBean[] newArray(int size) {
                    return new CommentListBean[size];
                }
            };


    private List<CommentBean> getComments() {
        return comments;
    }

    public void setComments(List<CommentBean> comments) {
        this.comments = comments;
    }


    @Override
    public CommentBean getItem(int position) {
        return getComments().get(position);
    }

    @Override
    public List<CommentBean> getItemList() {
        return getComments();
    }

    @Override
    public int getSize() {
        return comments.size();
    }

    @Override
    public void addNewData(CommentListBean newValue) {

        if (newValue == null || newValue.getSize() == 0) {
            return;
        }

        boolean receivedCountBelowRequestCount = newValue.getSize() < Integer.valueOf(SettingUtility.getMsgCount());
        boolean receivedCountEqualRequestCount = newValue.getSize() == Integer.valueOf(SettingUtility.getMsgCount());
        if (receivedCountEqualRequestCount && this.getSize() > 0) {
            newValue.getItemList().add(null);
        }
        this.getItemList().addAll(0, newValue.getItemList());
        this.setTotal_number(newValue.getTotal_number());
    }

    public void addMiddleData(int position, CommentListBean middleValue, boolean towardsBottom) {
        if (middleValue == null)
            return;

        if (middleValue.getSize() == 0 || middleValue.getSize() == 1) {
            getItemList().remove(position);
            return;
        }

        List<CommentBean> middleData = middleValue.getItemList().subList(1, middleValue.getSize());

        String beginId = getItem(position + 1).getId();
        String endId = getItem(position - 1).getId();
        Iterator<CommentBean> iterator = middleData.iterator();
        while (iterator.hasNext()) {
            CommentBean msg = iterator.next();
            boolean notNull = !TextUtils.isEmpty(msg.getId());
            if (notNull) {
                if (msg.getId().equals(beginId) || msg.getId().equals(endId)) {
                    iterator.remove();
                }
            }
        }

        getItemList().addAll(position, middleData);

    }

    @Override
    public void addOldData(CommentListBean oldValue) {

        if (oldValue != null && oldValue.getItemList().size() > 1) {
            List<CommentBean> list = oldValue.getItemList();
            getItemList().addAll(list.subList(1, list.size()));
            setTotal_number(oldValue.getTotal_number());
        }
    }

    public void replaceAll(CommentListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            setTotal_number(newValue.getTotal_number());
            getItemList().clear();
            getItemList().addAll(newValue.getItemList());
        }
    }

    public void clear() {
        setTotal_number(0);
        getItemList().clear();
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
