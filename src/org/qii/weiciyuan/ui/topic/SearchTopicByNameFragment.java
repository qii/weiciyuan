package org.qii.weiciyuan.ui.topic;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.TopicResultListBean;
import org.qii.weiciyuan.dao.topic.SearchTopicDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;

/**
 * User: qii
 * Date: 12-9-26
 */
public class SearchTopicByNameFragment extends AbstractMessageTimeLineFragment {

    private String q;
    //page 0 and page 1 data is same
    private int page = 1;

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
            refresh();
        }
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
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getItemList().get(position));
        intent.putExtra("token", ((IToken) getActivity()).getToken());
        startActivity(intent);
    }

    @Override
    protected ListBean<MessageBean> getDoInBackgroundNewData() throws WeiboException {

        page = 1;
        SearchTopicDao dao = new SearchTopicDao(((IToken) getActivity()).getToken(), q);
        dao.setPage(String.valueOf(page));
        TopicResultListBean result = dao.getGSONMsgList();

        return result;
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
    protected ListBean<MessageBean> getDoInBackgroundOldData() throws WeiboException {
        SearchTopicDao dao = new SearchTopicDao(((IToken) getActivity()).getToken(), q);
        dao.setPage(String.valueOf(page + 1));
        TopicResultListBean result = dao.getGSONMsgList();
        return result;
    }


    @Override
    protected void oldMsgOnPostExecute(ListBean<MessageBean> newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            getList().getItemList().addAll(newValue.getItemList());
            page++;
            getActivity().invalidateOptionsMenu();
        }
//        ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));

    }
}
