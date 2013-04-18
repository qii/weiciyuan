package org.qii.weiciyuan.ui.basefragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.interfaces.ICommander;

/**
 * User: qii
 * Date: 12-8-27
 * weiciyuan has two kinds of methods to send/receive network request/response asynchronously,
 * one is setRetainInstance(true) + AsyncTask, the other is AsyncTaskLoader
 * Because nested fragment(parent fragment has a viewpager, viewpager has many children fragments,
 * these children fragments are called nested fragment) can't use setRetainInstance(true), at this moment
 * you have to use AsyncTaskLoader to solve Android configuration change(for example: change screen orientation,
 * change system language)
 */
public abstract class AbstractTimeLineFragment<T extends ListBean> extends AbstractAppFragment {


    protected PullToRefreshListView pullToRefreshListView;
    protected TextView empty;
    protected ProgressBar progressBar;

    protected BaseAdapter timeLineAdapter;

    protected View footerView;
    protected TimeLineBitmapDownloader commander;

    protected TimeLineGetNewMsgListTask newTask;
    protected TimeLineGetOlderMsgListTask oldTask;
    protected TimeLineGetMiddleMsgListTask middleTask;

    protected boolean newMsgLoaderIsLoading = false;
    protected boolean middleMsgLoaderIsLoading = false;
    protected boolean oldMsgLoaderIsLoading = false;

    protected static final int DB_CACHE_LOADER_ID = 0;
    protected static final int NEW_MSG_LOADER_ID = 1;
    protected static final int MIDDLE_MSG_LOADER_ID = 2;
    protected static final int OLD_MSG_LOADER_ID = 3;

    protected ActionMode mActionMode;

    public abstract T getList();

    private int listViewScrollState = -1;

    public int getListViewScrollState() {
        return listViewScrollState;
    }

    public PullToRefreshListView getPullToRefreshListView() {
        return pullToRefreshListView;
    }

    public ListView getListView() {
        return pullToRefreshListView.getRefreshableView();
    }

    public BaseAdapter getAdapter() {
        return timeLineAdapter;
    }

    protected void refreshLayout(T bean) {
        if (bean != null && bean.getSize() > 0) {
//            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
//            listView.setVisibility(View.VISIBLE);
        } else if (bean == null || bean.getSize() == 0) {
//            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
//            listView.setVisibility(View.VISIBLE);
        } else if (bean.getSize() == bean.getTotal_number()) {
//            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
//            listView.setVisibility(View.VISIBLE);
        }
    }

    protected abstract void listViewItemClick(AdapterView parent, View view, int position, long id);

    protected void listViewFooterViewClick(View view) {
        if (Utility.isTaskStopped(oldTask)) {
            oldTask = new TimeLineGetOlderMsgListTask();
            oldTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void loadMiddleMsg(String beginId, String endId, String endTag, int position) {
        if (Utility.isTaskStopped(middleTask)) {
            middleTask = new TimeLineGetMiddleMsgListTask(beginId, endId, endTag, position);
            middleTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_layout, container, false);

        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);

        getListView().setHeaderDividersEnabled(false);
        footerView = inflater.inflate(R.layout.listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pullToRefreshListView.setOnRefreshListener(listViewOnRefreshListener);
        pullToRefreshListView.setOnLastItemVisibleListener(listViewOnLastItemVisibleListener);
        pullToRefreshListView.setOnScrollListener(listViewOnScrollListener);
        pullToRefreshListView.setOnItemClickListener(listViewOnItemClickListener);
        buildListAdapter();
    }

    protected void showFooterView() {
        TextView tv = ((TextView) footerView.findViewById(R.id.listview_footer));
        tv.setVisibility(View.VISIBLE);
        tv.setText(getString(R.string.loading));
        View view = footerView.findViewById(R.id.refresh);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));
    }

    private PullToRefreshBase.OnLastItemVisibleListener listViewOnLastItemVisibleListener = new PullToRefreshBase.OnLastItemVisibleListener() {
        @Override
        public void onLastItemVisible() {
            listViewFooterViewClick(null);
        }
    };

