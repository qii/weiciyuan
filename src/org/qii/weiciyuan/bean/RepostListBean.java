package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.AppConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-7
 */
public class RepostListBean extends ListBean<MessageBean,RepostListBean> {

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
    @Override
    public void addOldData(RepostListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 1) {
            getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
            setTotal_number(oldValue.getTotal_number());

        }
    }
}