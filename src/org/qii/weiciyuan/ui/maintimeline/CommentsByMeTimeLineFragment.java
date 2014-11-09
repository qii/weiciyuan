package org.qii.weiciyuan.ui.maintimeline;

import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.bean.android.CommentTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.dao.destroy.DestroyCommentDao;
import org.qii.weiciyuan.support.database.CommentByMeTimeLineDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.actionmenu.CommentFloatingMenu;
import org.qii.weiciyuan.ui.actionmenu.CommentSingleChoiceModeListener;
import org.qii.weiciyuan.ui.adapter.CommentListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.IRemoveItem;
import org.qii.weiciyuan.ui.loader.CommentsByMeDBLoader;
import org.qii.weiciyuan.ui.loader.CommentsByMeMsgLoader;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * User: qii
 * Date: 13-1-22
 */
public class CommentsByMeTimeLineFragment extends AbstractTimeLineFragment<CommentListBean>
        implements IRemoveItem {

    private static final String ARGUMENTS_ACCOUNT_EXTRA = CommentsByMeTimeLineFragment.class.getName() + ":account_extra";
    private static final String ARGUMENTS_USER_EXTRA = CommentsByMeTimeLineFragment.class.getName() + ":userBean_extra";
    private static final String ARGUMENTS_TOKEN_EXTRA = CommentsByMeTimeLineFragment.class.getName() + ":token_extra";
    private static final String ARGUMENTS_DATA_EXTRA = CommentsByMeTimeLineFragment.class.getName() + ":msg_extra";
    private static final String ARGUMENTS_TIMELINE_POSITION_EXTRA = CommentsByMeTimeLineFragment.class.getName()
            + ":timeline_position_extra";

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private RemoveTask removeTask;

    private CommentListBean bean = new CommentListBean();
    private TimeLinePosition timeLinePosition;

    @Override
    public CommentListBean getList() {
        return bean;
    }

    public static CommentsByMeTimeLineFragment newInstance(AccountBean accountBean,
            UserBean userBean, String token) {
        CommentsByMeTimeLineFragment fragment = new CommentsByMeTimeLineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENTS_ACCOUNT_EXTRA, accountBean);
        bundle.putParcelable(ARGUMENTS_USER_EXTRA, userBean);
        bundle.putString(ARGUMENTS_TOKEN_EXTRA, token);
        fragment.setArguments(bundle);
        return fragment;
    }

    public CommentsByMeTimeLineFragment() {

    }

    protected void clearAndReplaceValue(CommentListBean value) {
        getList().getItemList().clear();
        getList().getItemList().addAll(value.getItemList());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getActivity().isChangingConfigurations()) {
            outState.putParcelable(ARGUMENTS_DATA_EXTRA, bean);
            outState.putSerializable(ARGUMENTS_TIMELINE_POSITION_EXTRA, timeLinePosition);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onListViewScrollStop() {
        super.onListViewScrollStop();
        timeLinePosition = Utility.getCurrentPositionFromListView(getListView());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!getActivity().isChangingConfigurations()) {
            CommentByMeTimeLineDBTask.asyncUpdatePosition(timeLinePosition, accountBean.getUid());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        userBean = getArguments().getParcelable(ARGUMENTS_USER_EXTRA);
        accountBean = getArguments().getParcelable(ARGUMENTS_ACCOUNT_EXTRA);
        token = getArguments().getString(ARGUMENTS_TOKEN_EXTRA);

        super.onActivityCreated(savedInstanceState);
        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                timeLinePosition = (TimeLinePosition) savedInstanceState
                        .getSerializable(ARGUMENTS_TIMELINE_POSITION_EXTRA);

                Loader<CommentTimeLineData> loader = getLoaderManager()
                        .getLoader(DB_CACHE_LOADER_ID);
                if (loader != null) {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }
                CommentListBean savedBean = savedInstanceState
                        .getParcelable(ARGUMENTS_DATA_EXTRA);
                if (savedBean != null && savedBean.getSize() > 0) {
                    clearAndReplaceValue(savedBean);
                    timeLineAdapter.notifyDataSetChanged();
                    refreshLayout(getList());
                    setListViewPositionFromPositionsCache();
                } else {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }
                break;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(onItemLongClickListener);
    }

    private AdapterView.OnItemLongClickListener onItemLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position - 1 < getList().getSize() && position - 1 >= 0) {
                if (actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                    getListView().setItemChecked(position, true);
                    timeLineAdapter.notifyDataSetChanged();
                    actionMode = getActivity().startActionMode(
                            new CommentSingleChoiceModeListener(getListView(), timeLineAdapter,
                                    CommentsByMeTimeLineFragment.this,
                                    getList().getItemList().get(position - 1)));
                    return true;
                } else {
                    getListView().setItemChecked(position, true);
                    timeLineAdapter.notifyDataSetChanged();
                    actionMode = getActivity().startActionMode(
                            new CommentSingleChoiceModeListener(getListView(), timeLineAdapter,
                                    CommentsByMeTimeLineFragment.this,
                                    getList().getItemList().get(position - 1)));
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible() && isVisibleToUser) {
//            ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);
        }
    }

    @Override
    public void removeItem(int position) {
        clearActionMode();
        if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            removeTask = new RemoveTask(GlobalContext.getInstance().getSpecialToken(),
                    getList().getItemList().get(position).getId(), position);
            removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void removeCancel() {
        clearActionMode();
    }

    private class RemoveTask extends MyAsyncTask<Void, Void, Boolean> {
        String token;
        String id;
        int positon;
        WeiboException e;

        public RemoveTask(String token, String id, int positon) {
            this.token = token;
            this.id = id;
            this.positon = positon;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DestroyCommentDao dao = new DestroyCommentDao(token, id);
            try {
                return dao.destroy();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return false;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            if (Utility.isAllNotNull(getActivity(), this.e)) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                ((CommentListAdapter) timeLineAdapter).removeItem(positon);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    private void setListViewPositionFromPositionsCache() {
        Utility.setListViewAdapterPosition(getListView(),
                timeLinePosition != null ? timeLinePosition.getPosition(bean) : 0,
                timeLinePosition != null ? timeLinePosition.top : 0, null);
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(this,
                getList().getItemList(),
                getListView(), true, false);
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        CommentFloatingMenu menu = new CommentFloatingMenu(getList().getItem(position));
        menu.show(getFragmentManager(), "");
    }

    @Override
    protected void newMsgLoaderSuccessCallback(CommentListBean newValue, Bundle loaderArgs) {
        if (newValue != null && newValue.getItemList() != null
                && newValue.getItemList().size() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            CommentByMeTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        }
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(CommentListBean newValue) {
        if (newValue != null && newValue.getItemList().size() > 1) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            CommentByMeTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        }
    }

    @Override
    protected void middleMsgLoaderSuccessCallback(int position, CommentListBean newValue,
            boolean towardsBottom) {
        if (getActivity() != null && newValue != null && newValue.getSize() > 0) {
            getList().addMiddleData(position, newValue, towardsBottom);
            getAdapter().notifyDataSetChanged();
            CommentByMeTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        }
    }

    private LoaderManager.LoaderCallbacks<CommentTimeLineData> dbCallback
            = new LoaderManager.LoaderCallbacks<CommentTimeLineData>() {
        @Override
        public Loader<CommentTimeLineData> onCreateLoader(int id, Bundle args) {
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
            return new CommentsByMeDBLoader(getActivity(),
                    GlobalContext.getInstance().getCurrentAccountId());
        }

        @Override
        public void onLoadFinished(Loader<CommentTimeLineData> loader, CommentTimeLineData result) {
            if (result != null) {
                clearAndReplaceValue(result.cmtList);
                timeLinePosition = result.position;
            }

            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            setListViewPositionFromPositionsCache();

            refreshLayout(getList());
            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */
            if (getList().getSize() == 0) {
                getPullToRefreshListView().setRefreshing();
                loadNewMsg();
            }

            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<CommentTimeLineData> loader) {

        }
    };

    protected Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateNewMsgLoader(int id,
            Bundle args) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new CommentsByMeMsgLoader(getActivity(), accountId, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateMiddleMsgLoader(int id,
            Bundle args, String middleBeginId, String middleEndId, String middleEndTag,
            int middlePosition) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        return new CommentsByMeMsgLoader(getActivity(), accountId, token, middleBeginId,
                middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateOldMsgLoader(int id,
            Bundle args) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        String maxId = null;
        if (getList().getItemList().size() > 0) {
            maxId = getList().getItemList().get(getList().getItemList().size() - 1).getId();
        }
        return new CommentsByMeMsgLoader(getActivity(), accountId, token, null, maxId);
    }
}
