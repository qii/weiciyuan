package org.qii.weiciyuan.bean.android;

import org.qii.weiciyuan.bean.MessageListBean;

/**
 * User: qii
 * Date: 13-3-23
 */
public class MessageTimeLineData {
    public MessageListBean msgList;
    public TimeLinePosition position;

    public MessageTimeLineData(MessageListBean msgList, TimeLinePosition position) {
        this.msgList = msgList;
        this.position = position;
    }
}
