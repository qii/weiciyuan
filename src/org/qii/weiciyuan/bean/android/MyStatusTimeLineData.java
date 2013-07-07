package org.qii.weiciyuan.bean.android;

import org.qii.weiciyuan.bean.MessageListBean;

/**
 * User: qii
 * Date: 13-7-7
 */
public class MyStatusTimeLineData {

    public MessageListBean msgList;
    public TimeLinePosition position;

    public MyStatusTimeLineData(MessageListBean msgList, TimeLinePosition position) {
        this.msgList = msgList;
        this.position = position;
    }
}
