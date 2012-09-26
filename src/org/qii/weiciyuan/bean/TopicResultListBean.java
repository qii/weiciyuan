package org.qii.weiciyuan.bean;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-26
 */
public class TopicResultListBean extends ListBean<MessageBean> {


    private List<MessageBean> statuses;

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
}
