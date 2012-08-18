package org.qii.weiciyuan.ui.userinfo;

import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.user.FanListDao;
import org.qii.weiciyuan.ui.Abstract.IToken;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FanListFragment extends AbstractUserListFragment {

    public FanListFragment(String uid) {
        super(uid);
    }


    @Override
    protected UserListBean getDoInBackgroundNewData() {
        FanListDao dao = new FanListDao(((IToken) getActivity()).getToken(), uid);

        if (getList().getUsers().size() > 0) {
            dao.setCursor(bean.getPrevious_cursor());
        }
        UserListBean result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected UserListBean getDoInBackgroundOldData() {
        FanListDao dao = new FanListDao(((IToken) getActivity()).getToken(), uid);
        if (getList().getUsers().size() > 0) {
            dao.setCursor(bean.getNext_cursor());
        }
        UserListBean result = dao.getGSONMsgList();
        return result;
    }

}

