package org.qii.weiciyuan.ui.basefragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
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

    private volatile boolean enableRefreshTime = true;

    public boolean isListViewFling() {
        return !enableRefreshTime;
    }

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

    protected ActionMode mActionMode;

    public void setmActionMode(ActionMode mActionMode) {
        this.mActionMode = mActionMode;
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
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNewMsg();
            }
        });

        pullToRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                listViewFooterViewClick(null);
            }
        });

        footerView = inflater.inflate(R.layout.listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();


        userListAdapter = new UserListAdapter(AbstractUserListFragment.this, bean.getUsers(), getListView());
        pullToRefreshListView.setAdapter(userListAdapter);
        pullToRefreshListView.setOnPullEventListener(getPullEventListener());

        pullToRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {

                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (!enableRefreshTime) {
                            enableRefreshTime = true;
                            getAdapter().notifyDataSetChanged();
                        }
                        break;


                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:

                        enableRefreshTime = false;
                        break;

                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

                        enableRefreshTime = true;
                        break;


                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        }

        );

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mActionMode != null) {
                    getListView().clearChoices();
                    mActionMode.finish();
                    mActionMode = null;
                    return;
                }
                getListView().clearChoices();
                if (position - 1 < getList().getUsers().size()) {

                    listViewItemClick(parent, view, position - 1, id);
                } else {

                    listViewFooterViewClick(view);
                }

            }
        });
        return view;
    }

    private SoundPullEventListener<ListView> getPullEventListener() {
        SoundPullEventListener<ListView> listener = new SoundPullEventListener<ListView>(getActivity());
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
            getLoaderManager().initLoader(NEW_USER_LOADER_ID, null, msgCallback);
        }

        loader = getLoaderManager().getLoader(OLD_USER_LOADER_ID);
        if (loader != null) {
            getLoaderManager().initLoader(OLD_USER_LOADER_ID, null, msgCallback);
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


    private void showFooterView() {
        TextView tv = ((TextView) footerView.findViewById(R.id.listview_footer));
        tv.setVisibility(View.VISIBLE);
        tv.setText(getString(R.string.loading));
        View view = footerView.findViewById(R.id.refresh);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));
    }


    protected void dismissFooterView() {
        footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
        footerView.findViewById(R.id.refresh).clearAnimation();
        footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
    }


    private void showErrorFooterView() {
        TextView tv = ((TextView) footerView.findViewById(R.id.listview_footer));
        tv.setVisibility(View.VISIBLE);
        tv.setText(getString(R.string.click_to_load_older_message));
        View view = footerView.findViewById(R.id.refresh);
        view.clearAnimation();
        view.setVisibility(View.GONE);
    }


    public void loadNewMsg() {
        getLoaderManager().destroyLoader(OLD_USER_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_USER_LOADER_ID, null, msgCallback);
    }


    protected void loadOldMsg(View view) {
        getLoaderManager().destroyLoader(NEW_USER_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().restartLoader(OLD_USER_LOADER_ID, null, msgCallback);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_userlistfragment, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
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

    protected abstract void oldUserOnPostExecute(UserListBean newValue);

    protected void newUserOnPostExecute() {

    }

    private Loader<AsyncTaskLoaderResult<UserListBean>> createNewMsgLoader(int id, Bundle args) {
        Loader<AsyncTaskLoaderResult<UserListBean>> loader = onCreateNewMsgLoader(id, args);
        if (loader == null) {
            loader = new DummyLoader<UserListBean>(getActivity());
        }
        if (loader instanceof AbstractAsyncNetRequestTaskLoader) {
            ((AbstractAsyncNetRequestTaskLoader) loader).setArgs(args);
        }
        return loader;
    }


    private Loader<AsyncTaskLoaderResult<UserListBean>> createOldMsgLoader(int id, Bundle args) {
        Loader<AsyncTaskLoaderResult<UserListBean>> loader = onCreateOldMsgLoader(id, args);
        if (loader == null) {
            loader = new DummyLoader<UserListBean>(getActivity());
        }
        return loader;
    }


    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        return null;
    }


    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        return null;
    }


    protected LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserListBean>> msgCallback = new LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserListBean>>() {

        @Override
        public Loader<AsyncTaskLoaderResult<UserListBean>> onCreateLoader(int id, Bundle args) {
//            clearActionMode();
            showListView();
            switch (id) {
                case NEW_USER_LOADER_ID:
                    if (args == null || args.getBoolean(BundleArgsConstants.SCROLL_TO_TOP))
                        Utility.stopListViewScrollingAndScrollToTop(getListView());
                    return createNewMsgLoader(id, args);
                case OLD_USER_LOADER_ID:
                    showFooterView();
                    return createOldMsgLoader(id, args);
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<AsyncTaskLoaderResult<UserListBean>> loader, AsyncTaskLoaderResult<UserListBean> result) {

            UserListBean data = result != null ? result.data : null;
            WeiboException exception = result != null ? result.exception : null;

            switch (loader.getId()) {
                case NEW_USER_LOADER_ID:
                    getPullToRefreshListView().onRefreshComplete();
                    refreshLayout(getList());
                    if (Utility.isAllNotNull(exception)) {
                        Toast.makeText(getActivity(), exception.getError(), Toast.LENGTH_SHORT).show();
                    } else {
                        if (data != null && data.getUsers().size() > 0) {
                            clearAndReplaceValue(data);
                            getAdapter().notifyDataSetChanged();
                            getListView().setSelectionAfterHeaderView();
                            newUserOnPostExecute();
                        }
                    }
                    break;
                case OLD_USER_LOADER_ID:
                    refreshLayout(getList());

                    if (Utility.isAllNotNull(exception)) {
                        showErrorFooterView();
                    } else {
                        oldUserOnPostExecute(data);
                        getAdapter().notifyDataSetChanged();
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
