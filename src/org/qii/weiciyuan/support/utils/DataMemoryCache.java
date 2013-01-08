package org.qii.weiciyuan.support.utils;

import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;

import java.util.List;

/**
 * User: qii
 * Date: 13-1-6
 */
public class DataMemoryCache {

    private static MessageListBean friendTimeLineData = new MessageListBean();
    private static MessageListBean statusByIdTimeLineData = new MessageListBean();


    public static MessageListBean getFriendsTimeLineData() {
        return friendTimeLineData;
    }

    public static MessageListBean getStatusByIdTimeLineData() {
        return statusByIdTimeLineData;
    }

    public static void clearStatusByIdTimeLineData() {
        statusByIdTimeLineData = new MessageListBean();
    }

    public static void clearFriendsTimeLineData() {
        friendTimeLineData = new MessageListBean();
    }

    public static void update(MessageBean msg) {

        updateTimeLineDataCommentCount(msg, msg.getComments_count());
        updateTimeLineDataRepostCount(msg, msg.getReposts_count());
    }

    public static void updateTimeLineDataCommentCount(MessageBean msg, int commentCount) {
        if (msg == null) {
            return;
        }

        List<MessageBean> msgList = friendTimeLineData.getItemList();
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i) != null && msgList.get(i).equals(msg)) {
                msgList.get(i).setComments_count(commentCount);

                break;
            }
        }

        msgList = statusByIdTimeLineData.getItemList();
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i) != null && msgList.get(i).equals(msg)) {
                msgList.get(i).setComments_count(commentCount);
                break;
            }
        }
    }

    public static void updateTimeLineDataRepostCount(MessageBean msg, int repostCount) {
        if (msg == null) {
            return;
        }

        List<MessageBean> msgList = friendTimeLineData.getItemList();
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i).equals(msg)) {
                msgList.get(i).setReposts_count(repostCount);
                break;
            }
        }

        msgList = statusByIdTimeLineData.getItemList();
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i).equals(msg)) {
                msgList.get(i).setReposts_count(repostCount);
                break;
            }
        }
    }
}
