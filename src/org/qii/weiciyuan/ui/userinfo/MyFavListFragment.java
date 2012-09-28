package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.FavListBean;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.fav.FavListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;

/**
 * User: qii
 * Date: 12-8-18
 */
public class MyFavListFragment extends AbstractMessageTimeLineFragment {

    private int page = 1;


    public MyFavListFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null && bean.getItemList().size() == 0) {
            clearAndReplaceValue((FavListBean) savedInstanceState.getSerializable("bean"));
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.myfavlistfragment_menu, menu);
        if (bean != null) {
            int newSize = bean.getTotal_number();
            String number = bean.getSize() + "/" + newSize;
            menu.findItem(R.id.total_number).setTitle(number);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.total_number:
                Toast.makeText(getActivity(), getString(R.string.deleted_by_sina_weibo), Toast.LENGTH_SHORT).show();
                break;

            case R.id.myfavlistfragment_refresh:
                pullToRefreshListView.startRefreshNow();

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void newMsgOnPostExecute(ListBean<MessageBean> newValue) {
        if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
            clearAndReplaceValue(newValue);
            timeLineAdapter.notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();
        }

    }


    @Override
    protected ListBean<MessageBean> getDoInBackgroundNewData() throws WeiboException {
        page = 1;
        FavListDao dao = new FavListDao(((IToken) getActivity()).getToken()).setPage(String.valueOf(page));


        FavListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected ListBean<MessageBean> getDoInBackgroundOldData() throws WeiboException {

        FavListDao dao = new FavListDao(((IToken) getActivity()).getToken()).setPage(String.valueOf(page + 1));

        FavListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected void oldMsgOnPostExecute(ListBean<MessageBean> newValue) {

        if (newValue != null && newValue.getSize() > 0) {
            getList().getItemList().addAll(newValue.getItemList());
            bean.setTotal_number(newValue.getTotal_number());
            getActivity().invalidateOptionsMenu();
            page++;
        }
//        ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));
    }
}
