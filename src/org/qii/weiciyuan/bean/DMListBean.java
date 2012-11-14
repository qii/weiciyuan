package org.qii.weiciyuan.bean;

import java.util.List;

/**
 * User: qii
 * Date: 12-11-11
 */
public class DMListBean {
    private List<DMBean> direct_messages;

    private int total_number;

    public List<DMBean> getDirect_messages() {
        return direct_messages;
    }

    public void setDirect_messages(List<DMBean> direct_messages) {
        this.direct_messages = direct_messages;
    }

    public int getTotal_number() {
        return total_number;
    }

    public void setTotal_number(int total_number) {
        this.total_number = total_number;
    }
}
