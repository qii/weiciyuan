package org.qii.weiciyuan.ui.maintimeline;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;

import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-27
 */
public abstract class AbstractTimeLineFragment<T extends ListBean> extends Fragment {

    protected T bean;

    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;


    protected BaseAdapter timeLineAdapter;

    protected View headerView;
    protected View footerView;
    protected ICommander commander;

    protected TimeLineGetNewMsgListTask newTask;
    protected TimeLineGetOlderMsgListTask oldTask;

    public T getList() {
        return bean;
    }


    protected void refreshLayout(T bean) {
        if (bean != null && bean.getSize() > 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        } else if (bean == null || bean.getSize() == 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.INVISIBLE);
            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.INVISIBLE);
        } else if (bean.getSize() == bean.getTotal_number()) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.INVISIBLE);
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
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
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setScrollingCacheEnabled(false);
        headerView = inflater.inflate(R.layout.fragment_listview_header_layout, null);
        listView.addHeaderView(headerView);
        listView.setHeaderDividersEnabled(false);
        footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
        listView.addFooterView(footerView);

        if (bean == null || bean.getSize() == 0) {

            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position - 1 < getList().getSize()) {

                    listViewItemClick(parent, view, position - 1, id);
                } else {

                    listViewFooterViewClick(view);
                }
            }
        });
        buildListAdapter();
        return view;
    }

    protected abstract void buildListAdapter();

    @Override
    public void onDetach() {
        super.onDetach();
        if (newTask != null)
            newTask.cancel(true);

        if (oldTask != null)
            oldTask.cancel(true);

    }


    protected void refresh() {
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
    }


    public class TimeLineGetNewMsgListTask extends MyAsyncTask<Object, T, T> {
        WeiboException e;


        @Override
        protected void onPreExecute() {
            showListView();
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_progress).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_progress).startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));
            listView.setSelection(0);
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
            if (this.e != null)
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            cleanWork();
        }

        private void cleanWork() {
            refreshLayout(getList());
            headerView.findViewById(R.id.header_progress).clearAnimation();
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);

            if (bean.getSize() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
        }
    }

    protected abstract void newMsgOnPostExecute(T newValue);

    protected abstract void oldMsgOnPostExecute(T newValue);


    public class TimeLineGetOlderMsgListTask extends MyAsyncTask<Object, T, T> {

        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.loading));
            View view = footerView.findViewById(R.id.refresh);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));

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
            if (newValue.getSize() > 1) {
                ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.click_to_load_older_message));
            } else if (newValue.getSize() == 1) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            }
            cleanWork();
            super.onPostExecute(newValue);
        }

        @Override
        protected void onCancelled(T messageListBean) {
            super.onCancelled(messageListBean);
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.click_to_load_older_message));
            if (this.e != null)
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            cleanWork();

        }

        private void cleanWork() {

            footerView.findViewById(R.id.refresh).clearAnimation();
            footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
            timeLineAdapter.notifyDataSetChanged();
        }
    }

    protected void showListView() {
        empty.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    protected void afterGetNewMsg() {

    }

    protected void afterGetOldMsg() {

    }

    protected abstract T getDoInBackgroundNewData() throws WeiboException;


    protected abstract T getDoInBackgroundOldData() throws WeiboException;
}
