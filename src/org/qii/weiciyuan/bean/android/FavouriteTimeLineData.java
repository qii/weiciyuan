package org.qii.weiciyuan.bean.android;

import org.qii.weiciyuan.bean.FavListBean;

/**
 * User: qii
 * Date: 13-5-30
 */
public class FavouriteTimeLineData {
    public FavListBean favList;
    public TimeLinePosition position;
    public int page;

    public FavouriteTimeLineData(FavListBean favList, int page, TimeLinePosition position) {
        this.favList = favList;
        this.page = page;
        this.position = position;
    }
}
