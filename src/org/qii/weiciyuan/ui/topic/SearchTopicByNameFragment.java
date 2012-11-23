package org.qii.weiciyuan.ui.topic;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.TopicResultListBean;
import org.qii.weiciyuan.dao.topic.SearchTopicDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.interfaces.IToken;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

/**
 * User: qii
 * Date: 12-9-26
 */
public class SearchTopicByNameFragment extends AbstractMessageTimeLineFragment<TopicResultListBean> {

    private String q;
    //page 0 and page 1 data is same
    private int page = 1;

    private TopicResultListBean bean = new TopicResultListBean();

    @Override
    public TopicResultListBean getList() {
        return bean;
    }

    public SearchTopicByNameFragment() {

    }

    public SearchTopicByNameFragment(String q) {
        this.q = q;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("q", q);
        outState.putInt("page", page);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            q = savedInstanceState.getString("q");
            page = savedInstanceState.getInt("page");
        } else {
            pullToRefreshListView.startRefreshNow();

        }
    }

    @Override
    protected void newMsgOnPostExecute(TopicResultListBean newValue) {
        if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            buildActionBatSubtitle();
        }
    }

    @Override
    protected void oldMsgOnPostExecute(TopicResultListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            page++;
            buildActionBatSubtitle();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_searchtopicbynamefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_write:
                Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                intent.putExtra("content", "#" + q + "#");
                startActivity(intent);
                break;

            case R.id.menu_refresh:
                pullToRefreshListView.startRefreshNow();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getItemList().get(position));
        intent.putExtra("token", ((IToken) getActivity()).getToken());
        startActivity(intent);
    }

    @Override
    protected TopicResultListBean getDoInBackgroundNewData() throws WeiboException {

        page = 1;
        SearchTopicDao dao = new SearchTopicDao(((IToken) getActivity()).getToken(), q);
        dao.setPage(String.valueOf(page));
        TopicResultListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected TopicResultListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }


    private void buildActionBatSubtitle() {
        int newSize = bean.getTotal_number();
        String number = bean.getSize() + "/" + newSize;
        getActivity().getActionBar().setSubtitle(number);
    }

    @Override
    protected TopicResultListBean getDoInBackgroundOldData() throws WeiboException {
        SearchTopicDao dao = new SearchTopicDao(((IToken) getActivity()).getToken(), q);
        dao.setPage(String.valueOf(page + 1));
        TopicResultListBean result = dao.getGSONMsgList();
        return result;
    }


}
