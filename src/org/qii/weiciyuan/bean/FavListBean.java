package org.qii.weiciyuan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-8-18
 */
public class FavListBean implements Serializable {
    private List<FavBean> favorites = new ArrayList<FavBean>();
    private String total_number = "";

    public List<FavBean> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavBean> favorites) {
        this.favorites = favorites;
    }

    public String getTotal_number() {
        return total_number;
    }

    public void setTotal_number(String total_number) {
        this.total_number = total_number;
    }
}
