package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.AppConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-7-29
 */
public class MessageListBean extends ListBean<MessageBean> {

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


    public void addNewData(MessageListBean newValue) {
        if (newValue != null) {
            if (newValue.getSize() == 0) {

            } else if (newValue.getSize() > 0) {
                if (newValue.getItemList().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    //for speed, add old data after new data
                    newValue.getItemList().addAll(getItemList());
                } else {
                    //null is flag means this position has some old messages which dont appear
                    if (getSize() > 0) {
                        newValue.getItemList().add(null);
                    }
                    newValue.getItemList().addAll(this.getItemList());
                }
                this.getItemList().clear();
                this.getItemList().addAll(newValue.getItemList());
                this.setTotal_number(newValue.getTotal_number());


            }
        }
    }

    public void addOldData(MessageListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 1) {
            getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
            setTotal_number(oldValue.getTotal_number());

        }
    }
}