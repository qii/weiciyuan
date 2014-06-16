package org.qii.weiciyuan.bean.android;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * User: qii
 * Date: 13-3-23
 */
public class TimeLinePosition implements Serializable {

    public TimeLinePosition(int position, int top) {
        this.position = position;
        this.top = top;
    }

    public int position = 0;

    public int top = 0;

    public TreeSet<Long> newMsgIds = null;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append("position:").append(position)
                .append("; top=").append(top);
        return stringBuilder.toString();
    }
}
