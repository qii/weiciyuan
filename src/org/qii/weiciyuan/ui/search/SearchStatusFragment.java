package org.qii.weiciyuan.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.bean.SearchStatusListBean;
import org.qii.weiciyuan.dao.search.SearchDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.interfaces.ICommander;

/**
 * User: qii
 * Date: 12-11-10
 */
public class SearchStatusFragment extends AbstractMessageTimeLineFragment<SearchStatusListBean> {

    private int page = 1;

    private SearchStatusListBean bean = new SearchStatusListBean();

    @Override
    public SearchStatusListBean getList() {
        return bean;
    }

    public SearchStatusFragment() {

    }

    public void search() {
        pullToRefreshListView.startRefreshNow();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((ICommander) getActivity()).getBitmapDownloader();
        if (savedInstanceState != null && bean.getItemList().size() == 0) {
            clearAndReplaceValue((SearchStatusListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();

        }

        refreshLayout(bean);


    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("msg", bean.getItem(position));
        startActivity(intent);
    }


    @Override
    protected SearchStatusListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }


    @Override
    protected SearchStatusListBean getDoInBackgroundNewData() throws WeiboException {
        page = 1;
        SearchDao dao = new SearchDao(GlobalContext.getInstance().getSpecialToken(), ((SearchMainParentFragment) getParentFragment()).getSearchWord());
        SearchStatusListBean result = dao.getStatusList();

        return result;
    }

    @Override
    protected SearchStatusListBean getDoInBackgroundOldData() throws WeiboException {

        SearchDao dao = new SearchDao(GlobalContext.getInstance().getSpecialToken(), ((SearchMainParentFragment) getParentFragment()).getSearchWord());
        dao.setPage(String.valueOf(page + 1));

        SearchStatusListBean result = dao.getStatusList();

        return result;
    }

    @Override
    protected void newMsgOnPostExecute(SearchStatusListBean newValue) {
        if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();
        }

    }

    @Override
    protected void oldMsgOnPostExecute(SearchStatusListBean newValue) {

        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            getActivity().invalidateOptionsMenu();
            page++;
        }
    }
}
