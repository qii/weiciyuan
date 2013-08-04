package org.qii.weiciyuan.ui.dm;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.DMUserListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.database.DMDBTask;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.adapter.DMUserListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.loader.DMUserLoader;
import org.qii.weiciyuan.ui.main.LeftMenuFragment;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListFragment extends AbstractTimeLineFragment<DMUserListBean> implements MainTimeLineActivity.ScrollableListFragment {

    private DMUserListBean bean = new DMUserListBean();

    private DBCacheTask dbTask;


    @Override
    public DMUserListBean getList() {
        return bean;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bean", bean);
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
                bean.addNewData((DMUserListBean) savedInstanceState.getParcelable("bean"));
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }
        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex()
                == LeftMenuFragment.DM_INDEX) {
            buildActionBarAndViewPagerTitles();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(dbTask);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            buildActionBarAndViewPagerTitles();
        }
    }

    public void buildActionBarAndViewPagerTitles() {
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);

        if (Utility.isDevicePort()) {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.dm));
            getActivity().getActionBar().setIcon(R.drawable.ic_menu_message);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.dm));
            getActivity().getActionBar().setIcon(R.drawable.ic_launcher);
        }

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();
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
                startActivityForResult(intent, 0);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;

        Intent intent = new Intent(getActivity(), DMActivity.class);
        intent.putExtra("user", data.getParcelableExtra("user"));
        startActivity(intent);
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMUserListAdapter(this, getList().getItemList(), getListView());
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
                getPullToRefreshListView().setRefreshing();
                loadNewMsg();
            }
        }
    }

    @Override
    protected void newMsgOnPostExecute(DMUserListBean newValue, Bundle loaderArgs) {
        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            DMDBTask.asyncReplace(getList(), GlobalContext.getInstance().getCurrentAccountId());

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
    public void loadNewMsg() {
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgCallback);
    }

    @Override
    protected void loadOldMsg(View view) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().restartLoader(OLD_MSG_LOADER_ID, null, msgCallback);
    }


    protected Loader<AsyncTaskLoaderResult<DMUserListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = String.valueOf(0);
        return new DMUserLoader(getActivity(), token, cursor);
    }


    protected Loader<AsyncTaskLoaderResult<DMUserListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = null;
        if (getList().getSize() > 0 && Integer.valueOf(getList().getNext_cursor()) == 0) {
            return null;
        }

        if (getList().getSize() > 0) {
            cursor = String.valueOf(getList().getNext_cursor());
        }

        return new DMUserLoader(getActivity(), token, cursor);
    }

    @Override
    public void scrollToTop() {
        Utility.stopListViewScrollingAndScrollToTop(getListView());
    }
}
