package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-7
 */
public class RepostListBean extends ListBean<MessageBean> {

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

}