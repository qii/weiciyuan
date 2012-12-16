package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.settinghelper.SettingUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-7-29
 */
public class MessageListBean extends ListBean<MessageBean, MessageListBean> {

    private List<MessageBean> statuses = new ArrayList<MessageBean>();


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

    private int removedCount = 0;

    public int getReceivedNumber() {
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

        boolean receivedCountBelowRequestCount = newValue.getReceivedNumber() < Integer.valueOf(SettingUtility.getMsgCount());
        boolean receivedCountEqualRequestCount = newValue.getReceivedNumber() == Integer.valueOf(SettingUtility.getMsgCount());
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
}