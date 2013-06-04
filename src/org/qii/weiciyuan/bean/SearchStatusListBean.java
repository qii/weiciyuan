package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-23
 */
public class SearchStatusListBean extends ListBean<MessageBean, SearchStatusListBean> implements Serializable {
    private List<MessageBean> statuses = new ArrayList<MessageBean>();


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