    private PullToRefreshBase.OnRefreshListener<ListView> listViewOnRefreshListener = new PullToRefreshBase.OnRefreshListener<ListView>() {
        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            refresh();
        }
    };

    private AdapterView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mActionMode != null) {
                getListView().clearChoices();
                mActionMode.finish();
                mActionMode = null;
                return;
            }
            getListView().clearChoices();
            if (position - 1 < getList().getSize() && position - 1 >= 0) {
                int index = position - 1;
                ItemBean msg = getList().getItem(index);

                if (msg != null) {
                    listViewItemClick(parent, view, index, id);

                } else {
                    String beginId = getList().getItem(index - 1).getId();
                    String endTag = getList().getItem(index + 1).getId();
                    String endId = getList().getItem(index + 2).getId();

                    loadMiddleMsg(beginId, endId, endTag, index);

                    Toast.makeText(getActivity(), getString(R.string.loading_middle_msg), Toast.LENGTH_SHORT).show();
                }

            } else if (position - 1 >= getList().getSize()) {

                listViewFooterViewClick(view);
            }
        }
    };

    private AbsListView.OnScrollListener listViewOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            listViewScrollState = scrollState;
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    if (!enableRefreshTime) {
                        enableRefreshTime = true;
                        getAdapter().notifyDataSetChanged();
                    }
                    onListViewScrollStop();
                    LongClickableLinkMovementMethod.getInstance().setLongClickable(true);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    enableRefreshTime = false;
                    LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    enableRefreshTime = true;
                    LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            onListViewScroll();
        }
    };

    protected void onListViewScrollStop() {

    }

    protected void onListViewScroll() {

        LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();

        if (hasActionMode()) {
            int position = getListView().getCheckedItemPosition();
            if (getListView().getFirstVisiblePosition() > position || getListView().getLastVisiblePosition() < position) {
                clearActionMode();
            }
        }
    }

    protected void dismissFooterView() {
        footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
        footerView.findViewById(R.id.refresh).clearAnimation();
        footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
    }


    protected void showErrorFooterView() {
        TextView tv = ((TextView) footerView.findViewById(R.id.listview_footer));
        tv.setVisibility(View.VISIBLE);
        tv.setText(getString(R.string.click_to_load_older_message));
        View view = footerView.findViewById(R.id.refresh);
        view.clearAnimation();
        view.setVisibility(View.GONE);
    }

    public void clearActionMode() {
        if (mActionMode != null) {

            mActionMode.finish();
            mActionMode = null;
        }
        if (pullToRefreshListView != null && getListView().getCheckedItemCount() > 0) {
            getListView().clearChoices();
            if (getAdapter() != null) getAdapter().notifyDataSetChanged();
        }
    }

    public boolean clearActionModeIfOpen() {
        boolean flag = false;
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
            flag = true;
        }
        if (pullToRefreshListView != null && getListView().getCheckedItemCount() > 0) {
            getListView().clearChoices();
            if (getAdapter() != null) getAdapter().notifyDataSetChanged();
        }
        return flag;
    }

    protected abstract void buildListAdapter();

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(newTask, oldTask, middleTask);
    }


    protected boolean canSwitchGroup() {
        if (newTask != null && newTask.getStatus() != MyAsyncTask.Status.FINISHED) {
            return false;
        }
        if (oldTask != null && oldTask.getStatus() != MyAsyncTask.Status.FINISHED) {
            return false;
        }
        if (middleTask != null && middleTask.getStatus() != MyAsyncTask.Status.FINISHED) {
            return false;
        }
        return true;
    }


    public void refresh() {
        if (allowRefresh()) {
            newTask = new TimeLineGetNewMsgListTask();
            newTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            Activity activity = getActivity();
            if (activity == null)
                return;
            ((ICommander) activity).getBitmapDownloader().totalStopLoadPicture();

        }

    }

    protected boolean allowRefresh() {
        return Utility.isTaskStopped(newTask) && getPullToRefreshListView().getVisibility() == View.VISIBLE;
    }


    @Override
    public void onResume() {
        super.onResume();
        getListView().setFastScrollEnabled(SettingUtility.allowFastScroll());
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("newMsgLoaderIsLoading", newMsgLoaderIsLoading);
        outState.putBoolean("middleMsgLoaderIsLoading", middleMsgLoaderIsLoading);
        outState.putBoolean("oldMsgLoaderIsLoading", oldMsgLoaderIsLoading);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((ICommander) getActivity()).getBitmapDownloader();
        if (savedInstanceState != null) {
            newMsgLoaderIsLoading = savedInstanceState.getBoolean("newMsgLoaderIsLoading");
            middleMsgLoaderIsLoading = savedInstanceState.getBoolean("middleMsgLoaderIsLoading");
            oldMsgLoaderIsLoading = savedInstanceState.getBoolean("oldMsgLoaderIsLoading");
        }
        if (newMsgLoaderIsLoading) {
            getLoaderManager().initLoader(NEW_MSG_LOADER_ID, null, msgCallback);
        } else {
            getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        }

        if (middleMsgLoaderIsLoading) {
            getLoaderManager().initLoader(MIDDLE_MSG_LOADER_ID, null, msgCallback);
        } else {
            getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        }

        if (oldMsgLoaderIsLoading) {
            getLoaderManager().initLoader(OLD_MSG_LOADER_ID, null, msgCallback);
        } else {
            getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        }

    }

    public void setmActionMode(ActionMode mActionMode) {
        this.mActionMode = mActionMode;
    }

    public boolean hasActionMode() {
        return mActionMode != null;
    }


    protected abstract void newMsgOnPostExecute(T newValue);

    protected abstract void oldMsgOnPostExecute(T newValue);

    public class TimeLineGetNewMsgListTask extends MyAsyncTask<Object, T, T> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();
            clearActionMode();
            Utility.stopListViewScrollingAndScrollToTop(getListView());
        }

        @Override
        protected T doInBackground(Object... params) {

            try {
                return getDoInBackgroundNewData();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;

        }

        @Override
        protected void onPostExecute(T newValue) {
            newMsgOnPostExecute(newValue);
            cleanWork();
            super.onPostExecute(newValue);
        }

        @Override
        protected void onCancelled(T messageListBean) {
            super.onCancelled(messageListBean);
            if (getActivity() != null) {
                if (this.e != null)
                    Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
                cleanWork();
            }
        }

        private void cleanWork() {
            refreshLayout(getList());
            getPullToRefreshListView().onRefreshComplete();

        }
    }


    public class TimeLineGetOlderMsgListTask extends MyAsyncTask<Object, T, T> {

        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();

            showFooterView();
            clearActionMode();
        }

        @Override
        protected T doInBackground(Object... params) {

            try {
                return getDoInBackgroundOldData();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;

        }

        @Override
        protected void onPostExecute(T newValue) {
            oldMsgOnPostExecute(newValue);

            cleanWork();
            super.onPostExecute(newValue);
        }

        @Override
        protected void onCancelled(T messageListBean) {
            super.onCancelled(messageListBean);
            if (getActivity() != null) {
                if (Utility.isAllNotNull(this.e, getActivity())) {
                    Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
                    showErrorFooterView();
                    getPullToRefreshListView().onRefreshComplete();
                } else {
                    dismissFooterView();
                }
            }

        }

        private void cleanWork() {
            getPullToRefreshListView().onRefreshComplete();
            getAdapter().notifyDataSetChanged();
            dismissFooterView();
        }
    }


    public class TimeLineGetMiddleMsgListTask extends MyAsyncTask<Object, T, T> {

        WeiboException e;
        String beginId;
        String endId;
        String endTag;
        int position;

        public TimeLineGetMiddleMsgListTask(String beginId, String endId, String endTag, int position) {
            this.beginId = beginId;
            this.endId = endId;
            this.endTag = endTag;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            clearActionMode();
        }

        @Override
        protected T doInBackground(Object... params) {

            try {
                return getDoInBackgroundMiddleData(beginId, endId);
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;

        }

        @Override
        protected void onPostExecute(T newValue) {
            middleMsgOnPostExecute(endTag, position, newValue);
            getAdapter().notifyDataSetChanged();
            super.onPostExecute(newValue);
        }

    }


    protected void middleMsgOnPostExecute(String endTag, int position, T newValue) {

        if (newValue == null)
            return;

        if (newValue.getSize() == 1) {
            getList().getItemList().remove(position);
            getAdapter().notifyDataSetChanged();
            return;
        }

        ItemBean lastItem = newValue.getItem(newValue.getSize() - 1);

        if (!lastItem.getId().equals(endTag)) {
            getList().getItemList().addAll(position, newValue.getItemList().subList(1, newValue.getSize()));
            getAdapter().notifyDataSetChanged();
            return;
        }

        if (lastItem.getId().equals(endTag)) {
            int nullIndex = position + newValue.getSize() - 1;
            getList().getItemList().addAll(position, newValue.getItemList().subList(1, newValue.getSize()));
            getList().getItemList().remove(nullIndex - 1);
            getList().getItemList().remove(nullIndex - 1);
            getAdapter().notifyDataSetChanged();
            return;
        }


    }

    protected void showListView() {
        progressBar.setVisibility(View.INVISIBLE);
    }


    protected abstract T getDoInBackgroundNewData() throws WeiboException;

    protected abstract T getDoInBackgroundOldData() throws WeiboException;

    protected abstract T getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException;


    private volatile boolean enableRefreshTime = true;

    public boolean isListViewFling() {
        return !enableRefreshTime;
    }


    protected Loader<AsyncTaskLoaderResult<T>> onCreateNewMsgLoader(int id, Bundle args) {
        return null;
    }

    protected Loader<AsyncTaskLoaderResult<T>> onCreateMiddleMsgLoader(int id, Bundle args, String middleBeginId, String middleEndId, String middleEndTag, int middlePosition) {
        return null;
    }

    protected Loader<AsyncTaskLoaderResult<T>> onCreateOldMsgLoader(int id, Bundle args) {
        return null;
    }

    protected LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<T>> msgCallback = new LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<T>>() {

        private String middleBeginId = "";
        private String middleEndId = "";
        private String middleEndTag = "";
        private int middlePosition = -1;

        @Override
        public Loader<AsyncTaskLoaderResult<T>> onCreateLoader(int id, Bundle args) {
            clearActionMode();
            showListView();
            switch (id) {
                case NEW_MSG_LOADER_ID:
                    newMsgLoaderIsLoading = true;
                    Utility.stopListViewScrollingAndScrollToTop(getListView());
                    return onCreateNewMsgLoader(id, args);
                case MIDDLE_MSG_LOADER_ID:
                    middleMsgLoaderIsLoading = true;
                    middleBeginId = args.getString("beginId");
                    middleEndId = args.getString("endId");
                    middleEndTag = args.getString("endTag");
                    middlePosition = args.getInt("position");
                    return onCreateMiddleMsgLoader(id, args, middleBeginId, middleEndId, middleEndTag, middlePosition);
                case OLD_MSG_LOADER_ID:
                    oldMsgLoaderIsLoading = true;
                    showFooterView();
                    return onCreateOldMsgLoader(id, args);
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<AsyncTaskLoaderResult<T>> loader, AsyncTaskLoaderResult<T> result) {
            T data = result.data;
            WeiboException exception = result.exception;

            switch (loader.getId()) {
                case NEW_MSG_LOADER_ID:
                    newMsgLoaderIsLoading = false;
                    getPullToRefreshListView().onRefreshComplete();
                    refreshLayout(getList());

                    if (exception != null)
                        Toast.makeText(getActivity(), exception.getError(), Toast.LENGTH_SHORT).show();
                    else
                        newMsgOnPostExecute(data);
                    break;
                case MIDDLE_MSG_LOADER_ID:
                    middleMsgLoaderIsLoading = false;
                    middleMsgOnPostExecute(middleEndTag, middlePosition, data);
                    getAdapter().notifyDataSetChanged();
                    break;
                case OLD_MSG_LOADER_ID:
                    oldMsgLoaderIsLoading = false;
                    refreshLayout(getList());

                    if (Utility.isAllNotNull(exception)) {
                        Toast.makeText(getActivity(), exception.getError(), Toast.LENGTH_SHORT).show();
                        showErrorFooterView();
                    } else {
                        oldMsgOnPostExecute(data);
                        getAdapter().notifyDataSetChanged();
                        dismissFooterView();
                    }

                    break;
            }

        }

        @Override
        public void onLoaderReset(Loader<AsyncTaskLoaderResult<T>> loader) {

        }
    };

}


