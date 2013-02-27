package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-2-27
 */
public class ShareListBean extends ListBean<MessageBean, ShareListBean> {
    private String url_long;
    private String url_short;
    private List<MessageBean> share_statuses = new ArrayList<MessageBean>();

    public String getUrl_long() {
        return url_long;
    }

    public void setUrl_long(String url_long) {
        this.url_long = url_long;
    }

    public String getUrl_short() {
        return url_short;
    }

    public void setUrl_short(String url_short) {
        this.url_short = url_short;
    }

    public List<MessageBean> getShare_statuses() {
        return share_statuses;
    }

    public void setShare_statuses(List<MessageBean> share_statuses) {
        this.share_statuses = share_statuses;
    }

    @Override
    public int getSize() {
        return share_statuses.size();
    }

    @Override
    public MessageBean getItem(int position) {
        return share_statuses.get(position);
    }

    @Override
    public List<MessageBean> getItemList() {
        return share_statuses;
    }

    @Override
    public void addNewData(ShareListBean newValue) {
        if (newValue == null)
            return;
        getItemList().clear();
        getItemList().addAll(newValue.getItemList());
        setTotal_number(newValue.getTotal_number());
    }

    @Override
    public void addOldData(ShareListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 1) {
            getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
            setTotal_number(oldValue.getTotal_number());
        }
    }
}
