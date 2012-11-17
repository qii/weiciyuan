package org.qii.weiciyuan.ui.search;

import android.os.Bundle;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.search.SearchDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractUserListFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

import java.util.List;

/**
 * User: qii
 * Date: 12-11-10
 */
public class SearchUserFragment extends AbstractUserListFragment {

    private int page = 1;


    public SearchUserFragment() {
        super();
    }

    public void search() {
        pullToRefreshListView.startRefreshNow();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null) {
            clearAndReplaceValue((UserListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();

        }
        refreshLayout(bean);
    }


    @Override
    protected UserListBean getDoInBackgroundNewData() throws WeiboException {
        page = 1;
        SearchDao dao = new SearchDao(GlobalContext.getInstance().getSpecialToken(), ((SearchMainActivity) getActivity()).getSearchWord());
        UserListBean result = dao.getUserList();

        return result;
    }

    @Override
    protected UserListBean getDoInBackgroundOldData() throws WeiboException {
        SearchDao dao = new SearchDao(GlobalContext.getInstance().getSpecialToken(), ((SearchMainActivity) getActivity()).getSearchWord());
        dao.setPage(String.valueOf(page + 1));

        UserListBean result = dao.getUserList();

        return result;
    }

    @Override
    protected void oldUserOnPostExecute(UserListBean newValue) {
        if (newValue != null && newValue.getUsers().size() > 0) {
            List<UserBean> list = newValue.getUsers();
            getList().getUsers().addAll(list);
            page++;
        }
    }

    @Override
    protected void newUserOnPostExecute() {

    }

}

