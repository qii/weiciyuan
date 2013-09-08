package org.qii.weiciyuan.bean.android;

import org.qii.weiciyuan.bean.MessageListBean;

/**
 * User: qii
 * Date: 13-3-23
 */
public class MessageTimeLineData {
    public MessageListBean msgList;
    public TimeLinePosition position;
    public String groupId;

    public MessageTimeLineData(String groupId, MessageListBean msgList, TimeLinePosition position) {
        this.groupId = groupId;
        this.msgList = msgList;
        this.position = position;
    }
}
