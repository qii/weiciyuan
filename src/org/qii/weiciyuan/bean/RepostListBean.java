package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-7
  */
public class RepostListBean extends ListBean {

    private List<MessageBean> reposts = new ArrayList<MessageBean>();
    private String previous_cursor = "";
    private String next_cursor = "0";

    public List<MessageBean> getReposts() {
        return reposts;
    }

    public void setReposts(List<MessageBean> reposts) {
        this.reposts = reposts;
    }

    public String getPrevious_cursor() {
        return previous_cursor;
    }

    public void setPrevious_cursor(String previous_cursor) {
        this.previous_cursor = previous_cursor;
    }

    public String getNext_cursor() {
        return next_cursor;
    }

    public void setNext_cursor(String next_cursor) {
        this.next_cursor = next_cursor;
    }

    @Override
    public int getSize() {
        return getReposts().size();
    }

    public int getTotal_number() {
        return total_number;
    }

    public void setTotal_number(int total_number) {
        this.total_number = total_number;
    }
}