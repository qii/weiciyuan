package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.dao.send.CommentNewMsgDao;
import org.qii.weiciyuan.dao.timeline.CommentsTimeLineByIdDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.send.CommentNewActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: qii
 * Date: 12-7-29
 */
public class CommentsByIdTimeLineFragment extends Fragment {

    protected View headerView;
    protected View footerView;
    protected ICommander commander;
    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;
    protected TimeLineAdapter timeLineAdapter;
    protected CommentListBean bean = new CommentListBean();

    private FriendsTimeLineGetNewMsgListTask newTask;
    private FriendsTimeLineGetOlderMsgListTask oldTask;


    private EditText et;


    public CommentListBean getList() {
        return bean;
    }

    private String token;
    private String id;

    public CommentsByIdTimeLineFragment(String token, String id) {
        this.token = token;
        this.id = id;
    }

    public CommentsByIdTimeLineFragment() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (newTask != null)
            newTask.cancel(true);

        if (oldTask != null)
            oldTask.cancel(true);

        removeListViewTimeRefresh();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putString("id", id);
        outState.putString("token", token);
    }

    public void load() {
        if ((bean == null || bean.getComments().size() == 0) && newTask == null) {
            refresh();
        }
    }

    private boolean canSend() {

        boolean haveContent = !TextUtils.isEmpty(et.getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (et.getText().toString().length() < 140);

        if (haveContent && haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(getActivity(), getString(R.string.content_cant_be_empty_and_dont_have_account), Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                et.setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(getActivity(), getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                et.setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
    }


    protected void refreshLayout(CommentListBean bean) {
        if (bean.getComments().size() > 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            if (bean.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            }
        } else {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.INVISIBLE);
            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null && bean.getComments().size() == 0) {
            bean = (CommentListBean) savedInstanceState.getSerializable("bean");
            token = savedInstanceState.getString("token");
            id = savedInstanceState.getString("id");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        }
        addListViewTimeRefresh();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_listview_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setScrollingCacheEnabled(false);
        headerView = inflater.inflate(R.layout.fragment_listview_header_layout, null);
        listView.addHeaderView(headerView);
        listView.setHeaderDividersEnabled(false);
        footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
        listView.addFooterView(footerView);

        if (bean.getComments().size() == 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
        }


        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position - 1 < getList().getComments().size() && position - 1 >= 0) {
                    listViewItemClick(parent, view, position - 1, id);
                } else if (position - 1 >= getList().getComments().size()) {
                    listViewFooterViewClick(view);
                }
            }
        });

        et = (EditText) view.findViewById(R.id.content);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        return view;
    }

    private void sendComment() {

        if (canSend()) {
            new SimpleTask().execute();
        }
    }

    class SimpleTask extends AsyncTask<Void, Void, CommentBean> {
        WeiboException e;
        SendProgressFragment progressFragment = new SendProgressFragment();

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    SimpleTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    SimpleTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected CommentBean doInBackground(Void... params) {
            CommentNewMsgDao dao = new CommentNewMsgDao(token, id, et.getText().toString());
            try {
                return dao.sendNewMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(CommentBean commentBean) {
            super.onCancelled(commentBean);
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onPostExecute(CommentBean s) {
            progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                et.setText("");
                refresh();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);

        }
    }

    protected class TimeLineAdapter extends BaseAdapter {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @Override
        public int getCount() {

            if (getList() != null && getList().getComments() != null) {
                return getList().getComments().size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if (getList() != null && getList().getSize() > 0 && position < getList().getSize())
                return getList().getComments().get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.fragment_listview_item_comments_layout, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            bindViewData(holder, position);


            return convertView;
        }

        private void bindViewData(ViewHolder holder, int position) {

            final CommentBean msg = getList().getComments().get(position);


            holder.username.setText(msg.getUser().getScreen_name());
            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url)) {
                downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
                holder.avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                        intent.putExtra("token", ((IToken) getActivity()).getToken());
                        intent.putExtra("user", msg.getUser());
                        startActivity(intent);
                    }
                });
            }
            String time = msg.getListviewItemShowTime();
            UpdateString updateString = new UpdateString(time, holder.time, msg, getActivity());
            if (!holder.time.getText().toString().equals(time)) {
                holder.time.setText(updateString);
            }
            holder.time.setTag(msg.getId());

            holder.content.setText(msg.getListViewSpannableString());

        }

    }

    static class ViewHolder {
        TextView username;
        TextView content;
        TextView time;
        ImageView avatar;

    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        CommentOperatorDialog progressFragment = new CommentOperatorDialog(bean.getComments().get(position));
        progressFragment.show(getFragmentManager(), "");
    }


    protected void listViewFooterViewClick(View view) {
        if (oldTask == null || oldTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            oldTask = new FriendsTimeLineGetOlderMsgListTask();
            oldTask.execute();
        }
    }

    protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }


    public void refresh() {

        if (getActivity() == null)
            return;

        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((AbstractAppActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();

        if (newTask == null || newTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            newTask = new FriendsTimeLineGetNewMsgListTask();
            newTask.execute();
        }
        Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
        for (String key : keys) {
            avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
            avatarBitmapWorkerTaskHashMap.remove(key);
        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.commentsbyidtimelinefragment_menu, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.commentsbyidtimelinefragment_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.commentsbyidtimelinefragment_comment:

                Intent intent = new Intent(getActivity(), CommentNewActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", id);
                startActivity(intent);

                break;

            case R.id.commentsbyidtimelinefragment_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    class FriendsTimeLineGetNewMsgListTask extends MyAsyncTask<Void, CommentListBean, CommentListBean> {
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
        protected CommentListBean doInBackground(Void... params) {

            CommentsTimeLineByIdDao dao = new CommentsTimeLineByIdDao(token, id);

            if (getList().getComments().size() > 0) {
                dao.setSince_id(getList().getComments().get(0).getId());
            }
            CommentListBean result = null;
            try {
                result = dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }

            return result;

        }

        @Override
        protected void onCancelled(CommentListBean commentListBean) {
            super.onCancelled(commentListBean);
            cleanWork();
            if (this.e != null)
                Toast.makeText(getActivity(), this.e.getError(), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(CommentListBean newValue) {
            if (newValue != null) {
                if (newValue.getComments().size() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.total) + newValue.getComments().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                    if (newValue.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        newValue.getComments().addAll(getList().getComments());
                    }

                    bean = newValue;
                    timeLineAdapter.notifyDataSetChanged();
                    listView.setSelectionAfterHeaderView();
                    headerView.findViewById(R.id.header_progress).clearAnimation();

                }
            }
            cleanWork();
            invlidateTabText();
            super.onPostExecute(newValue);

        }


        private void cleanWork() {
            headerView.findViewById(R.id.header_progress).clearAnimation();
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);
            if (bean.getComments().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
        }
    }


    class FriendsTimeLineGetOlderMsgListTask extends MyAsyncTask<Void, CommentListBean, CommentListBean> {
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
        protected CommentListBean doInBackground(Void... params) {

            CommentsTimeLineByIdDao dao = new CommentsTimeLineByIdDao(token, id);
            if (getList().getComments().size() > 0) {
                dao.setMax_id(getList().getComments().get(getList().getComments().size() - 1).getId());
            }
            CommentListBean result = null;
            try {
                result = dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }

            return result;

        }

        @Override
        protected void onCancelled(CommentListBean commentListBean) {
            super.onCancelled(commentListBean);
            cleanWork();
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));
            if (this.e != null)
                Toast.makeText(getActivity(), this.e.getError(), Toast.LENGTH_SHORT).show();
        }


        @Override
        protected void onPostExecute(CommentListBean newValue) {
            if (newValue != null && newValue.getComments().size() > 1) {
                List<CommentBean> list = newValue.getComments();
                getList().getComments().addAll(list.subList(1, list.size() - 1));
                ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));

            } else {
                ((TextView) footerView.findViewById(R.id.listview_footer)).setVisibility(View.GONE);

            }

            cleanWork();
            timeLineAdapter.notifyDataSetChanged();
            invlidateTabText();
            super.onPostExecute(newValue);
        }

        private void cleanWork() {
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

    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar.Tab tab = activity.getActionBar().getTabAt(1);
            String num = getString(R.string.comments) + "(" + bean.getTotal_number() + ")";
            tab.setText(num);

        }
    }

    private volatile boolean enableRefreshTime = true;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private class refreshTimeWorker implements Runnable {
        @Override
        public void run() {
            if (!enableRefreshTime)
                return;
            Activity activity = getActivity();
            if (activity == null)
                return;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int start = listView.getFirstVisiblePosition();
                    int end = listView.getLastVisiblePosition();


                    int visibleItemNum = listView.getChildCount();
                    for (int i = 0; i < visibleItemNum; i++) {
                        if (start + i > 0 && timeLineAdapter != null) {
                            Object object = timeLineAdapter.getItem(start + i - 1);
                            if (object instanceof CommentBean) {
                                CommentBean msg = (CommentBean) object;
                                TextView time = (TextView) listView.getChildAt(i).findViewById(R.id.time);
                                if (time != null)
                                    time.setText(msg.getListviewItemShowTime());
                            }
                        }
                    }
                }
            });


        }

    }

    private void removeListViewTimeRefresh() {
        scheduledExecutorService.shutdownNow();
    }

    protected void addListViewTimeRefresh() {

        scheduledExecutorService.scheduleAtFixedRate(new refreshTimeWorker(), 1, 1, TimeUnit.SECONDS);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {

                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:

                        enableRefreshTime = true;
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
