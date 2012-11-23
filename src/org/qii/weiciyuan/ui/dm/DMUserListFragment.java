package org.qii.weiciyuan.ui.dm;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.DMUserListBean;
import org.qii.weiciyuan.dao.dm.DMDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.adapter.DMUserListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListFragment extends AbstractTimeLineFragment<DMUserListBean> {

    private DMUserListBean bean = new DMUserListBean();

     @Override
     public DMUserListBean getList() {
         return bean;
     }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new DMUserListBean();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        pullToRefreshListView.startRefreshNow();
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DMActivity.class);
        intent.putExtra("user", bean.getItem(position).getUser());
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.actionbar_menu_mystatustimelinefragment, menu);
        menu.findItem(R.id.name).setTitle(getString(R.string.personal_info));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.name:
                intent = new Intent(getActivity(), MyInfoActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", GlobalContext.getInstance().getAccountBean().getInfo());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                startActivity(intent);
                break;
            case R.id.write_weibo:
                intent = new Intent(getActivity(), WriteWeiboActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMUserListAdapter(this, ((AbstractAppActivity) getActivity()).getCommander(), getList().getItemList(), getListView());
        getListView().setAdapter(timeLineAdapter);
    }

    protected void clearAndReplaceValue(DMUserListBean value) {
        bean.getItemList().clear();
        bean.getItemList().addAll(value.getItemList());
        bean.setTotal_number(value.getTotal_number());
    }

    @Override
    protected void newMsgOnPostExecute(DMUserListBean newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getSize() == 0) {

            } else if (newValue.getSize() > 0) {
                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();

            }
        }
    }

    @Override
    protected void oldMsgOnPostExecute(DMUserListBean newValue) {

    }

    @Override
    protected DMUserListBean getDoInBackgroundNewData() throws WeiboException {
        return new DMDao(GlobalContext.getInstance().getSpecialToken()).getUserList();
    }

    @Override
    protected DMUserListBean getDoInBackgroundOldData() throws WeiboException {
        return null;
    }

    @Override
    protected DMUserListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }
}
