package org.qii.weiciyuan.ui.basefragment;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.lib.pulltorefresh.extras.SoundPullEventListener;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.adapter.UserListAdapter;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.loader.AbstractAsyncNetRequestTaskLoader;
import org.qii.weiciyuan.ui.loader.DummyLoader;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * User: qii
 * Date: 12-8-18
 */
public abstract class AbstractUserListFragment extends AbstractAppFragment {

    protected View footerView;
    protected PullToRefreshListView pullToRefreshListView;
    protected TextView empty;
    protected ProgressBar progressBar;

    private UserListAdapter userListAdapter;
    protected UserListBean bean = new UserListBean();

    protected static final int NEW_USER_LOADER_ID = 1;
    protected static final int OLD_USER_LOADER_ID = 2;

    private boolean canLoadOldData = true;

    public ListView getListView() {
        return pullToRefreshListView.getRefreshableView();
    }

    protected UserListAdapter getAdapter() {
        return userListAdapter;
    }

    protected void clearAndReplaceValue(UserListBean value) {
        bean.setNext_cursor(value.getNext_cursor());
        bean.getUsers().clear();
        bean.getUsers().addAll(value.getUsers());
        bean.setTotal_number(value.getTotal_number());
        bean.setPrevious_cursor(value.getPrevious_cursor());
    }

    protected ActionMode actionMode;

    public void setmActionMode(ActionMode mActionMode) {
        this.actionMode = mActionMode;
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setFastScrollEnabled(SettingUtility.allowFastScroll());
    }

