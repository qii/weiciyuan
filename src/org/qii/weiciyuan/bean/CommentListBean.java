package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class CommentListBean extends ListBean<CommentBean> {

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
}
