package org.qii.weiciyuan.ui.userinfo;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.user.FanListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.basefragment.AbstractUserListFragment;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FanListFragment extends AbstractUserListFragment {

    public FanListFragment(String uid) {
        super(uid);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!TextUtils.isEmpty(currentUser.getFollowers_count())) {
            int size = Integer.valueOf(currentUser.getFollowers_count());
            int newSize = bean.getTotal_number();
            String number = "";
            if (size >= newSize) {
                number = bean.getUsers().size() + "/" + size;
            } else {
                number = bean.getUsers().size() + "/" + newSize;
            }
            menu.findItem(R.id.statusesbyidtimelinefragment_status_number).setTitle(number);
        }
    }

    @Override
    protected UserListBean getDoInBackgroundNewData() throws WeiboException {
        FanListDao dao = new FanListDao(((IToken) getActivity()).getToken(), uid);

        if (getList().getUsers().size() > 0 && bean.getPrevious_cursor() > 0) {
            dao.setCursor(String.valueOf(bean.getPrevious_cursor() - 1));
        }

        UserListBean result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected UserListBean getDoInBackgroundOldData() throws WeiboException {
        FanListDao dao = new FanListDao(((IToken) getActivity()).getToken(), uid);
        if (getList().getUsers().size() > 0) {
            dao.setCursor(String.valueOf(bean.getNext_cursor()));
        }

        UserListBean result = dao.getGSONMsgList();
        return result;
    }

}

