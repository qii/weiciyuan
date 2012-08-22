package org.qii.weiciyuan.ui.maintimeline;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.userinfo.StatusesListAdapter;

/**
 * User: qii
 * Date: 12-7-29
 */
public abstract class AbstractTimeLineFragment extends Fragment {
    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;


    protected BaseAdapter timeLineAdapter;
    protected MessageListBean bean = new MessageListBean();
    protected View headerView;
    protected View footerView;
    public volatile boolean isBusying = false;
    protected ICommander commander;

    protected TimeLineGetNewMsgListTask newTask;
    protected TimeLineGetOlderMsgListTask oldTask;

    @Override
    public void onDetach() {
        super.onDetach();
        if (newTask != null)
            newTask.cancel(true);

        if (oldTask != null)
            oldTask.cancel(true);

    }

    public MessageListBean getList() {
        return bean;
    }

    protected void clearAndReplaceValue(MessageListBean value) {
        bean.getStatuses().clear();
        bean.getStatuses().addAll(value.getStatuses());
    }

    protected abstract void listViewItemClick(AdapterView parent, View view, int position, long id);

    protected abstract void listViewFooterViewClick(View view);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
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

        if (bean.getStatuses().size() == 0) {

            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
        }


        timeLineAdapter = new StatusesListAdapter(getActivity(), ((AbstractAppActivity) getActivity()).getCommander(), getList(), listView);

        listView.setAdapter(timeLineAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position - 1 < getList().getStatuses().size()) {

                    listViewItemClick(parent, view, position - 1, id);
                } else {

                    listViewFooterViewClick(view);
                }
            }
        });
        return view;
    }

    protected void refreshLayout(MessageListBean bean) {
        if (bean.getStatuses().size() > 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        } else {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.INVISIBLE);
            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
    }


    protected abstract MessageListBean getDoInBackgroundNewData();

    public class TimeLineGetNewMsgListTask extends AsyncTask<Object, MessageListBean, MessageListBean> {


        @Override
        protected void onPreExecute() {
            showListView();
            isBusying = true;
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_progress).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
            Animation rotateAnimation = new RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            headerView.findViewById(R.id.header_progress).startAnimation(rotateAnimation);
            listView.setSelection(0);
        }

        @Override
        protected MessageListBean doInBackground(Object... params) {

            return getDoInBackgroundNewData();

        }

        @Override
        protected void onPostExecute(MessageListBean newValue) {
            if (newValue != null && getActivity() != null) {
                if (newValue.getStatuses().size() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
                } else if (newValue.getStatuses().size() > 0) {
                    Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                    if (newValue.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        //for speed, add old data after new data
                        newValue.getStatuses().addAll(getList().getStatuses());
                    }
                    clearAndReplaceValue(newValue);
                    timeLineAdapter.notifyDataSetChanged();
                    listView.setSelectionAfterHeaderView();

                }
            }
            cleanWork();
            afterGetNewMsg();
            super.onPostExecute(newValue);
        }

        @Override
        protected void onCancelled(MessageListBean messageListBean) {
            super.onCancelled(messageListBean);
            cleanWork();
        }

        private void cleanWork() {
            headerView.findViewById(R.id.header_progress).clearAnimation();
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);
            isBusying = false;
            if (bean.getStatuses().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
        }
    }

    protected abstract MessageListBean getDoInBackgroundOldData();

    protected void afterGetNewMsg() {

    }

    protected void afterGetOldMsg() {

    }


    public class TimeLineGetOlderMsgListTask extends AsyncTask<Object, MessageListBean, MessageListBean> {

        @Override
        protected void onPreExecute() {
            showListView();

            isBusying = true;

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.loading));
            View view = footerView.findViewById(R.id.refresh);
            view.setVisibility(View.VISIBLE);

            Animation rotateAnimation = new RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            view.startAnimation(rotateAnimation);

        }

        @Override
        protected MessageListBean doInBackground(Object... params) {

            return getDoInBackgroundOldData();

        }

        @Override
        protected void onPostExecute(MessageListBean newValue) {
            if (newValue != null) {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.old_messages), Toast.LENGTH_SHORT).show();

                getList().getStatuses().addAll(newValue.getStatuses().subList(1, newValue.getStatuses().size() - 1));

            }

            cleanWork();
            afterGetOldMsg();
            super.onPostExecute(newValue);
        }

        @Override
        protected void onCancelled(MessageListBean messageListBean) {
            super.onCancelled(messageListBean);
            cleanWork();

        }

        private void cleanWork() {
            isBusying = false;
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.click_to_load_older_message));
            footerView.findViewById(R.id.refresh).clearAnimation();
            footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
            timeLineAdapter.notifyDataSetChanged();
        }
    }

    private void showListView() {
        empty.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

}
