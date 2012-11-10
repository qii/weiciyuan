package org.qii.weiciyuan.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.search.SearchDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-11-10
 */
public class SearchStatusFragment extends AbstractMessageTimeLineFragment {

    private int page = 1;


    public SearchStatusFragment() {

    }

    public void search(){
        pullToRefreshListView.startRefreshNow();
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
            clearAndReplaceValue((ListBean<MessageBean>) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();

        }

        refreshLayout(bean);


    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token",GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("msg", bean.getItem(position));
        startActivity(intent);
    }


    @Override
    protected ListBean<MessageBean> getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
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
        SearchDao dao = new SearchDao(GlobalContext.getInstance().getSpecialToken(), ((SearchMainActivity) getActivity()).getSearchWord());
        ListBean<MessageBean> result = dao.getStatusList();

        return result;
    }

    @Override
    protected ListBean<MessageBean> getDoInBackgroundOldData() throws WeiboException {

        SearchDao dao = new SearchDao(GlobalContext.getInstance().getSpecialToken(), ((SearchMainActivity) getActivity()).getSearchWord());
        dao.setPage(String.valueOf(page + 1));

        ListBean<MessageBean> result = dao.getStatusList();

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
    }
}
