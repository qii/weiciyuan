package org.qii.weiciyuan.ui.basefragment;

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
import org.qii.weiciyuan.support.lib.ListViewMiddleMsgLoadingView;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.TopTipBar;
import org.qii.weiciyuan.support.lib.VelocityListView;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.lib.pulltorefresh.extras.SoundPullEventListener;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.adapter.AbstractAppListAdapter;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.loader.AbstractAsyncNetRequestTaskLoader;
import org.qii.weiciyuan.ui.loader.DummyLoader;

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
    protected TopTipBar newMsgTipBar;

    protected BaseAdapter timeLineAdapter;

    protected View footerView;


    protected static final int DB_CACHE_LOADER_ID = 0;
    protected static final int NEW_MSG_LOADER_ID = 1;
    protected static final int MIDDLE_MSG_LOADER_ID = 2;
    protected static final int OLD_MSG_LOADER_ID = 3;

    protected ActionMode mActionMode;

    protected int savedCurrentLoadingMsgViewPositon = NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION;

    public static final int NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION = -1;

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

    public TopTipBar getNewMsgTipBar() {
        return newMsgTipBar;
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

    public void loadNewMsg() {
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgCallback);
    }


    protected void loadOldMsg(View view) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().restartLoader(OLD_MSG_LOADER_ID, null, msgCallback);
    }

    public void loadMiddleMsg(String beginId, String endId, int position) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        dismissFooterView();

        Bundle bundle = new Bundle();
        bundle.putString("beginId", beginId);
        bundle.putString("endId", endId);
        bundle.putInt("position", position);
        VelocityListView velocityListView = (VelocityListView) getListView();
        bundle.putBoolean("towardsBottom", velocityListView.getTowardsOrientation() == VelocityListView.TOWARDS_BOTTOM);
        getLoaderManager().restartLoader(MIDDLE_MSG_LOADER_ID, bundle, msgCallback);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("savedCurrentLoadingMsgViewPositon", savedCurrentLoadingMsgViewPositon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_layout, container, false);
        buildLayout(inflater, view);
        return view;
    }

    protected void buildLayout(LayoutInflater inflater, View view) {
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);
        newMsgTipBar = (TopTipBar) view.findViewById(R.id.tv_unread_new_message_count_tip_bar);

        getListView().setHeaderDividersEnabled(false);
        getListView().setScrollingCacheEnabled(false);

        footerView = inflater.inflate(R.layout.listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pullToRefreshListView.setOnRefreshListener(listViewOnRefreshListener);
        pullToRefreshListView.setOnLastItemVisibleListener(listViewOnLastItemVisibleListener);
        pullToRefreshListView.setOnScrollListener(listViewOnScrollListener);
        pullToRefreshListView.setOnItemClickListener(listViewOnItemClickListener);
        pullToRefreshListView.setOnPullEventListener(getPullEventListener());
        buildListAdapter();
        if (savedInstanceState != null)
            savedCurrentLoadingMsgViewPositon = savedInstanceState.getInt("savedCurrentLoadingMsgViewPositon", NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION);
        if (savedCurrentLoadingMsgViewPositon != NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION
                && timeLineAdapter instanceof AbstractAppListAdapter) {
            ((AbstractAppListAdapter) timeLineAdapter).setSavedMiddleLoadingViewPosition(savedCurrentLoadingMsgViewPositon);
        }
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
            if (getActivity() == null) {
                return;
            }

            if (getLoaderManager().getLoader(OLD_MSG_LOADER_ID) != null)
                return;

            loadOldMsg(null);
        }
    };

    private PullToRefreshBase.OnRefreshListener<ListView> listViewOnRefreshListener = new PullToRefreshBase.OnRefreshListener<ListView>() {
        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            if (getActivity() == null) {
                return;
            }

            if (getLoaderManager().getLoader(NEW_MSG_LOADER_ID) != null)
                return;

            loadNewMsg();
        }
    };

    private SoundPullEventListener<ListView> getPullEventListener() {
        SoundPullEventListener<ListView> listener = new SoundPullEventListener<ListView>(getActivity());
        if (SettingUtility.getEnableSound()) {
            listener.addSoundEvent(PullToRefreshBase.State.RELEASE_TO_REFRESH, R.raw.psst1);
//            listener.addSoundEvent(PullToRefreshBase.State.GIVE_UP, R.raw.psst2);
            listener.addSoundEvent(PullToRefreshBase.State.RESET, R.raw.pop);
        }
        return listener;
    }


    private AdapterView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (resetActionMode())
                return;

            getListView().clearChoices();
            int headerViewsCount = getListView().getHeaderViewsCount();
            if (isPositionBetweenHeaderViewAndFooterView(position)) {
                int indexInDataSource = position - headerViewsCount;
                ItemBean msg = getList().getItem(indexInDataSource);
                if (!isNullFlag(msg)) {
                    listViewItemClick(parent, view, indexInDataSource, id);
                } else {
                    String beginId = getList().getItem(indexInDataSource + 1).getId();
                    String endId = getList().getItem(indexInDataSource - 1).getId();
                    ListViewMiddleMsgLoadingView loadingView = (ListViewMiddleMsgLoadingView) view;
                    if (!((ListViewMiddleMsgLoadingView) view).isLoading()
                            && savedCurrentLoadingMsgViewPositon == NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION) {
                        loadingView.load();
                        loadMiddleMsg(beginId, endId, indexInDataSource);
                        savedCurrentLoadingMsgViewPositon = indexInDataSource + headerViewsCount;
                        if (timeLineAdapter instanceof AbstractAppListAdapter) {
                            ((AbstractAppListAdapter) timeLineAdapter).setSavedMiddleLoadingViewPosition(savedCurrentLoadingMsgViewPositon);
                        }
                    }
                }

            } else if (isLastItem(position)) {
                loadOldMsg(view);
            }
        }

        boolean isPositionBetweenHeaderViewAndFooterView(int position) {
            return position - getListView().getHeaderViewsCount() < getList().getSize()
                    && position - getListView().getHeaderViewsCount() >= 0;
        }

        boolean resetActionMode() {
            if (mActionMode != null) {
                getListView().clearChoices();
                mActionMode.finish();
                mActionMode = null;
                return true;
            } else {
                return false;
            }
        }

        boolean isNullFlag(ItemBean msg) {
            return msg == null;
        }

        boolean isLastItem(int position) {
            return position - 1 >= getList().getSize();
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
                    TimeLineBitmapDownloader.getInstance().setPauseDownloadWork(false);
                    TimeLineBitmapDownloader.getInstance().setPauseReadWork(false);

                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    enableRefreshTime = false;
                    LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
                    TimeLineBitmapDownloader.getInstance().setPauseDownloadWork(true);
                    TimeLineBitmapDownloader.getInstance().setPauseReadWork(true);
                    onListViewScrollStateFling();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    enableRefreshTime = true;
                    LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
                    TimeLineBitmapDownloader.getInstance().setPauseDownloadWork(true);
                    onListViewScrollStateTouchScroll();
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            onListViewScroll();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        TimeLineBitmapDownloader.getInstance().setPauseDownloadWork(false);
        TimeLineBitmapDownloader.getInstance().setPauseReadWork(false);

    }

    protected void onListViewScrollStop() {

    }

    protected void onListViewScrollStateTouchScroll() {

    }

    protected void onListViewScrollStateFling() {

    }

    protected void onListViewScroll() {


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


    protected boolean allowRefresh() {
        boolean isNewMsgLoaderLoading = getLoaderManager().getLoader(NEW_MSG_LOADER_ID) != null;
        return getPullToRefreshListView().getVisibility() == View.VISIBLE && !isNewMsgLoaderLoading;
    }


    @Override
    public void onResume() {
        super.onResume();
        getListView().setFastScrollEnabled(SettingUtility.allowFastScroll());
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Loader<T> loader = getLoaderManager().getLoader(NEW_MSG_LOADER_ID);
        if (loader != null) {
            getLoaderManager().initLoader(NEW_MSG_LOADER_ID, null, msgCallback);
        }
        loader = getLoaderManager().getLoader(MIDDLE_MSG_LOADER_ID);
        if (loader != null) {
            getLoaderManager().initLoader(MIDDLE_MSG_LOADER_ID, null, msgCallback);
        }
        loader = getLoaderManager().getLoader(OLD_MSG_LOADER_ID);
        if (loader != null) {
            getLoaderManager().initLoader(OLD_MSG_LOADER_ID, null, msgCallback);
        }
    }

    public void setmActionMode(ActionMode mActionMode) {
        this.mActionMode = mActionMode;
    }

    public boolean hasActionMode() {
        return mActionMode != null;
    }


    protected abstract void newMsgOnPostExecute(T newValue, Bundle loaderArgs);

    protected abstract void oldMsgOnPostExecute(T newValue);


    protected void middleMsgOnPostExecute(int position, T newValue, boolean towardsBottom) {

        if (newValue == null)
            return;

        if (newValue.getSize() == 0 || newValue.getSize() == 1) {
            getList().getItemList().remove(position);
            getAdapter().notifyDataSetChanged();
            return;
        }

        ItemBean lastItem = newValue.getItem(newValue.getSize() - 1);

//        if (!lastItem.getId().equals(endTag)) {
//            getList().getItemList().addAll(position, newValue.getItemList().subList(1, newValue.getSize()));
//            getAdapter().notifyDataSetChanged();
//            return;
//        }
//
//        if (lastItem.getId().equals(endTag)) {
//            int nullIndex = position + newValue.getSize() - 1;
//            getList().getItemList().addAll(position, newValue.getItemList().subList(1, newValue.getSize()));
//            getList().getItemList().remove(nullIndex - 1);
//            getList().getItemList().remove(nullIndex - 1);
//            getAdapter().notifyDataSetChanged();
//            return;
//        }


    }

    protected void showListView() {
        progressBar.setVisibility(View.INVISIBLE);
    }


    private volatile boolean enableRefreshTime = true;

    public boolean isListViewFling() {
        return !enableRefreshTime;
    }


    private Loader<AsyncTaskLoaderResult<T>> createNewMsgLoader(int id, Bundle args) {
        Loader<AsyncTaskLoaderResult<T>> loader = onCreateNewMsgLoader(id, args);
        if (loader == null) {
            loader = new DummyLoader<T>(getActivity());
        }
        if (loader instanceof AbstractAsyncNetRequestTaskLoader) {
            ((AbstractAsyncNetRequestTaskLoader) loader).setArgs(args);
        }
        return loader;
    }

    private Loader<AsyncTaskLoaderResult<T>> createMiddleMsgLoader(int id, Bundle args, String middleBeginId, String middleEndId, String middleEndTag, int middlePosition) {
        Loader<AsyncTaskLoaderResult<T>> loader = onCreateMiddleMsgLoader(id, args, middleBeginId, middleEndId, middleEndTag, middlePosition);
        if (loader == null) {
            loader = new DummyLoader<T>(getActivity());
        }
        return loader;
    }

    private Loader<AsyncTaskLoaderResult<T>> createOldMsgLoader(int id, Bundle args) {
        Loader<AsyncTaskLoaderResult<T>> loader = onCreateOldMsgLoader(id, args);
        if (loader == null) {
            loader = new DummyLoader<T>(getActivity());
        }
        return loader;
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
        private int middlePosition = -1;
        private boolean towardsBottom = false;

        @Override
        public Loader<AsyncTaskLoaderResult<T>> onCreateLoader(int id, Bundle args) {
            clearActionMode();
            showListView();
            switch (id) {
                case NEW_MSG_LOADER_ID:
                    if (args == null || args.getBoolean(BundleArgsConstants.SCROLL_TO_TOP))
                        Utility.stopListViewScrollingAndScrollToTop(getListView());
                    return createNewMsgLoader(id, args);
                case MIDDLE_MSG_LOADER_ID:
                    middleBeginId = args.getString("beginId");
                    middleEndId = args.getString("endId");
                    middlePosition = args.getInt("position");
                    towardsBottom = args.getBoolean("towardsBottom");
                    return createMiddleMsgLoader(id, args, middleBeginId, middleEndId, null, middlePosition);
                case OLD_MSG_LOADER_ID:
                    showFooterView();
                    return createOldMsgLoader(id, args);
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<AsyncTaskLoaderResult<T>> loader, AsyncTaskLoaderResult<T> result) {

            T data = result != null ? result.data : null;
            WeiboException exception = result != null ? result.exception : null;
            Bundle args = result != null ? result.args : null;

            switch (loader.getId()) {
                case NEW_MSG_LOADER_ID:
                    getPullToRefreshListView().onRefreshComplete();
                    refreshLayout(getList());
                    if (Utility.isAllNotNull(exception)) {
                        newMsgTipBar.setError(exception.getError());
                        newMsgOnPostExecuteError(exception);
                    } else
                        newMsgOnPostExecute(data, args);
                    break;
                case MIDDLE_MSG_LOADER_ID:
                    if (exception != null) {
                        View view = Utility.getListViewItemViewFromPosition(getListView(), savedCurrentLoadingMsgViewPositon);
                        ListViewMiddleMsgLoadingView loadingView = (ListViewMiddleMsgLoadingView) view;
                        if (loadingView != null)
                            loadingView.setErrorMessage(exception.getError());
                    } else {
                        middleMsgOnPostExecute(middlePosition, data, towardsBottom);
//                        getAdapter().notifyDataSetChanged();
                    }
                    savedCurrentLoadingMsgViewPositon = NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION;
                    if (timeLineAdapter instanceof AbstractAppListAdapter) {
                        ((AbstractAppListAdapter) timeLineAdapter).setSavedMiddleLoadingViewPosition(savedCurrentLoadingMsgViewPositon);
                    }
                    break;
                case OLD_MSG_LOADER_ID:
                    refreshLayout(getList());

                    if (Utility.isAllNotNull(exception)) {
                        showErrorFooterView();
                    } else {
                        oldMsgOnPostExecute(data);
                        getAdapter().notifyDataSetChanged();
                        dismissFooterView();
                    }
                    break;
            }
            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<AsyncTaskLoaderResult<T>> loader) {

        }
    };

    protected void newMsgOnPostExecuteError(WeiboException exception) {

    }
}


