package org.qii.weiciyuan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-10-17
 */
public class GroupListBean implements Serializable {

    private List<GroupBean> lists = new ArrayList<GroupBean>();
    private String total_number = "0";


    public List<GroupBean> getLists() {
        return lists;
    }

    public void setLists(List<GroupBean> lists) {
        this.lists = lists;
    }

    public String getTotal_number() {
        return total_number;
    }

    public void setTotal_number(String total_number) {
        this.total_number = total_number;
    }
}
