package org.qii.weiciyuan.bean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午8:22
 * To change this template use File | Settings | File Templates.
 */
public class TimeLineMsgList {
    public List<WeiboMsg> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<WeiboMsg> statuses) {
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

    private List<WeiboMsg> statuses;
    private String previous_cursor;
    private String next_cursor;
    private String total_number;

}
