package org.qii.weiciyuan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
  * User: qii
 * Date: 12-7-29
   */
public class MessageListBean implements Serializable{
    public List<MessageBean> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<MessageBean> statuses) {
        this.statuses = statuses;
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

    public String getTotal_number() {
        return total_number;
    }

    public void setTotal_number(String total_number) {
        this.total_number = total_number;
    }

    private List<MessageBean> statuses = new ArrayList<MessageBean>();
    private String previous_cursor = "";
    private String next_cursor = "0";
    private String total_number = "";

}
