package org.qii.weiciyuan.bean.android;

import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.bean.ListBean;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * User: qii
 * Date: 13-3-23
 */
public class TimeLinePosition implements Serializable {

    public TimeLinePosition(long firstItemId, int top, int position) {
        this.firstItemId = firstItemId;
        this.top = top;
        this.position = position;
    }

    public long firstItemId = 0L;

    //Fragment contain ListView, when Fragment is invisible (user dont open Fragment), ListView children count is zero, getFirstVisiblePosition return zero
    //App have to use firstItemId to calc correct position
    public int position = 0;
    public int top = 0;

    public TreeSet<Long> newMsgIds = new TreeSet<Long>();

    private boolean empty = false;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append("first item id :")
                .append(firstItemId)
                .append("; top=").append(top);
        return stringBuilder.toString();
    }

    public int getPosition(ListBean source) {
        for (int i = 0; i < source.getSize(); i++) {
            ItemBean item = source.getItem(i);
            if (item != null && item.getIdLong() == this.firstItemId) {
                return i;
            }
        }

        return 0;
    }

    public static TimeLinePosition empty() {
        TimeLinePosition position = new TimeLinePosition(0, 0, 0);
        position.empty = true;
        return position;
    }

    public boolean isEmpty() {
        return empty;
    }
}