    public AbstractUserListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(false);
    }

    public UserListBean getList() {
        return bean;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bean", bean);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);
        pullToRefreshListView.setOnRefreshListener(
                new UserListOnRefreshListener());
        pullToRefreshListView
                .setOnLastItemVisibleListener(new UserListOnLastItemVisibleListener());
        pullToRefreshListView.setOnPullEventListener(getPullEventListener());
        pullToRefreshListView.setOnScrollListener(new UserListOnScrollListener());
        pullToRefreshListView.setOnItemClickListener(new UserListOnItemClickListener());
        pullToRefreshListView.getRefreshableView().setFooterDividersEnabled(false);

        footerView = inflater.inflate(R.layout.listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();

        userListAdapter = new UserListAdapter(AbstractUserListFragment.this, bean.getUsers(),
                getListView());
        pullToRefreshListView.setAdapter(userListAdapter);

        return view;
    }

    private SoundPullEventListener<ListView> getPullEventListener() {
        SoundPullEventListener<ListView> listener = new SoundPullEventListener<ListView>(
                getActivity());
        if (SettingUtility.getEnableSound()) {
            listener.addSoundEvent(PullToRefreshBase.State.RELEASE_TO_REFRESH, R.raw.psst1);
            //            listener.addSoundEvent(PullToRefreshBase.State.GIVE_UP, R.raw.psst2);
            listener.addSoundEvent(PullToRefreshBase.State.RESET, R.raw.pop);
        }
        return listener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Loader<UserListBean> loader = getLoaderManager().getLoader(NEW_USER_LOADER_ID);
        if (loader != null) {
            getLoaderManager().initLoader(NEW_USER_LOADER_ID, null, userAsyncTaskLoaderCallback);
        }

        loader = getLoaderManager().getLoader(OLD_USER_LOADER_ID);
        if (loader != null) {
            getLoaderManager().initLoader(OLD_USER_LOADER_ID, null, userAsyncTaskLoaderCallback);
        }
    }

    protected void listViewFooterViewClick(View view) {
        loadOldMsg(view);
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("user", bean.getUsers().get(position));
        startActivity(intent);
    }

    protected void refreshLayout(UserListBean bean) {
        if (bean.getUsers().size() > 0) {
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
//            listView.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
//            listView.setVisibility(View.INVISIBLE);
        }
    }

    protected void showFooterView() {
        View view = footerView.findViewById(R.id.loading_progressbar);
        view.setVisibility(View.VISIBLE);
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.setAlpha(1.0f);
        footerView.findViewById(R.id.laod_failed).setVisibility(View.GONE);
    }

    protected void dismissFooterView() {
        final View progressbar = footerView.findViewById(R.id.loading_progressbar);
        progressbar.animate().scaleX(0).scaleY(0).alpha(0.5f).setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        progressbar.setVisibility(View.GONE);
                    }
                });
        footerView.findViewById(R.id.laod_failed).setVisibility(View.GONE);
    }

    protected void showErrorFooterView() {
        View view = footerView.findViewById(R.id.loading_progressbar);
        view.setVisibility(View.GONE);
        TextView tv = ((TextView) footerView.findViewById(R.id.laod_failed));
        tv.setVisibility(View.VISIBLE);
    }

    public void loadNewMsg() {
        canLoadOldData = true;

        getLoaderManager().destroyLoader(OLD_USER_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_USER_LOADER_ID, null, userAsyncTaskLoaderCallback);
    }

    protected void loadOldMsg(View view) {
        if (getLoaderManager().getLoader(OLD_USER_LOADER_ID) != null || !canLoadOldData) {
            return;
        }

        getLoaderManager().destroyLoader(NEW_USER_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().restartLoader(OLD_USER_LOADER_ID, null, userAsyncTaskLoaderCallback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_userlistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.loading_progressbar:
                pullToRefreshListView.setRefreshing();
                loadNewMsg();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showListView() {
        empty.setVisibility(View.INVISIBLE);
//        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private PullToRefreshListView getPullToRefreshListView() {
        return this.pullToRefreshListView;
    }

    public void clearActionMode() {
        if (actionMode != null) {

            actionMode.finish();
            actionMode = null;
        }
        if (pullToRefreshListView != null && getListView().getCheckedItemCount() > 0) {
            getListView().clearChoices();
            if (getAdapter() != null) {
                getAdapter().notifyDataSetChanged();
            }
        }
    }

    private class UserListOnLastItemVisibleListener
            implements PullToRefreshBase.OnLastItemVisibleListener {

        @Override
        public void onLastItemVisible() {
            listViewFooterViewClick(null);
        }
    }

    private class UserListOnRefreshListener
            implements PullToRefreshBase.OnRefreshListener<ListView> {

        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            loadNewMsg();
        }
    }

    private class UserListOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (actionMode != null) {
                getListView().clearChoices();
                actionMode.finish();
                actionMode = null;
                return;
            }
            getListView().clearChoices();
            if (position - 1 < getList().getUsers().size()) {

                listViewItemClick(parent, view, position - 1, id);
            } else {

                listViewFooterViewClick(view);
            }
        }
    }

    private class UserListOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            if (getListView().getLastVisiblePosition() > 7
                    && getListView().getLastVisiblePosition() > getList().getUsers().size() - 3
                    && getListView().getFirstVisiblePosition() != getListView()
                    .getHeaderViewsCount()) {
                loadOldMsg(null);
            }
        }
    }

    protected abstract void oldUserLoaderSuccessCallback(UserListBean newValue);

    protected void newUserLoaderSuccessCallback() {

    }

    private Loader<AsyncTaskLoaderResult<UserListBean>> createNewUserLoader(int id, Bundle args) {
        Loader<AsyncTaskLoaderResult<UserListBean>> loader = onCreateNewUserLoader(id, args);
        if (loader == null) {
            loader = new DummyLoader<UserListBean>(getActivity());
        }
        if (loader instanceof AbstractAsyncNetRequestTaskLoader) {
            ((AbstractAsyncNetRequestTaskLoader) loader).setArgs(args);
        }
        return loader;
    }

    private Loader<AsyncTaskLoaderResult<UserListBean>> createOldUserLoader(int id, Bundle args) {
        Loader<AsyncTaskLoaderResult<UserListBean>> loader = onCreateOldUserLoader(id, args);
        if (loader == null) {
            loader = new DummyLoader<UserListBean>(getActivity());
        }
        return loader;
    }

    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateNewUserLoader(int id,
            Bundle args) {
        return null;
    }

    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateOldUserLoader(int id,
            Bundle args) {
        return null;
    }

    protected LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserListBean>>
            userAsyncTaskLoaderCallback
            = new LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserListBean>>() {

        @Override
        public Loader<AsyncTaskLoaderResult<UserListBean>> onCreateLoader(int id, Bundle args) {
//            clearActionMode();
            showListView();
            switch (id) {
                case NEW_USER_LOADER_ID:
                    if (args == null || args.getBoolean(BundleArgsConstants.SCROLL_TO_TOP)) {
                        Utility.stopListViewScrollingAndScrollToTop(getListView());
                    }
                    return createNewUserLoader(id, args);
                case OLD_USER_LOADER_ID:
                    showFooterView();
                    return createOldUserLoader(id, args);
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<AsyncTaskLoaderResult<UserListBean>> loader,
                AsyncTaskLoaderResult<UserListBean> result) {
            UserListBean data = result != null ? result.data : null;
            WeiboException exception = result != null ? result.exception : null;

            switch (loader.getId()) {
                case NEW_USER_LOADER_ID:
                    getPullToRefreshListView().onRefreshComplete();
                    refreshLayout(getList());
                    if (Utility.isAllNotNull(exception)) {
                        Toast.makeText(getActivity(), exception.getError(), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        if (data != null && data.getUsers().size() > 0) {
                            clearAndReplaceValue(data);
                            getAdapter().notifyDataSetChanged();
                            getListView().setSelectionAfterHeaderView();
                            newUserLoaderSuccessCallback();
                        }
                    }
                    break;
                case OLD_USER_LOADER_ID:
                    refreshLayout(getList());

                    if (exception != null) {
                        showErrorFooterView();
                    } else if (data != null) {
                        canLoadOldData = data.getUsers().size() > 1;
                        oldUserLoaderSuccessCallback(data);
                        getAdapter().notifyDataSetChanged();
                        dismissFooterView();
                    } else {
                        canLoadOldData = false;
                        dismissFooterView();
                    }
                    break;
            }
            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<AsyncTaskLoaderResult<UserListBean>> loader) {

        }
    };
}
