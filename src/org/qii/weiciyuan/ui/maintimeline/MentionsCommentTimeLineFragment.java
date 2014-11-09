package org.qii.weiciyuan.ui.maintimeline;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.bean.android.CommentTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.dao.destroy.DestroyCommentDao;
import org.qii.weiciyuan.dao.unread.ClearUnreadDao;
import org.qii.weiciyuan.othercomponent.AppNotificationCenter;
import org.qii.weiciyuan.othercomponent.unreadnotification.NotificationServiceHelper;
import org.qii.weiciyuan.support.database.MentionCommentsTimeLineDBTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.TopTipBar;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.actionmenu.CommentFloatingMenu;
import org.qii.weiciyuan.ui.actionmenu.CommentSingleChoiceModeListener;
import org.qii.weiciyuan.ui.adapter.CommentListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.IRemoveItem;
import org.qii.weiciyuan.ui.loader.MentionsCommentDBLoader;
import org.qii.weiciyuan.ui.loader.MentionsCommentMsgLoader;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.main.MentionsTimeLine;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * User: qii
 * Date: 13-1-23
 */
public class MentionsCommentTimeLineFragment extends AbstractTimeLineFragment<CommentListBean>
        implements IRemoveItem {

    private static final String ARGUMENTS_ACCOUNT_EXTRA = MentionsCommentTimeLineFragment.class.getName() + ":account_extra";
    private static final String ARGUMENTS_USER_EXTRA = MentionsCommentTimeLineFragment.class.getName() + ":userBean_extra";
    private static final String ARGUMENTS_TOKEN_EXTRA = MentionsCommentTimeLineFragment.class.getName() + ":token_extra";
    private static final String ARGUMENTS_DATA_EXTRA = MentionsCommentTimeLineFragment.class.getName() + ":msg_extra";
    private static final String ARGUMENTS_TIMELINE_POSITION_EXTRA = MentionsCommentTimeLineFragment.class.getName()
            + ":timeline_position_extra";

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private RemoveTask removeTask;

    private CommentListBean bean = new CommentListBean();
    private TimeLinePosition timeLinePosition;

    private final int POSITION_IN_PARENT_FRAGMENT = 1;

    @Override
    public CommentListBean getList() {
        return bean;
    }

    public static MentionsCommentTimeLineFragment newInstance(AccountBean accountBean,
            UserBean userBean,
            String token) {
        MentionsCommentTimeLineFragment fragment = new MentionsCommentTimeLineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENTS_ACCOUNT_EXTRA, accountBean);
        bundle.putParcelable(ARGUMENTS_USER_EXTRA, userBean);
        bundle.putString(ARGUMENTS_TOKEN_EXTRA, token);
        fragment.setArguments(bundle);
        return fragment;
    }

    public MentionsCommentTimeLineFragment() {

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
    protected void onListViewScrollStop() {
        super.onListViewScrollStop();
        timeLinePosition = Utility.getCurrentPositionFromListView(getListView());
    }

    @Override
    public void onResume() {
        super.onResume();
        setListViewPositionFromPositionsCache();

        showUIUnreadCount(newMsgTipBar.getValues().size());

        newMsgTipBar.setOnChangeListener(new TopTipBar.OnChangeListener() {
            @Override
            public void onChange(int count) {

                showUIUnreadCount(newMsgTipBar.getValues().size());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!getActivity().isChangingConfigurations()) {
            saveTimeLinePositionToDB();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppNotificationCenter.getInstance().removeCallback(callback);
    }

    private void saveTimeLinePositionToDB() {
        TimeLinePosition current = Utility.getCurrentPositionFromListView(getListView());

        if (!current.isEmpty()) {
            timeLinePosition = current;
            timeLinePosition.newMsgIds = newMsgTipBar.getValues();
        }

        if (timeLinePosition != null) {
            MentionCommentsTimeLineDBTask.asyncUpdatePosition(timeLinePosition,
                    accountBean.getUid());
        }
    }

    private void setActionBarTabCount(int count) {
        MentionsTimeLine parent = (MentionsTimeLine) getParentFragment();
        ActionBar.Tab tab = parent.getCommentTab();
        if (tab == null) {
            return;
        }
        String tabTag = (String) tab.getTag();
        if (MentionsCommentTimeLineFragment.class.getName().equals(tabTag)) {
            View customView = tab.getCustomView();
            TextView countTV = (TextView) customView.findViewById(R.id.tv_home_count);
            countTV.setText(String.valueOf(count));
            if (count > 0) {
                countTV.setVisibility(View.VISIBLE);
            } else {
                countTV.setVisibility(View.GONE);
            }
        }
    }

    private void setLeftMenuUnreadCount(int count) {
        MainTimeLineActivity mainTimeLineActivity = (MainTimeLineActivity) getActivity();
        if (mainTimeLineActivity == null) {
            return;
        }
        mainTimeLineActivity.setMentionsCommentCount(count);
    }

    private void showUIUnreadCount(int count) {
        setActionBarTabCount(count);
        setLeftMenuUnreadCount(count);
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
                CommentListBean savedBean = savedInstanceState
                        .getParcelable(ARGUMENTS_DATA_EXTRA);

                Loader<CommentTimeLineData> loader = getLoaderManager()
                        .getLoader(DB_CACHE_LOADER_ID);
                if (loader != null) {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }

                if (savedBean != null && savedBean.getSize() > 0) {
                    clearAndReplaceValue(savedBean);
                    timeLineAdapter.notifyDataSetChanged();
                    refreshLayout(getList());
                    AppNotificationCenter.getInstance().addCallback(callback);
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
        newMsgTipBar.setType(TopTipBar.Type.ALWAYS);
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
                                    MentionsCommentTimeLineFragment.this,
                                    getList().getItemList().get(position - 1)));
                    return true;
                } else {
                    getListView().setItemChecked(position, true);
                    timeLineAdapter.notifyDataSetChanged();
                    actionMode = getActivity().startActionMode(
                            new CommentSingleChoiceModeListener(getListView(), timeLineAdapter,
                                    MentionsCommentTimeLineFragment.this,
                                    getList().getItemList().get(position - 1)));
                    return true;
                }
            }
            return false;
        }
    };

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

    class RemoveTask extends MyAsyncTask<Void, Void, Boolean> {

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
                timeLinePosition != null ? timeLinePosition.top : 0, new Runnable() {
                    @Override
                    public void run() {
                        setListViewUnreadTipBar(timeLinePosition);
                    }
                });
    }

    private void setListViewUnreadTipBar(TimeLinePosition p) {
        if (p != null && p.newMsgIds != null) {
            newMsgTipBar.setValue(p.newMsgIds);
            showUIUnreadCount(newMsgTipBar.getValues().size());
        }
    }

    @Override
    protected void buildListAdapter() {
        CommentListAdapter adapter = new CommentListAdapter(this,
                getList().getItemList(),
                getListView(), true, false);
        adapter.setTopTipBar(newMsgTipBar);
        timeLineAdapter = adapter;
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        CommentFloatingMenu menu = new CommentFloatingMenu(getList().getItem(position));
        menu.show(getFragmentManager(), "");
    }

    @Override
    protected void newMsgLoaderSuccessCallback(CommentListBean newValue, Bundle loaderArgs) {
        if (newValue != null && newValue.getItemList().size() > 0) {
            addNewDataAndRememberPosition(newValue);
        }

        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationServiceHelper
                .getMentionsCommentNotificationId(GlobalContext.getInstance().getAccountBean()));
    }

    private void addNewDataAndRememberPosition(final CommentListBean newValue) {
        AppLogger.i("Add new unread data to memory cache");
        if (getActivity() == null || newValue.getSize() == 0) {
            AppLogger.i("Activity is destroyed or new data count is zero, give up");
            return;
        }

        final boolean isDataSourceEmpty = getList().getSize() == 0;
        TimeLinePosition previousPosition = Utility.getCurrentPositionFromListView(getListView());
        getList().addNewData(newValue);
        if (isDataSourceEmpty) {
            newMsgTipBar.setValue(newValue, true);
            newMsgTipBar.clearAndReset();
            getAdapter().notifyDataSetChanged();
            AppLogger
                    .i("Init data source is empty, ListView jump to zero position after refresh, first time open app? ");
            getListView().setSelection(0);
            saveTimeLinePositionToDB();
        } else {

            if (previousPosition.isEmpty() && timeLinePosition != null) {
                previousPosition = timeLinePosition;
            }
            AppLogger.i("Previous first visible item id " + previousPosition.firstItemId);
            getAdapter().notifyDataSetChanged();
            List<CommentBean> unreadData = newValue.getItemList();
            for (CommentBean comment : unreadData) {
                if (comment != null) {
                    MentionsCommentTimeLineFragment.this.timeLinePosition.newMsgIds
                            .add(comment.getIdLong());
                }
            }
            newMsgTipBar
                    .setValue(
                            MentionsCommentTimeLineFragment.this.timeLinePosition.newMsgIds);
            int positionInAdapter = Utility.getAdapterPositionFromItemId(getAdapter(),
                    previousPosition.firstItemId);
            //use 1 px to show newMsgTipBar
            AppLogger.i("ListView restore to previous position " + positionInAdapter);
            getListView().getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            getListView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            AppLogger.i("Save ListView position to database");
                            saveTimeLinePositionToDB();
                        }
                    });
            Utility.setListViewAdapterPosition(getListView(), positionInAdapter,
                    previousPosition.top - 1,
                    null);
        }

        showUIUnreadCount(
                MentionsCommentTimeLineFragment.this.timeLinePosition.newMsgIds.size());
        MentionCommentsTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
    }

    protected void middleMsgLoaderSuccessCallback(int position, CommentListBean newValue,
            boolean towardsBottom) {
        if (getActivity() != null && newValue != null && newValue.getSize() > 0) {
            getList().addMiddleData(position, newValue, towardsBottom);
            getAdapter().notifyDataSetChanged();
            MentionCommentsTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        }
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(CommentListBean newValue) {
        if (newValue != null && newValue.getItemList().size() > 1) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            MentionCommentsTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        }
    }

    private LoaderManager.LoaderCallbacks<CommentTimeLineData> dbCallback
            = new LoaderManager.LoaderCallbacks<CommentTimeLineData>() {
        @Override
        public Loader<CommentTimeLineData> onCreateLoader(int id, Bundle args) {
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
            return new MentionsCommentDBLoader(getActivity(),
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
            AppNotificationCenter.getInstance().addCallback(callback);
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
        return new MentionsCommentMsgLoader(getActivity(), accountId, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateMiddleMsgLoader(int id,
            Bundle args, String middleBeginId, String middleEndId, String middleEndTag,
            int middlePosition) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        return new MentionsCommentMsgLoader(getActivity(), accountId, token, middleBeginId,
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
        return new MentionsCommentMsgLoader(getActivity(), accountId, token, null, maxId);
    }

    private AppNotificationCenter.Callback callback = new AppNotificationCenter.Callback() {
        @Override
        public void unreadMentionsCommentChanged(AccountBean account, CommentListBean data) {
            super.unreadMentionsCommentChanged(account, data);
            if (!accountBean.equals(account)) {
                return;
            }

            addUnreadMessage(data);
            clearUnreadMentions(AppNotificationCenter.getInstance().getUnreadBean(account));
        }
    };

    private void addUnreadMessage(CommentListBean data) {
        if (data != null && data.getSize() > 0) {
            CommentBean last = data.getItem(data.getSize() - 1);
            boolean dup = getList().getItemList().contains(last);
            if (!dup) {
                addNewDataAndRememberPosition(data);
            }
        }
    }

    private void clearUnreadMentions(final UnreadBean data) {
        new MyAsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    new ClearUnreadDao(
                            GlobalContext.getInstance().getAccountBean().getAccess_token())
                            .clearMentionCommentUnread(data,
                                    GlobalContext.getInstance().getAccountBean().getUid());
                } catch (WeiboException ignored) {

                }
                return null;
            }
        }.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }
}
