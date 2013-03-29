package org.qii.weiciyuan.support.utils;

import org.qii.weiciyuan.bean.*;

/**
 * User: qii
 * Date: 13-3-29
 */
public class ObjectToStringUtility {

    public static String toString(AccountBean account) {
        return account.getUsernick();
    }

    public static String toString(AtUserBean user) {
        return String.format("nickname=%s,remark=%s", user.getNickname(), user.getRemark());
    }

    public static String toString(CommentBean comment) {
        UserBean userBean = comment.getUser();
        String username = (userBean != null ? userBean.getScreen_name() : "user is null");
        return String.format("%s @%s:%s", TimeTool.getListTime(comment.getMills()), username, comment.getText());
    }

    public static String toString(CommentListBean commentList) {
        StringBuilder builder = new StringBuilder();
        for (CommentBean comment : commentList.getItemList()) {
            builder.append(comment.toString());
        }
        return builder.toString();
    }

    public static String toString(DMListBean listBean) {
        StringBuilder builder = new StringBuilder();
        for (DMBean data : listBean.getItemList()) {
            builder.append(data.toString());
        }
        return builder.toString();
    }

    public static String toString(MessageBean msg) {
        UserBean userBean = msg.getUser();
        String username = (userBean != null ? userBean.getScreen_name() : "user is null");
        return String.format("%s @%s:%s", TimeTool.getListTime(msg.getMills()), username, msg.getText());
    }

    public static String toString(DMBean dm) {
        UserBean userBean = dm.getUser();
        String username = (userBean != null ? userBean.getScreen_name() : "user is null");
        return String.format("%s @%s:%s", TimeTool.getListTime(dm.getMills()), username, dm.getText());
    }
}
