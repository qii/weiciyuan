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
import org.qii.weiciyuan.support.database.DMDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.adapter.DMUserListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListFragment extends AbstractTimeLineFragment<DMUserListBean> {

    private DMUserListBean bean = new DMUserListBean();

    private DBCacheTask dbTask;


    @Override
    public DMUserListBean getList() {
        return bean;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                if (Utility.isTaskStopped(dbTask)) {
                    dbTask = new DBCacheTask();
                    dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                bean.addNewData((DMUserListBean) savedInstanceState.getSerializable("bean"));
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(dbTask);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_dmuserlistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_write_dm:
                Intent intent = new Intent(getActivity(), DMSelectUserActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DMActivity.class);
        intent.putExtra("user", bean.getItem(position).getUser());
        startActivity(intent);
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMUserListAdapter(this, ((AbstractAppActivity) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView());
        getListView().setAdapter(timeLineAdapter);
    }

    private class DBCacheTask extends MyAsyncTask<Void, DMUserListBean, DMUserListBean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected DMUserListBean doInBackground(Void... params) {
            return DMDBTask.get(GlobalContext.getInstance().getCurrentAccountId());
        }

        @Override
        protected void onPostExecute(DMUserListBean result) {
            super.onPostExecute(result);
            if (result != null)
                getList().addNewData(result);
            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            refreshLayout(getList());

            if (getList().getSize() == 0) {
                getPullToRefreshListView().startRefreshNow();
            }
        }
    }

    @Override
    protected void newMsgOnPostExecute(DMUserListBean newValue) {
        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
        }

    }

    @Override
    protected void oldMsgOnPostExecute(DMUserListBean newValue) {
        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected DMUserListBean getDoInBackgroundNewData() throws WeiboException {
        DMDao dao = new DMDao(GlobalContext.getInstance().getSpecialToken());
        dao.setCursor(String.valueOf(0));
        DMUserListBean result = dao.getUserList();
        if (result != null) {
            DMDBTask.add(result, GlobalContext.getInstance().getCurrentAccountId());
        }
        return result;
    }

    @Override
    protected DMUserListBean getDoInBackgroundOldData() throws WeiboException {

        if (getList().getSize() > 0 && Integer.valueOf(getList().getNext_cursor()) == 0) {
            return null;
        }

        DMDao dao = new DMDao(GlobalContext.getInstance().getSpecialToken());
        if (getList().getSize() > 0) {
            dao.setCursor(String.valueOf(getList().getNext_cursor()));
        }

        return dao.getUserList();
    }

    @Override
    protected DMUserListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }
}
