package org.qii.weiciyuan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListBean extends ListBean<DMUserBean> {
    private List<DMUserBean> user_list = new ArrayList<DMUserBean>();

    @Override
    public int getSize() {
        return user_list.size();
    }

    @Override
    public DMUserBean getItem(int position) {
        return user_list.get(position);
    }

    @Override
    public List<DMUserBean> getItemList() {
        return user_list;
    }


}

