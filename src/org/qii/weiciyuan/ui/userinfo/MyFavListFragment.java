package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.FavListBean;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.fav.FavListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.interfaces.IToken;

/**
 * User: qii
 * Date: 12-8-18
 * this class need to refactor
 */
public class MyFavListFragment extends AbstractMessageTimeLineFragment<FavListBean> {

    private int page = 1;

    private FavListBean bean = new FavListBean();

    @Override
    public FavListBean getList() {
        return bean;
    }

    public MyFavListFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_myfavlistfragment, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null && bean.getItemList().size() == 0) {
            clearAndReplaceValue((ListBean<MessageBean>) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            pullToRefreshListView.startRefreshNow();
            refresh();

        }

    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", ((IToken) getActivity()).getToken());
        intent.putExtra("msg", bean.getItem(position));
        startActivity(intent);
    }


    private void buildActionBarSubtitle() {
        if (bean != null) {
            int newSize = bean.getTotal_number();
            String number = bean.getSize() + "/" + newSize;
            getActivity().getActionBar().setSubtitle(number);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                pullToRefreshListView.startRefreshNow();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected FavListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }


    @Override
    protected FavListBean getDoInBackgroundNewData() throws WeiboException {
        page = 1;
        FavListDao dao = new FavListDao(((IToken) getActivity()).getToken()).setPage(String.valueOf(page));


        FavListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected FavListBean getDoInBackgroundOldData() throws WeiboException {

        FavListDao dao = new FavListDao(((IToken) getActivity()).getToken()).setPage(String.valueOf(page + 1));

        FavListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected void newMsgOnPostExecute(FavListBean newValue) {
        if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            buildActionBarSubtitle();
        }

    }

    @Override
    protected void oldMsgOnPostExecute(FavListBean newValue) {

        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            buildActionBarSubtitle();
            page++;
        }
    }


}
