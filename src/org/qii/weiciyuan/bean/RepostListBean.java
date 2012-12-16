package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-7
 */
public class RepostListBean extends ListBean<MessageBean, RepostListBean> {

    private List<MessageBean> reposts = new ArrayList<MessageBean>();

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
}