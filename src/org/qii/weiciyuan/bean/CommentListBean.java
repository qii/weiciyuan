package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class CommentListBean extends ListBean<CommentBean, CommentListBean> {

    private List<CommentBean> comments = new ArrayList<CommentBean>();


    private List<CommentBean> getComments() {
        return comments;
    }

    public void setComments(List<CommentBean> comments) {
        this.comments = comments;
    }


    @Override
    public CommentBean getItem(int position) {
        return getComments().get(position);
    }

    @Override
    public List<CommentBean> getItemList() {
        return getComments();
    }

    @Override
    public int getSize() {
        return comments.size();
    }

    @Override
    public void addNewData(CommentListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            setTotal_number(newValue.getTotal_number());
            getItemList().clear();
            getItemList().addAll(newValue.getItemList());
        }

    }

    @Override
    public void addOldData(CommentListBean oldValue) {

        if (oldValue != null && oldValue.getItemList().size() > 1) {
            List<CommentBean> list = oldValue.getItemList();
            getItemList().addAll(list.subList(1, list.size()));
            setTotal_number(oldValue.getTotal_number());
        }
    }

    public void replaceAll(CommentListBean newValue) {
        addNewData(newValue);
    }

    public void clear() {
        setTotal_number(0);
        getItemList().clear();
    }
}
