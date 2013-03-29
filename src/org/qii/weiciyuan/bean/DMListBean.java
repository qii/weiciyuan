package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-11
 */
public class DMListBean extends ListBean<DMBean, DMListBean> {
    private List<DMBean> direct_messages = new ArrayList<DMBean>();


    public List<DMBean> getDirect_messages() {
        return direct_messages;
    }

    public void setDirect_messages(List<DMBean> direct_messages) {
        this.direct_messages = direct_messages;
    }

    @Override
    public int getSize() {
        return direct_messages.size();
    }


    @Override
    public DMBean getItem(int position) {
        return direct_messages.get(position);
    }

    @Override
    public List<DMBean> getItemList() {
        return direct_messages;
    }

    @Override
    public void addNewData(DMListBean newValue) {
        getItemList().clear();
        getItemList().addAll(newValue.getItemList());
        setTotal_number(newValue.getTotal_number());
    }

    @Override
    public void addOldData(DMListBean oldValue) {
        setTotal_number(oldValue.getTotal_number());
        getItemList().addAll(oldValue.getItemList());
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
