package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-8-18
 */
public class FavListBean extends ListBean<MessageBean> {
    private List<FavBean> favorites = new ArrayList<FavBean>();
    private List<MessageBean> actualStore = null;

    public List<FavBean> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavBean> favorites) {
        this.favorites = favorites;
    }

    @Override
    public int getSize() {
        return favorites.size();
    }


    @Override
    public MessageBean getItem(int position) {
        return favorites.get(position).getStatus();
    }

    @Override
    public List<MessageBean> getItemList() {
        if (actualStore == null) {
            actualStore = new ArrayList<MessageBean>();
            for (FavBean b : favorites) {
                actualStore.add(b.getStatus());
            }
        }
        return actualStore;
    }


}
