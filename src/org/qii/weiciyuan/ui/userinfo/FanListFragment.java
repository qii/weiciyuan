package org.qii.weiciyuan.ui.userinfo;

import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.actionmenu.MyFanSingleChoiceModeListener;
import org.qii.weiciyuan.ui.actionmenu.NormalFriendShipSingleChoiceModeListener;
import org.qii.weiciyuan.ui.basefragment.AbstractFriendsFanListFragment;
import org.qii.weiciyuan.ui.loader.FanUserLoader;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FanListFragment extends AbstractFriendsFanListFragment {

    public static FanListFragment newInstance(UserBean userBean) {
        FanListFragment fragment = new FanListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("userBean", userBean);
        fragment.setArguments(bundle);
        return fragment;
    }

    public FanListFragment() {
        super();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(new FanListOnItemLongClickListener());
    }

    @Override
    protected UserBean getCurrentUser() {
        return getArguments().getParcelable("userBean");
    }

    private class FanListOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position - 1 < getList().getUsers().size() && position - 1 >= 0) {
                if (actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                    getListView().setItemChecked(position, true);
                    getAdapter().notifyDataSetChanged();
                    if (getCurrentUser().getId()
                            .equals(GlobalContext.getInstance().getCurrentAccountId())) {
                        actionMode = getActivity().startActionMode(
                                new MyFanSingleChoiceModeListener(getListView(), getAdapter(),
                                        FanListFragment.this, bean.getUsers().get(position - 1)));
                    } else {
                        actionMode = getActivity().startActionMode(
                                new NormalFriendShipSingleChoiceModeListener(getListView(),
                                        getAdapter(), FanListFragment.this,
                                        bean.getUsers().get(position - 1)));
                    }
                    return true;
                } else {
                    getListView().setItemChecked(position, true);
                    getAdapter().notifyDataSetChanged();
                    if (getCurrentUser().getId()
                            .equals(GlobalContext.getInstance().getCurrentAccountId())) {
                        actionMode = getActivity().startActionMode(
                                new MyFanSingleChoiceModeListener(getListView(), getAdapter(),
                                        FanListFragment.this, bean.getUsers().get(position - 1)));
                    } else {
                        actionMode = getActivity().startActionMode(
                                new NormalFriendShipSingleChoiceModeListener(getListView(),
                                        getAdapter(), FanListFragment.this,
                                        bean.getUsers().get(position - 1)));
                    }
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateNewUserLoader(int id,
            Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = String.valueOf(0);
        return new FanUserLoader(getActivity(), token, getCurrentUser().getId(), cursor);
    }

    @Override
    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateOldUserLoader(int id,
            Bundle args) {

        if (getList().getUsers().size() > 0 && Integer.valueOf(getList().getNext_cursor()) == 0) {
            return null;
        }

        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = String.valueOf(bean.getNext_cursor());
        return new FanUserLoader(getActivity(), token, getCurrentUser().getId(), cursor);
    }
}


