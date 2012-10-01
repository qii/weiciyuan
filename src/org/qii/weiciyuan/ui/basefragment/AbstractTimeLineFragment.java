package org.qii.weiciyuan.ui.basefragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.RefreshTimeWorker;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: qii
 * Date: 12-8-27
 */
public abstract class AbstractTimeLineFragment<T extends ListBean> extends Fragment {

    protected T bean;

    protected PullToRefreshListView pullToRefreshListView;
    protected TextView empty;
    protected ProgressBar progressBar;


    protected BaseAdapter timeLineAdapter;

    protected View footerView;
    protected ICommander commander;

    protected TimeLineGetNewMsgListTask newTask;
    protected TimeLineGetOlderMsgListTask oldTask;
    protected ActionMode mActionMode;

    public T getList() {
        return bean;
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
        if (oldTask == null || oldTask.getStatus() == MyAsyncTask.Status.FINISHED) {

            oldTask = new TimeLineGetOlderMsgListTask();
            oldTask.execute();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listview_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);

        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                refresh();
            }
        });

        pullToRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                listViewFooterViewClick(null);
            }
        });

//        getListView().setScrollingCacheEnabled(false);
        getListView().setHeaderDividersEnabled(false);
        footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();


        pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

                    listViewItemClick(parent, view, position - 1, id);
                } else if (position - 1 >= getList().getSize()) {

                    listViewFooterViewClick(view);
                }
            }
        });
        buildListAdapter();
        return view;
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

    public void clearActionMode() {
        if (mActionMode != null) {

            mActionMode.finish();
            mActionMode = null;
        }
        if (pullToRefreshListView != null && getListView().getCheckedItemCount() > 0) {
            getListView().clearChoices();
            if (timeLineAdapter != null) timeLineAdapter.notifyDataSetChanged();
        }
    }

    protected abstract void buildListAdapter();

    @Override
    public void onDetach() {
        super.onDetach();
        if (newTask != null)
            newTask.cancel(true);

        if (oldTask != null)
            oldTask.cancel(true);

//        removeListViewTimeRefresh();

    }


    @Override
    public void onPause() {
        super.onPause();
        removeListViewTimeRefresh();
    }

    public void refresh() {
        if (newTask == null || newTask.getStatus() == MyAsyncTask.Status.FINISHED) {


            newTask = new TimeLineGetNewMsgListTask();
            newTask.execute();
            Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((AbstractAppActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();
            Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = ((AbstractAppActivity) getActivity()).getPictureBitmapWorkerTaskMap();


            Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
            for (String key : keys) {
                avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
                avatarBitmapWorkerTaskHashMap.remove(key);
            }

            Set<String> pKeys = pictureBitmapWorkerTaskMap.keySet();
            for (String pkey : pKeys) {
                pictureBitmapWorkerTaskMap.get(pkey).cancel(true);
                pictureBitmapWorkerTaskMap.remove(pkey);
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        timeLineAdapter.notifyDataSetChanged();
        addListViewTimeRefresh();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();

//        addListViewTimeRefresh();
    }

    public void setmActionMode(ActionMode mActionMode) {
        this.mActionMode = mActionMode;
    }


    public class TimeLineGetNewMsgListTask extends MyAsyncTask<Object, T, T> {
        WeiboException e;


        @Override
        protected void onPreExecute() {
            showListView();

            getListView().setSelection(0);
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
            pullToRefreshListView.onRefreshComplete();

        }
    }

    protected abstract void newMsgOnPostExecute(T newValue);

    protected abstract void oldMsgOnPostExecute(T newValue);


    public class TimeLineGetOlderMsgListTask extends MyAsyncTask<Object, T, T> {

        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();

            showFooterView();

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
                if (this.e != null) {
                    Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
                    showErrorFooterView();
                    pullToRefreshListView.onRefreshComplete();
                } else {
                    dismissFooterView();
                }
            }

        }

        private void cleanWork() {
            pullToRefreshListView.onRefreshComplete();
            timeLineAdapter.notifyDataSetChanged();
            dismissFooterView();
        }
    }


    protected void showListView() {
//        empty.setVisibility(View.INVISIBLE);
//        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    protected void afterGetNewMsg() {

    }

    protected void afterGetOldMsg() {

    }

    protected abstract T getDoInBackgroundNewData() throws WeiboException;


    protected abstract T getDoInBackgroundOldData() throws WeiboException;


    private volatile boolean enableRefreshTime = true;
    private ScheduledExecutorService scheduledExecutorService = null;

    public boolean isListViewFling() {
        return !enableRefreshTime;
    }


    private void removeListViewTimeRefresh() {
        scheduledExecutorService.shutdownNow();
    }

    protected void addListViewTimeRefresh() {

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new RefreshTimeWorker(AbstractTimeLineFragment.this).refreshTime();
            }
        }, 1, 1, TimeUnit.SECONDS);

        pullToRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {

                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:

                        enableRefreshTime = true;
                        timeLineAdapter.notifyDataSetChanged();
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
    }
}


