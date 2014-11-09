package org.qii.weiciyuan.ui.maintimeline;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.MessageReCmtCountBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.bean.android.MentionTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.dao.maintimeline.TimeLineReCmtCountDao;
import org.qii.weiciyuan.dao.unread.ClearUnreadDao;
import org.qii.weiciyuan.othercomponent.AppNotificationCenter;
import org.qii.weiciyuan.support.database.MentionWeiboTimeLineDBTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.TopTipBar;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.adapter.StatusListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.loader.MentionsWeiboMsgLoader;
import org.qii.weiciyuan.ui.loader.MentionsWeiboTimeDBLoader;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.main.MentionsTimeLine;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-7-29
 * Weibo repost message timeline
 */
public class MentionsWeiboTimeLineFragment
        extends AbstractMessageTimeLineFragment<MessageListBean> {

    private static final String ARGUMENTS_ACCOUNT_EXTRA = MentionsWeiboTimeLineFragment.class.getName() + ":account_extra";
    private static final String ARGUMENTS_USER_EXTRA = MentionsWeiboTimeLineFragment.class.getName() + ":userBean_extra";
    private static final String ARGUMENTS_TOKEN_EXTRA = MentionsWeiboTimeLineFragment.class.getName() + ":token_extra";
    private static final String ARGUMENTS_DATA_EXTRA = MentionsWeiboTimeLineFragment.class.getName() + ":msg_extra";
    private static final String ARGUMENTS_TIMELINE_POSITION_EXTRA = MentionsWeiboTimeLineFragment.class.getName()
            + ":timeline_position_extra";

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private TimeLinePosition timeLinePosition;
    private MessageListBean bean = new MessageListBean();

    private final int POSITION_IN_PARENT_FRAGMENT = 0;

    @Override
    public MessageListBean getList() {
        return bean;
    }

    public static MentionsWeiboTimeLineFragment newInstance(AccountBean accountBean,
            UserBean userBean, String token) {
        MentionsWeiboTimeLineFragment fragment = new MentionsWeiboTimeLineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENTS_ACCOUNT_EXTRA, accountBean);
        bundle.putParcelable(ARGUMENTS_USER_EXTRA, userBean);
        bundle.putString(ARGUMENTS_TOKEN_EXTRA, token);
        fragment.setArguments(bundle);
        return fragment;
    }

    public MentionsWeiboTimeLineFragment() {

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
            MentionWeiboTimeLineDBTask.asyncUpdatePosition(timeLinePosition,
                    accountBean.getUid());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newMsgTipBar.setType(TopTipBar.Type.ALWAYS);
    }

    @Override
    protected void onListViewScrollStop() {
        super.onListViewScrollStop();
        timeLinePosition = Utility.getCurrentPositionFromListView(getListView());
    }

    @Override
    protected void buildListAdapter() {
        StatusListAdapter adapter = new StatusListAdapter(this,
                getList().getItemList(),
                getListView(), true, false);
        adapter.setTopTipBar(newMsgTipBar);
        timeLineAdapter = adapter;
        getListView().setAdapter(timeLineAdapter);
    }

    private void setActionBarTabCount(int count) {
        MentionsTimeLine parent = (MentionsTimeLine) getParentFragment();
        ActionBar.Tab tab = parent.getWeiboTab();
        if (tab == null) {
            return;
        }
        String tabTag = (String) tab.getTag();
        if (MentionsWeiboTimeLineFragment.class.getName().equals(tabTag)) {
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

    @Override
    protected void newMsgLoaderSuccessCallback(MessageListBean newValue, Bundle loaderArgs) {
        if (getActivity() != null && newValue.getSize() > 0) {
            addNewDataAndRememberPosition(newValue);
        }
    }

    private void addNewDataAndRememberPosition(final MessageListBean newValue) {
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
            List<MessageBean> unreadData = newValue.getItemList();
            for (MessageBean message : unreadData) {
                if (message != null) {
                    MentionsWeiboTimeLineFragment.this.timeLinePosition.newMsgIds
                            .add(message.getIdLong());
                }
            }
            newMsgTipBar
                    .setValue(
                            MentionsWeiboTimeLineFragment.this.timeLinePosition.newMsgIds);
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
                MentionsWeiboTimeLineFragment.this.timeLinePosition.newMsgIds.size());
        MentionWeiboTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
    }

    protected void middleMsgLoaderSuccessCallback(int position, MessageListBean newValue,
            boolean towardsBottom) {
        if (getActivity() != null && newValue != null && newValue.getSize() > 0) {
            getList().addMiddleData(position, newValue, towardsBottom);
            getAdapter().notifyDataSetChanged();
            MentionWeiboTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        }
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {
            getList().addOldData(newValue);
            MentionWeiboTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getActivity().isChangingConfigurations()) {
            outState.putParcelable(ARGUMENTS_DATA_EXTRA, bean);
            outState.putSerializable(ARGUMENTS_TIMELINE_POSITION_EXTRA, timeLinePosition);
        }
    }

    private void setLeftMenuUnreadCount(int count) {
        MainTimeLineActivity mainTimeLineActivity = (MainTimeLineActivity) getActivity();
        if (mainTimeLineActivity == null) {
            return;
        }
        mainTimeLineActivity.setMentionsWeiboCount(count);
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

                Loader<MentionTimeLineData> loader = getLoaderManager()
                        .getLoader(DB_CACHE_LOADER_ID);
                if (loader != null) {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }

                MessageListBean savedBean = savedInstanceState
                        .getParcelable(ARGUMENTS_DATA_EXTRA);
                if (savedBean != null && savedBean.getSize() > 0) {
                    getList().replaceData(savedBean);
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
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        startActivityForResult(
                BrowserWeiboMsgActivity.newIntent(bean.getItemList().get(position),
                        GlobalContext.getInstance().getSpecialToken()),
                MainTimeLineActivity.REQUEST_CODE_UPDATE_MENTIONS_WEIBO_TIMELINE_COMMENT_REPOST_COUNT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //use Up instead of Back to reach this fragment
        if (data == null) {
            return;
        }
        final MessageBean msg = data.getParcelableExtra("msg");
        if (msg != null) {
            for (int i = 0; i < getList().getSize(); i++) {
                if (msg.equals(getList().getItem(i))) {
                    MessageBean ori = getList().getItem(i);
                    if (ori.getComments_count() != msg.getComments_count()
                            || ori.getReposts_count() != msg.getReposts_count()) {
                        ori.setReposts_count(msg.getReposts_count());
                        ori.setComments_count(msg.getComments_count());
                        MentionWeiboTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
                        getAdapter().notifyDataSetChanged();
                    }
                    break;
                }
            }
        }
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

    private LoaderManager.LoaderCallbacks<MentionTimeLineData> dbCallback
            = new LoaderManager.LoaderCallbacks<MentionTimeLineData>() {
        @Override
        public Loader<MentionTimeLineData> onCreateLoader(int id, Bundle args) {
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
            return new MentionsWeiboTimeDBLoader(getActivity(),
                    GlobalContext.getInstance().getCurrentAccountId());
        }

        @Override
        public void onLoadFinished(Loader<MentionTimeLineData> loader, MentionTimeLineData result) {
            getPullToRefreshListView().setVisibility(View.VISIBLE);

            if (result != null) {
                getList().replaceData(result.msgList);
                timeLinePosition = result.position;
            }

            getAdapter().notifyDataSetChanged();
            setListViewPositionFromPositionsCache();
            refreshLayout(bean);

            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */

            if (bean.getSize() == 0) {
                pullToRefreshListView.setRefreshing();
                loadNewMsg();
            } else {
                new RefreshReCmtCountTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }

            getLoaderManager().destroyLoader(loader.getId());

            AppNotificationCenter.getInstance().addCallback(callback);
        }

        @Override
        public void onLoaderReset(Loader<MentionTimeLineData> loader) {

        }
    };

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateNewMsgLoader(int id,
            Bundle args) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new MentionsWeiboMsgLoader(getActivity(), accountId, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateMiddleMsgLoader(int id,
            Bundle args, String middleBeginId, String middleEndId, String middleEndTag,
            int middlePosition) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        return new MentionsWeiboMsgLoader(getActivity(), accountId, token, middleBeginId,
                middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateOldMsgLoader(int id,
            Bundle args) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        String maxId = null;
        if (getList().getItemList().size() > 0) {
            maxId = getList().getItemList().get(getList().getItemList().size() - 1).getId();
        }
        return new MentionsWeiboMsgLoader(getActivity(), accountId, token, null, maxId);
    }

    private AppNotificationCenter.Callback callback = new AppNotificationCenter.Callback() {

        @Override
        public void unreadMentionsChanged(AccountBean account, MessageListBean data) {
            super.unreadMentionsChanged(account, data);
            if (!accountBean.equals(account)) {
                return;
            }

            addUnreadMessage(data);
            clearUnreadMentions(AppNotificationCenter.getInstance().getUnreadBean(account));
        }
    };

    private void addUnreadMessage(MessageListBean data) {
        if (data != null && data.getSize() > 0) {
            MessageBean last = data.getItem(data.getSize() - 1);
            boolean dup = getList().getItemList().contains(last);
            if (!dup) {
                addNewDataAndRememberPosition(data);
            }
        }
    }

    private class RefreshReCmtCountTask
            extends MyAsyncTask<Void, List<MessageReCmtCountBean>, List<MessageReCmtCountBean>> {

        List<String> msgIds;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            msgIds = new ArrayList<String>();
            List<MessageBean> msgList = getList().getItemList();
            for (MessageBean msg : msgList) {
                if (msg != null) {
                    msgIds.add(msg.getId());
                }
            }
        }

        @Override
        protected List<MessageReCmtCountBean> doInBackground(Void... params) {
            try {
                return new TimeLineReCmtCountDao(GlobalContext.getInstance().getSpecialToken(),
                        msgIds).get();
            } catch (WeiboException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MessageReCmtCountBean> value) {
            super.onPostExecute(value);
            if (getActivity() == null || value == null) {
                return;
            }

            for (int i = 0; i < value.size(); i++) {
                MessageBean msg = getList().getItem(i);
                MessageReCmtCountBean count = value.get(i);
                if (msg != null && msg.getId().equals(count.getId())) {
                    msg.setReposts_count(count.getReposts());
                    msg.setComments_count(count.getComments());
                }
            }
            MentionWeiboTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
            getAdapter().notifyDataSetChanged();
        }
    }

    private void clearUnreadMentions(final UnreadBean data) {
        new MyAsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    new ClearUnreadDao(
                            GlobalContext.getInstance().getAccountBean().getAccess_token())
                            .clearMentionStatusUnread(data,
                                    GlobalContext.getInstance().getAccountBean().getUid());
                } catch (WeiboException ignored) {

                }
                return null;
            }
        }.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }
}

