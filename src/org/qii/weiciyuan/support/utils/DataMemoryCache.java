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

    public static MessageListBean getFriendsTimeLineData() {
        return friendTimeLineData;
    }

    public static MessageListBean update(MessageBean msg) {

        updateTimeLineDataCommentCount(msg, msg.getComments_count());
        updateTimeLineDataRepostCount(msg, msg.getReposts_count());
        return friendTimeLineData;
    }

    public static MessageListBean updateTimeLineDataCommentCount(MessageBean msg, int commentCount) {
        if (msg == null) {
            return friendTimeLineData;
        }

        List<MessageBean> msgList = friendTimeLineData.getItemList();
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i).equals(msg)) {
                msgList.get(i).setComments_count(commentCount);
                break;
            }
        }
        return friendTimeLineData;
    }

    public static MessageListBean updateTimeLineDataRepostCount(MessageBean msg, int repostCount) {
        if (msg == null) {
            return friendTimeLineData;
        }

        List<MessageBean> msgList = friendTimeLineData.getItemList();
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i).equals(msg)) {
                msgList.get(i).setReposts_count(repostCount);
                break;
            }
        }
        return friendTimeLineData;
    }
}
