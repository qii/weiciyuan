package org.qii.weiciyuan.ui.userinfo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.user.FriendListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.actionmenu.FriendSingleChoiceModeListener;
import org.qii.weiciyuan.ui.actionmenu.MyFriendSingleChoiceModeListener;
import org.qii.weiciyuan.ui.basefragment.AbstractUserListFragment;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FriendsListFragment extends AbstractUserListFragment {


    public FriendsListFragment() {

    }

    public FriendsListFragment(String uid) {
        super(uid);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (position - 1 < getList().getUsers().size() && position - 1 >= 0) {
                    if (mActionMode != null) {
                        mActionMode.finish();
                        mActionMode = null;
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        if (currentUser.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
                            mActionMode = getActivity().startActionMode(new MyFriendSingleChoiceModeListener(getListView(), timeLineAdapter, FriendsListFragment.this, bean.getUsers().get(position - 1)));
                        } else {
                            mActionMode = getActivity().startActionMode(new FriendSingleChoiceModeListener(getListView(), timeLineAdapter, FriendsListFragment.this, bean.getUsers().get(position - 1)));
                        }
                        return true;
                    } else {
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        if (currentUser.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
                            mActionMode = getActivity().startActionMode(new MyFriendSingleChoiceModeListener(getListView(), timeLineAdapter, FriendsListFragment.this, bean.getUsers().get(position - 1)));
                        } else {
                            mActionMode = getActivity().startActionMode(new FriendSingleChoiceModeListener(getListView(), timeLineAdapter, FriendsListFragment.this, bean.getUsers().get(position - 1)));
                        }
                        return true;
                    }
                }
                return false;
            }
        }

        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!TextUtils.isEmpty(currentUser.getFriends_count())) {

            int size = Integer.valueOf(currentUser.getFriends_count());
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
        FriendListDao dao = new FriendListDao(((IToken) getActivity()).getToken(), uid);

        if (getList().getUsers().size() > 0 && bean.getPrevious_cursor() > 0) {
            dao.setCursor(String.valueOf(bean.getPrevious_cursor() - 1));
        }
        UserListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected UserListBean getDoInBackgroundOldData() throws WeiboException {
        FriendListDao dao = new FriendListDao(((IToken) getActivity()).getToken(), uid);
        if (getList().getUsers().size() > 0) {
            dao.setCursor(String.valueOf(bean.getNext_cursor()));
        }
        UserListBean result = dao.getGSONMsgList();

        return result;
    }
}

