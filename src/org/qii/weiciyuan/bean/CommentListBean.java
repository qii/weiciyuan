package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午3:28
 */
public class CommentListBean extends ListBean {
    private List<CommentBean> comments = new ArrayList<CommentBean>();
    private String previous_cursor = "";
    private String next_cursor = "0";


    public List<CommentBean> getComments() {
        return comments;
    }

    public void setComments(List<CommentBean> comments) {
        this.comments = comments;
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

    public int getTotal_number() {
        return total_number;
    }

    public void setTotal_number(int total_number) {
        this.total_number = total_number;
    }

    @Override
    public int getSize() {
        return comments.size();
    }
}
